package com.bwang.testcases;

import com.alibaba.fastjson.JSONObject;
import com.bwang.base.BaseCase;
import com.bwang.data.GlobalEnvironment;
import com.bwang.pojo.CaseInfo;
import com.bwang.util.PhoneRandom;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * 注册测试类
 */
public class RegisterTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        //读取用例的数据
        caseInfoList = getCaseDataFromExcel(0);
    }

    @Test(dataProvider = "getRegisterDatas")
    public void testRegister(CaseInfo caseInfo) {
        //随机生成三个没有注册过的手机号码
        if (caseInfo.getCaseId() == 1) {
            GlobalEnvironment.envData.put("mobile_phone1", PhoneRandom.getRandomPhone());
        } else if (caseInfo.getCaseId() == 2) {
            GlobalEnvironment.envData.put("mobile_phone2", PhoneRandom.getRandomPhone());
        } else if (caseInfo.getCaseId() == 3) {
            GlobalEnvironment.envData.put("mobile_phone3", PhoneRandom.getRandomPhone());
        }
        //参数化替换--对当前的case
        caseInfo = paramsReplaceCaseInfo(caseInfo);
        Response res = given().log().all().
                headers(JSONObject.parseObject(caseInfo.getRequestHeader(), Map.class)).
                body(caseInfo.getInputParams()).
                when().post(caseInfo.getUrl()).
                then().log().all().extract().response();
        //1、断言响应结果
        assertExpected(caseInfo, res);
        //2、断言数据库
        assertSQL(caseInfo);
        //注册成功的密码--从用例数据里面获取
        Map inputParamsMap = JSONObject.parseObject(caseInfo.getInputParams(), Map.class);
        Object pwd = inputParamsMap.get("pwd");
        //拿到正常用例返回的响应信息
        if (caseInfo.getCaseId() ==1) {
            //保存到环境变量中
            GlobalEnvironment.envData.put("member_id1",res.path("data.id"));
            GlobalEnvironment.envData.put("mobile_phone1",res.path("data.mobile_phone"));
            GlobalEnvironment.envData.put("pwd1",pwd);
        } else if (caseInfo.getCaseId() == 2) {
            //保存到环境变量中
            GlobalEnvironment.envData.put("member_id2",res.path("data.id"));
            GlobalEnvironment.envData.put("mobile_phone2",res.path("data.mobile_phone"));
            GlobalEnvironment.envData.put("pwd2",pwd);
        } else if (caseInfo.getCaseId()==3) {
            //保存到环境变量中
            GlobalEnvironment.envData.put("member_id3",res.path("data.id"));
            GlobalEnvironment.envData.put("mobile_phone3",res.path("data.mobile_phone"));
            GlobalEnvironment.envData.put("pwd3",pwd);
        }
    }

    @DataProvider
    public Object[] getRegisterDatas() {
        //DataProvider数据提供者返回值类型可以是Object[]，也可以是Object[][]
        //怎么将list集合转换为Object[][]或者Object[]？？？
        return caseInfoList.toArray();
    }
}
