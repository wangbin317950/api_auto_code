package com.bwang.util;

import com.bwang.data.Constants;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC工具类
 */
public class JDBCUtils {
    /**
     * 获取到数据库的连接对象
     *
     * @return
     */
    public static Connection getConnection() {
        //定义数据库连接
        //Oracle：jdbc:oracle:thin:@localhost:1521:DBName
        //SqlServer：jdbc:microsoft:sqlserver://localhost:1433; DatabaseName=DBName
        //jdbc:sqlserver://pdc-sqlserver-01t.glp-inc.cn:1433;useUnicode=true&characterEncoding = utf-8; DatabaseName=GLP
        //MySql：jdbc:mysql://localhost:3306/DBName
        String url = Constants.DATA_BASE_URL;
        String username = Constants.DATA_BASE_USERNAME;
        String password = Constants.DATA_BASE_PASSWORD;
        //定义数据库连接对象
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 查询结果集中的单个数据
     *
     * @param sql 要执行的sql语句
     * @return 结果
     */
    public static Object querySingle(String sql) {
        //1、获取到数据库连接对象
        Connection conn = getConnection();
        //2、数据库操作--dbutils提供
        QueryRunner runner = new QueryRunner();
        try {
            //第一个参数：数据库连接对象，第二个参数：执行sql语句，第三个参数：接收查询结果
            return runner.query(conn, sql, new ScalarHandler<>());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            //3、关闭数据库的连接
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        //查询单个数据
        String sql = "select amount from loan where id =50949";
        System.out.println(querySingle(sql));
    }
}
