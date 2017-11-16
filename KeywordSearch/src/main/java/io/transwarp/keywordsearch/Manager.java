package io.transwarp.keywordsearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Manager {
    private static Logger log = LoggerFactory.getLogger(Manager.class);

    // 线程对象
    private class Task implements Runnable {
        @Override
        public void run() {
            try {
                Connection connection = Connector.getConnection();
                //Executor.createHBaseTable(connection);
                Executor.testSearch(connection);
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 创建线程池，执行SQL
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