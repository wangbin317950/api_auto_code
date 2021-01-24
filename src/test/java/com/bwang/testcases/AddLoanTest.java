package com.bwang.testcases;

import com.alibaba.fastjson.JSONObject;
import com.bwang.base.BaseCase;
import com.bwang.data.GlobalEnvironment;
import com.bwang.pojo.CaseInfo;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class AddLoanTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        //从Excel读取用户信息接口模块所需要的用例数据
        caseInfoList = getCaseDataFromExcel(4);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getAddLoanDatas")
    public void testAddLoan(CaseInfo caseInfo){
        Response res = given().
                headers(JSONObject.parseObject(caseInfo.getRequestHeader(), Map.class)).
                body(caseInfo.getInputParams()).
                when().post(caseInfo.getUrl()).
                then().log().all().extract().response();
        //获取项目id，保存到环境变量中
        if (res.path("data.id") != null) {
            GlobalEnvironment.envData.put("loan_id", res.path("data.id"));
            System.out.println("loan_id::" + GlobalEnvironment.envData.get("loan_id"));
        }
        //对当前case进行参数化替换
        caseInfo = paramsReplaceCaseInfo(caseInfo);
        //接口响应断言
        assertExpected(caseInfo, res);
        //数据库断言
        assertSQL(caseInfo);
    }

    @DataProvider
    public Object[] getAddLoanDatas() {
        //DataProvider数据提供者返回值类型可以是Object[]，也可以是Object[][]
        return caseInfoList.toArray();
    }
}
