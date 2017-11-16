package io.transwarp.keywordsearch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Executor {
    // 创建Hyperbase表
    public static void createHBaseTable(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String sql1 = Constant.PARAMETER;
            statement.execute(sql1);
            String sql2 = Constant.CREATE_EXTERNAL_TABLE;
            statement.execute(sql2);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 测试SQL查询
    public static void testSearch(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String sql1 = Constant.PARAMETER;
            statement.execute(sql1);
            String[] sql = Constant.SEARCH_SQL.split(";");
            int n = sql.length;
            for (int i = 0; i < n; ++i) {
                ResultSet rs = statement.executeQuery(sql[i]);
                while (rs.next()) {
                    System.out.println(rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4));
                }
                rs.close();
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
