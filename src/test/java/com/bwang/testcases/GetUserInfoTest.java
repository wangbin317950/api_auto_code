package com.bwang.testcases;

import com.alibaba.fastjson.JSONObject;
import com.bwang.base.BaseCase;
import com.bwang.pojo.CaseInfo;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static io.restassured.RestAssured.given;

/**
 * 获取用户信息测试类
 */
public class GetUserInfoTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        //从Excel读取用户信息接口模块所需要的用例数据
        caseInfoList = getCaseDataFromExcel(2);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);
    }
    @Test(dataProvider = "getUserInfoDatas")
    public void testGetUserInfo(CaseInfo caseInfo){
        Response res = given().log().all().
                headers(JSONObject.parseObject(caseInfo.getRequestHeader(), Map.class)).
                when().get(caseInfo.getUrl()).
                then().log().all().extract().response();
        //断言
        assertExpected(caseInfo,res);
    }
    @DataProvider
    public Object[] getUserInfoDatas() {
        //DataProvider数据提供者返回值类型可以是Object[]，也可以是Object[][]
        return caseInfoList.toArray();
    }

    public static void main(String[] args) {
        String str1 = "/member/{{member_id}}/info/{{mobile_phone}}";
        String str2 = "{\n" +
                "    \"code\": 0,\n" +
                "    \"msg\": \"OK\", \"data.id\":{{member_id}},\n" +
                "\"data.mobile_phone\":\"15317900639\"\n" +
                "}";
        //参数化替换功能实现
        //正则表达式：
        //"."匹配任意字符
        //"*"匹配前面的字符零次或者任意多次
        //"?"贪婪匹配
        //.*?
        //1、定义正则表达式
        String regex = "\\{\\{(.*?)\\}\\}";
        //2、通过正则表达式编译出来一个匹配器pattern
        Pattern pattern = Pattern.compile(regex);
        //3、开始进行匹配 参数：为你要去在哪一个字符串里面去进行匹配
        Matcher matcher = pattern.matcher(str1);
        //4、连续查找、连续匹配
        while (matcher.find()) {
            //输出找到匹配的结果
            System.out.println("group(0):" + matcher.group(0));
            String findStr = matcher.group(0);
            System.out.println("group(1):" + matcher.group(1));
            //每一次匹配上就去进行替换
            str1 = str1.replace(findStr, "10010011");
        }
        System.out.println(str1);
    }
}
