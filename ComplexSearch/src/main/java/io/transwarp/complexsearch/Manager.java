package io.transwarp.complexsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Manager {
    private static Logger log = LoggerFactory.getLogger(Manager.class);

    // 线程逻辑
    private class Task implements Runnable {
        @Override
        public void run() {
            try {
                Connection connection = Connector.getConnection();
                Executor.complexSearchSQL(connection, Constant.ES_HYPERBASE_SQL);
                Executor.complexSearchSQL(connection, Constant.ESDRIVE_SQL);
                Executor.complexSearchSQL(connection, Constant.HYPERDRIVE_SQL);
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 启动多线程查询
    public void go() {
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(Constant.THREAD_POOL_SIZE));
        for (int i = 0; i < Integer.parseInt(Constant.THREAD_NUM); ++i) {
            log.info("thread " + i + " is ready start!");

            executorService.execute(new Task());

            log.info("thread " + i + " has finished!");
        }
        executorService.shutdown();
    }
}
