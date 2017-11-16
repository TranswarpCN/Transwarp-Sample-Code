package io.transwarp.batchinsert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

// JDBC连接线程池管理
public class ConnectorPools {
    // 线程池中默认线程的个数为5
    private static int worker_num = Integer.parseInt(Constant.connectionsNum);
    // 工作线程
    private List<Connection> connectionsQueue = new LinkedList<Connection>();
    private static ConnectorPools threadPool;

    // 创建具有默认线程个数的线程池
    private ConnectorPools() {
        this(5);
    }

    // 创建线程池,worker_num为线程池中工作线程的个数
    private ConnectorPools(int worker_num) {
        ConnectorPools.worker_num = worker_num;
        for (int i = 0; i < worker_num; i++) {
            connectionsQueue.add(ConnectorSingleton.getConnectorSingleton().getConnection());
            // 开启线程池中的线程
        }
    }

    // 单态模式，获得一个默认线程个数的线程池
    public static ConnectorPools getConnectorPool() {
        return getConnectorPool(ConnectorPools.worker_num);
    }

    // 从线程池中获取一个连接
    public Connection getConnection(){
        synchronized (connectionsQueue) {
            while(connectionsQueue.isEmpty()){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return connectionsQueue.remove(0);
        }
    }

    // 将连接归还到线程池
    public void backConnection(Connection connection){
        synchronized (connectionsQueue) {
            connectionsQueue.add(connection);
            connectionsQueue.notify();
        }
    }

    /*
     * 单态模式，获得一个指定线程个数的线程池,worker_num(>0)为线程池中工作线程的个数
     *  worker_num<=0创建默认的工作线程个数
     */
    public static ConnectorPools getConnectorPool(int worker_num1) {
        if (worker_num1 <= 0)
            worker_num1 = ConnectorPools.worker_num;
        if(threadPool == null){
            threadPool = new ConnectorPools(worker_num);
        }
        return threadPool;
    }

    // 销毁线程池,该方法保证在所有任务都完成的情况下才销毁所有线程，否则等待任务完成才销毁
    public void destroy() {
        while (connectionsQueue.size() != ConnectorPools.worker_num) {
            // 如果还有任务没执行完成，就先睡会吧
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(connectionsQueue.isEmpty()){
            Connection con  = connectionsQueue.remove(0);
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connectionsQueue.clear();
        // 清空任务队列
    }

    // 返回工作线程的个数
    public int getWorkThreadNumber() {
        return worker_num;
    }
}
