package com.bwang.testcases;

import com.alibaba.fastjson.JSONObject;
import com.bwang.base.BaseCase;
import com.bwang.pojo.CaseInfo;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class RechargeTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        //从Excel读取用户信息接口模块所需要的用例数据
        caseInfoList = getCaseDataFromExcel(3);
        //参数化替换
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getRechargeDatas")
    public void testRecharge(CaseInfo caseInfo) {
        Response res = given().log().all().
                headers(JSONObject.parseObject(caseInfo.getRequestHeader(), Map.class)).
                body(caseInfo.getInputParams()).
                when().post(caseInfo.getUrl()).
                then().log().all().extract().response();
        //接口响应断言
        assertExpected(caseInfo, res);
        //数据库断言
        assertSQL(caseInfo);
    }

    @DataProvider
    public Object[] getRechargeDatas() {
        //DataProvider数据提供者返回值类型可以是Object[]，也可以是Object[][]
        return caseInfoList.toArray();
    }

    public static void main(String[] args) {
        //Double？double？
        Double a = 0.01;
        Float b = 0.01f;
        if (a instanceof Double) {
            System.out.println("是Double类型");
        }
        if (b instanceof Float) {
            System.out.println("是Float类型");
        }
        //类型不一致会导致断言失败
        //BigDecimal-->大的小数，用它来进行运算可以避免精度的丢失（金额）
        //把原始的类型Float/Double转化成为BigDecimal
        //rest-assured如果接口响应结果返回的是json，并且json里面有小数，
        //1、解决方案：Gpath表达式获取结果的时候用BigDecimal来存储（实际值）
        //2、把期望值也转化成BigDecimal
        BigDecimal bigDecimala = new BigDecimal(a.toString());
        BigDecimal bigDecimalb = new BigDecimal(b.toString());
        Assert.assertEquals(bigDecimala, bigDecimalb);
    }
}
