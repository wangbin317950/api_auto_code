package com.bwang.testcases;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.alibaba.fastjson.JSONObject;
import com.bwang.base.BaseCase;
import com.bwang.data.GlobalEnvironment;
import com.bwang.pojo.CaseInfo;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * 登录测试类
 */
public class LoginTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        //从Excel读取登录接口模块所需要的用例数据
        caseInfoList = getCaseDataFromExcel(1);
    }

    @Test(dataProvider = "getLoginDatas")
    public void testLogin(CaseInfo caseInfo){
        System.out.println("caseInfo:" + caseInfo);
        //参数化替换--对当前的case
        caseInfo = paramsReplaceCaseInfo(caseInfo);
        Response res = given().
                headers(JSONObject.parseObject(caseInfo.getRequestHeader(), Map.class)).
                body(caseInfo.getInputParams()).
                when().post(caseInfo.getUrl()).
                then().log().all().extract().response();
        //断言
        assertExpected(caseInfo, res);
        if (caseInfo.getCaseId() == 1) {
            //拿到正常用例返回信息里面的token
            String token = res.path("data.token_info.token");
            //保存到环境变量中
            GlobalEnvironment.envData.put("token1", token);
        } else if (caseInfo.getCaseId() == 2) {
            //拿到正常用例返回信息里面的token
            String token = res.path("data.token_info.token");
            //保存到环境变量中
            GlobalEnvironment.envData.put("token2", token);
        } else if (caseInfo.getCaseId() == 3) {
            //拿到正常用例返回信息里面的token
            String token = res.path("data.token_info.token");
            //保存到环境变量中
            GlobalEnvironment.envData.put("token3", token);
        }
    }

    @DataProvider
    public Object[] getLoginDatas() {
        //DataProvider数据提供者返回值类型可以是Object[]，也可以是Object[][]
        //怎么list集合转换为Object[][]或者Object[]？？？
        return caseInfoList.toArray();
    }

    public static void main(String[] args) {
        //读取Excel测试用例数据，poi麻烦、笨重
        //推荐读取Excel技术：EasyPOI
        //第一个参数：File对象；第二个参数：映射的实体类.class；第三个参数：读取配置对象
        File excelFile = new File("src/test/resources/api_testcases_futureloan_V4.xls");
        ImportParams importParams = new ImportParams();
        //sheet索引，默认的起始值为0
        importParams.setStartSheetIndex(0);
        //要读取的sheet数量，默认为1
        importParams.setSheetNum(2);
        List<CaseInfo> list = ExcelImportUtil.importExcel(excelFile, CaseInfo.class, importParams);
        for (CaseInfo caseInfo : list) {
            System.out.println(caseInfo);
        }
    }
}
