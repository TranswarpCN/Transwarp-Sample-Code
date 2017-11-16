package io.transwarp.kafkaProducer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class    kafkaProducer {
    // 构造函数
    public kafkaProducer() {

    }

    // 创建线程
    private static class Task implements Runnable {
        int num;
        String topic;
        Producer producer;
        CopyOnWriteArrayList<String> fileList;
        int batchSize;

        Task(int num, String topic, Producer producer, CopyOnWriteArrayList<String> fileList, int batchSize) {
            this.num = num;
            this.topic = topic;
            this.producer = producer;
            this.fileList = fileList;
            this.batchSize = batchSize;
        }

        @Override
        @SuppressWarnings("unchecked")
        // 线程读取文件列表
        public void run() {
            try {
                while (fileList.size() != 0) {
                    String s = fileList.remove(0);
                    FileInputStream fileInputStream =
                            new FileInputStream(s);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
                    String strLine;
                    int k = 0;
                    System.out.println("Thread " + String.valueOf(num) + " is start reading " + s);
                    List<KeyedMessage> keyedMessageList = new ArrayList<>();
                    while ((strLine = br.readLine()) != null) {
                        String ip = s + "--" + String.valueOf(k);
                        ++k;
                        keyedMessageList.add(new KeyedMessage<>(topic, ip, strLine));
                        if (k % batchSize == 0) {
                            producer.send(keyedMessageList);
                            keyedMessageList = new ArrayList<>();
                        }
                    }
                    producer.send(keyedMessageList);
                    br.close();
                    System.out.println("Thread " + String.valueOf(num) + " has read " + s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                producer.close();
            }
        }
    }

    // 读取配置文件，创建线程池，运行线程
    public void go() {
        Constant constant = new Constant();
        kafkaProperties kafkaProperties = new kafkaProperties();
        ProducerConfig config = new ProducerConfig(kafkaProperties.properties());

        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(constant.THREAD_POOL_SIZE));

        String topic = constant.TOPIC_NAME;
        Task[] tasks = new Task[Integer.parseInt(constant.THREAD_NUM)];
        String[] folders = constant.FILE_FOLDERS.split(";");
        int batchSize = Integer.parseInt(constant.BATCH_SIZE);
        CopyOnWriteArrayList<String> fileList = addFiles(folders);

        for (int i = 0; i < tasks.length; ++i) {
            tasks[i] = new Task(i, topic, new Producer<String, String>(config), fileList, batchSize);
        }

        for (Task task : tasks) {
            executorService.execute(task);
        }
        executorService.shutdown();
    }

    // 创建列表，将多个文件夹下文件以及子文件夹文件加入列表中
    private static CopyOnWriteArrayList<String> addFiles(String[] folders) {
        CopyOnWriteArrayList<String> fileList = new CopyOnWriteArrayList<>();
        for (String folder : folders) {
            File file = new File(folder);
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.isDirectory()) {
                        fileList.add(f.toString());
                    } else {
                        recursion(f.toString(), fileList);
                    }
                }
            }
        }
        return fileList;
    }

    // 辅助函数，读取子文件夹中文件
    private static void recursion(String root, CopyOnWriteArrayList<String> fileList) {
        File file = new File(root);
        File[] subFile = file.listFiles();
        if (subFile != null) {
            for (int i = 0; i < subFile.length; i++) {
                if (subFile[i].isDirectory()) {
                    recursion(subFile[i].getAbsolutePath(), fileList);
                } else {
                    fileList.add(subFile[i].getAbsolutePath());
                }
            }
        }
    }
}
