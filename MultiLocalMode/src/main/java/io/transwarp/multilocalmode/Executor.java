package io.transwarp.multilocalmode;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Executor {
    // 执行SQL
    public static void selectSQL(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String sql = Constant.HIGH_CONCURRENCY_SQL;
            String[] parameters = Constant.PARAMETER.split(";");
            for (String s : parameters) {
                statement.execute(s);
            }
            long pt = System.currentTimeMillis();
            statement.execute(sql);
            statement.getResultSet().next();
            long et = System.currentTimeMillis();
            System.out.println(et - pt);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
