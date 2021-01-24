package com.bwang.base;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.fastjson.JSONObject;
import com.bwang.data.Constants;
import com.bwang.data.GlobalEnvironment;
import com.bwang.pojo.CaseInfo;
import com.bwang.util.JDBCUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

/**
 * 所有测试用例类的父类，里面放置公用方法
 */
public class BaseCase {
    @BeforeTest
    public void globalSetup() throws FileNotFoundException {
        //整体全局性前置/初始化 
        //1、设置项目的BaseUrl
        RestAssured.baseURI = Constants.BASE_URL;
        //2、设置接口响应结果，如果是Json返回的小数类型，使用BigDecimal类型来存储
        //让REST-Assured返回json小数的时候，使用BigDecimal类型来存储小数（默认是Float存储的）
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL));
        //3、设置项目的日志存储到本地文件中log/test_all.log
        PrintStream fileOutPutStream = new PrintStream(new File("all.log"));
        RestAssured.filters(new RequestLoggingFilter(fileOutPutStream), new ResponseLoggingFilter(fileOutPutStream));
    }

    /**
     * 从Excel读取所需的用例数据
     *
     * @param index sheet的索引，从0开始
     * @return caseinfo实体对象集合
     */
    public List<CaseInfo> getCaseDataFromExcel(int index) {
        //从Excel读取所有的用例数据，poi麻烦、笨重
        //推荐读取Excel技术：EasyPOI
        File excelFile = new File(Constants.EXCEL_PATH);
        ImportParams importParams = new ImportParams();
        importParams.setStartSheetIndex(index);
        return ExcelImportUtil.importExcel(excelFile, CaseInfo.class, importParams);
    }

    /**
     * 接口响应断言方法，断言期望值和实际值
     *
     * @param caseInfo 用例信息
     * @param res      接口响应结果
     */
    public void assertExpected(CaseInfo caseInfo, Response res) {
        //1、使用fastjson把断言数据由json字符串转换为map
        Map<String, Object> expectedMap = JSONObject.parseObject(caseInfo.getExpected(), Map.class);
        //2、循环遍历取到map里面的每一组键值对
        Set<Map.Entry<String, Object>> set = expectedMap.entrySet();
        for (Map.Entry<String, Object> map : set) {
            //map.getValue()判断一下是不是小数类型（Float/Double）
            Object expected = map.getValue();
            //判断期望值（期望的json结果是小数类型-Float/Double才需要转换）
            if (expected instanceof Float || expected instanceof Double) {
                BigDecimal bigDecimalData = new BigDecimal(expected.toString());
                Assert.assertEquals(res.path(map.getKey()), bigDecimalData, "接口响应断言失败");
            } else {
                Assert.assertEquals(res.path(map.getKey()), expected, "接口响应断言失败");
            }
        }
    }

    /**
     * 数据库断言
     *
     * @param caseInfo 用例信息
     */
    public void assertSQL(CaseInfo caseInfo) {
        if (caseInfo.getCheckSQL() != null) {
            //使用fastjson把json字符串转换成MAP
            Map<String, Object> checkSQLMap = JSONObject.parseObject(caseInfo.getCheckSQL(), Map.class);
            Set<Map.Entry<String, Object>> set = checkSQLMap.entrySet();
            for (Map.Entry<String, Object> map : set) {
                String sql = map.getKey();
                //查询数据库
                Object actual = JDBCUtils.querySingle(sql);
                //1、数据库查询返回的结果是Long类型，Excel读取的期望值结果是Integer
                if (actual instanceof Long) {
                    //把expected转成Long类型
                    Long expected = new Long(map.getValue().toString());
                    Assert.assertEquals(actual, expected, "Long类型和Integer类型数据库断言失败");
                    //2、数据库查询返回的结果是BigDecimal类型，Excel读取的期望结果是Double
                } else if (actual instanceof BigDecimal) {
                    //把expected转成BigDecimal
                    BigDecimal expected = new BigDecimal(map.getValue().toString());
                    Assert.assertEquals(actual, expected, "BigDecimal类型和Double类型数据库断言失败");
                } else {
                    Assert.assertEquals(actual, map.getValue(), "字符串类型数据库断言失败");
                }
            }
        }
    }

    /**
     * 正则替换
     *
     * @param sourceStr 原始的字符串
     * @return 查找匹配替换之后的内容
     */
    public String regexReplace(String sourceStr) {
        //如果参数化的原始字符串为null的话，不需要进行参数化替换的过程
        if (sourceStr == null) {
            return sourceStr;
        }
        //1、定义正则表达式
        String regex = "\\{\\{(.*?)\\}\\}";
        //2、通过正则表达式编译出来一个匹配器pattern
        Pattern pattern = Pattern.compile(regex);
        //3、开始进行匹配 参数：要进行匹配的字符串
        Matcher matcher = pattern.matcher(sourceStr);
        //4、连续查找、连续匹配
        while (matcher.find()) {
            //匹配到整个正则表达式对应的字符串内容
            String findStr = matcher.group(0);
            //匹配到小括号里面的内容
            String singleStr = matcher.group(1);
            //5、找到环境变量里面对应的值
            Object replacStr = GlobalEnvironment.envData.get(singleStr);
            //环境变量里面对应的key值不为null,才进行替换
            if (replacStr != null) {
                //6、替换原始字符串中的内容
                sourceStr = sourceStr.replace(findStr, replacStr + "");
            }
        }
        return sourceStr;
    }

    /**
     * 对所有的case参数化替换
     *
     * @param caseInfoList 当前测试类中的所有测试用例数据
     * @return 参数化替换之后的用例数据
     */
    public List<CaseInfo> paramsReplace(List<CaseInfo> caseInfoList) {
        //对五块做参数化处理（请求头、接口地址、参数输入、期望返回结果、数据库效验）
        for (CaseInfo caseInfo : caseInfoList) {
            //参数化替换请求头
            String requestHeader = regexReplace(caseInfo.getRequestHeader());
            caseInfo.setRequestHeader(requestHeader);
            //参数化替换请求地址
            String url = regexReplace(caseInfo.getUrl());
            caseInfo.setUrl(url);
            //参数化替换输入参数
            String inputParams = regexReplace(caseInfo.getInputParams());
            caseInfo.setInputParams(inputParams);
            //参数化替换期望值
            String expected = regexReplace(caseInfo.getExpected());
            caseInfo.setExpected(expected);
            //参数化替换数据库效验
            String checkSQL = regexReplace(caseInfo.getCheckSQL());
            caseInfo.setCheckSQL(checkSQL);
        }
        return caseInfoList;
    }

    /**
     * 对当前一条case进行参数化替换
     *
     * @param caseInfo 当前测试类中的测试用例数据
     * @return 参数化替换之后的用例数据
     */
    public CaseInfo paramsReplaceCaseInfo(CaseInfo caseInfo) {
        //对五块做参数化处理（请求头、接口地址、参数输入、期望返回结果、数据库效验）
        //参数化替换请求头
        String requestHeader = regexReplace(caseInfo.getRequestHeader());
        caseInfo.setRequestHeader(requestHeader);
        //参数化替换请求地址
        String url = regexReplace(caseInfo.getUrl());
        caseInfo.setUrl(url);
        //参数化替换输入参数
        String inputParams = regexReplace(caseInfo.getInputParams());
        caseInfo.setInputParams(inputParams);
        //参数化替换期望值
        String expected = regexReplace(caseInfo.getExpected());
        caseInfo.setExpected(expected);
        //参数化替换数据库效验
        String checkSQL = regexReplace(caseInfo.getCheckSQL());
        caseInfo.setCheckSQL(checkSQL);
        return caseInfo;
    }

    /**
     * 使用jackson，把json字符串转换成map类型
     *
     * @param jsonStr json字符串
     * @return
     */
    public Map<String, Object> fromJsonToMap(String jsonStr) {
        //将字符串请求头转换成Map
        //实现思路：原始的字符串转换会比较麻烦，把原始的字符串通过json数据类型保存，通过ObjectMapper来去转换为Map
        //jackson json字符串--》Map
        //1、实例化objectMapper对象
        ObjectMapper objectMapper = new ObjectMapper();
        //readValue方法参数解释：(第一个参数：json字符串,第二个参数：转成的类型（Map）)
        try {
            return objectMapper.readValue(jsonStr, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
