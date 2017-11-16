package io.transwarp.complexsearch;

import java.sql.*;

public class Executor {
    // 通过JDBC进行查询SQL
    public static void complexSearchSQL(Connection connection, String selectSQL) {
        try {
            Statement statement = connection.createStatement();
            String parameter = Constant.PARAMETER;
            statement.execute(parameter);
            ResultSet rs = statement.executeQuery(selectSQL);
            ResultSetMetaData rsmd = rs.getMetaData();
            int size = rsmd.getColumnCount();
            while (rs.next()) {
                StringBuffer value = new StringBuffer();
                for (int i = 0; i < size; i++) {
                    value.append(rs.getString(i + 1)).append("\t");
                }
                System.out.println(value.toString());
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
