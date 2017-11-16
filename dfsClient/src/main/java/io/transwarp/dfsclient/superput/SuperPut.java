package io.transwarp.dfsclient.superput;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.io.compress.Lz4Codec;
import org.apache.hadoop.io.compress.SnappyCodec;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SuperPut {
    private static Constant constant = new Constant();
    private static final DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();
    private static AtomicInteger count = new AtomicInteger();
    private static int num;

    // 将文件夹中文件添加到队列中
    public static void addFiles(String[] folders) {
        String[] file_types = constant.FILE_TYPE_FILTER.split(",");
        for (String folder : folders) {
            File file = new File(folder);
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    String s1 = f.toString();
                    if (!f.isDirectory() && !s1.contains("COMPLETED_")) {
                        for (String s2 : file_types) {
                            int l1 = s1.length();
                            int l2 = s2.length();
                            String s3 = s1.substring(l1-l2,l1);
                            if (s3.equals(s2)) {
                                try {
                                    blockingQueue.put(s1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // SuperPut作为Daemon进程
    private static class TaskAsDaemon implements Runnable {
        int num;
        Configuration configuration;
        String hdfs_dir;

        TaskAsDaemon(int num, Configuration configuration, String hdfs_dir) {
            this.num = num;
            this.configuration = configuration;
            this.hdfs_dir = hdfs_dir;
        }

        // 线程逻辑，一旦发现目标文件夹中新增文件，就准备上传
        @Override
        public void run() {
            while (true) {
                try {
                    String file = blockingQueue.take();
                    String[] arr2 = file.split("\\\\");
                    String f2 = arr2[arr2.length-1];
                    if (Boolean.valueOf(constant.OPEN_KERBEROS)) {
                        UserGroupInformation.setConfiguration(configuration);
                        UserGroupInformation.loginUserFromKeytab(constant.KERBEROS_USER, constant.KEYTAB);
                    }
                    DistributedFileSystem distributedFileSystem = new DistributedFileSystem();
                    distributedFileSystem.initialize(new URI(configuration.get("fs.defaultFS")), configuration);
                    if (Boolean.valueOf(constant.COMPRESSION_SWITCHER)
                            && Integer.valueOf(constant.BUFFER_SIZE) <= 1024*1024) {
                        CompressionCodec codec = null;
                        if (constant.COMPRESSION_CODEC.equals("snappy")) {
                            SnappyCodec snappyCodec = new SnappyCodec();
                            snappyCodec.setConf(configuration);
                            codec = snappyCodec;
                        } else if (constant.COMPRESSION_CODEC.equals("lz4")) {
                            Lz4Codec lz4Codec = new Lz4Codec();
                            lz4Codec.setConf(configuration);
                            codec = lz4Codec;
                        } else if (constant.COMPRESSION_CODEC.equals("gzip")) {
                            GzipCodec gzipCodec = new GzipCodec();
                            gzipCodec.setConf(configuration);
                            codec = gzipCodec;
                        }
                        String hdfsFileName = hdfs_dir + "/" + f2 + codec.getDefaultExtension();
                        if (!distributedFileSystem.exists(new Path(hdfsFileName))) {
                            FSDataOutputStream fsDataOutputStream = distributedFileSystem.create(
                                    new Path(hdfsFileName), true, Integer.valueOf(constant.BUFFER_SIZE));
                            FileInputStream fileInputStream = new FileInputStream(file);
                            OutputStream outputStream = codec.createOutputStream(fsDataOutputStream);
                            System.out.println(sdf.format(new Date()) + " file " + f2 + " is uploading by thread " + num);
                            IOUtils.copyBytes(fileInputStream, outputStream, Integer.valueOf(constant.BUFFER_SIZE));
                            outputStream.close();
                            fileInputStream.close();
                            System.out.println(sdf.format(new Date()) + " file " + f2 + " has been uploaded by thread " + num);
                        }
                    } else {
                        if (!distributedFileSystem.exists(new Path(hdfs_dir + "/" + f2))) {
                            System.out.println(sdf.format(new Date()) + " file " + f2 + " is uploading by thread " + num);
                            distributedFileSystem.copyFromLocalFile(false, false, new Path(file), new Path(hdfs_dir));
                            System.out.println(sdf.format(new Date()) + " file " + f2 + " has been uploaded by thread " + num);
                        }
                    }
                    if (Boolean.getBoolean(constant.DELETE_LOCAL_DATA)) {
                        File f = new File(file);
                        f.delete();
                    } else {
                        File f = new File(file);
                        String[] arr1 = String.valueOf(f).split("\\\\");
                        f.renameTo(new File(String.valueOf(f).replace(arr1[arr1.length-1],"COMPLETED_"+arr1[arr1.length-1])));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // SuperPut作为普通进程，一旦发现目标文件夹中新增文件，就准备上传
    private static class TaskAsUser implements Runnable {
        int num;
        Configuration configuration;
        String hdfs_dir;

        TaskAsUser(int num, Configuration configuration, String hdfs_dir) {
            this.num = num;
            this.configuration = configuration;
            this.hdfs_dir = hdfs_dir;
        }

        // 线程逻辑，一旦发现目标文件夹中新增文件，就准备上传
        @Override
        public void run() {
            while (blockingQueue.size() != 0) {
                try {
                    System.out.println("There are still " + blockingQueue.size() + " file(s) in the queue waited for been uploading");
                    String file = blockingQueue.poll();
                    String[] arr2 = file.split("\\\\");
                    String f2 = arr2[arr2.length-1];
                    if (Boolean.valueOf(constant.OPEN_KERBEROS)) {
                        UserGroupInformation.setConfiguration(configuration);
                        UserGroupInformation.loginUserFromKeytab(constant.KERBEROS_USER, constant.KEYTAB);
                    }
                    DistributedFileSystem distributedFileSystem = new DistributedFileSystem();
                    distributedFileSystem.initialize(new URI(configuration.get("fs.defaultFS")), configuration);
                    if (Boolean.valueOf(constant.COMPRESSION_SWITCHER)
                            && Integer.valueOf(constant.BUFFER_SIZE) <= 1024*1024) {
                        CompressionCodec codec = null;
                        if (constant.COMPRESSION_CODEC.equals("snappy")) {
                            SnappyCodec snappyCodec = new SnappyCodec();
                            snappyCodec.setConf(configuration);
                            codec = snappyCodec;
                        } else if (constant.COMPRESSION_CODEC.equals("lz4")) {
                            Lz4Codec lz4Codec = new Lz4Codec();
                            lz4Codec.setConf(configuration);
                            codec = lz4Codec;
                        } else if (constant.COMPRESSION_CODEC.equals("gzip")) {
                            GzipCodec gzipCodec = new GzipCodec();
                            gzipCodec.setConf(configuration);
                            codec = gzipCodec;
                        }
                        String hdfsFileName = hdfs_dir + "/" + f2 + codec.getDefaultExtension();
                        if (!distributedFileSystem.exists(new Path(hdfsFileName))) {
                            FSDataOutputStream fsDataOutputStream = distributedFileSystem.create(
                                    new Path(hdfsFileName), true, Integer.valueOf(constant.BUFFER_SIZE));
                            FileInputStream fileInputStream = new FileInputStream(file);
                            OutputStream outputStream = codec.createOutputStream(fsDataOutputStream);
                            System.out.println(sdf.format(new Date()) + " file " + f2 + " is uploading by thread " + num);
                            IOUtils.copyBytes(fileInputStream, outputStream, Integer.valueOf(constant.BUFFER_SIZE));
                            outputStream.close();
                            fileInputStream.close();
                            System.out.println(sdf.format(new Date()) + " file " + f2 + " has been uploaded by thread " + num);
                        }
                    } else {
                        if (!distributedFileSystem.exists(new Path(hdfs_dir + "/" + f2))) {
                            System.out.println(sdf.format(new Date()) + " file " + f2 + " is uploading by thread " + num);
                            distributedFileSystem.copyFromLocalFile(false, false, new Path(file), new Path(hdfs_dir));
                            System.out.println(sdf.format(new Date()) + " file " + f2 + " has been uploaded by thread " + num);
                        }
                    }
                    if (Boolean.getBoolean(constant.DELETE_LOCAL_DATA)) {
                        File f = new File(file);
                        f.delete();
                    } else {
                        File f = new File(file);
                        String[] arr1 = String.valueOf(f).split("\\\\");
                        count.getAndIncrement();
                        f.renameTo(new File(String.valueOf(f).replace(arr1[arr1.length-1],"COMPLETED_"+arr1[arr1.length-1])));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 负责监控目标文件夹是否有变化
    public static void go(int option) {
        if (option == 1) {
            hdfsProperties hdfsProperties = new hdfsProperties();

            ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(constant.THREAD_POOL_SIZE));

            String hdfs_dir = constant.HDFS_DATA_DIR;
            TaskAsDaemon[] tasks = new TaskAsDaemon[Integer.parseInt(constant.THREAD_NUM)];

            Configuration configuration = hdfsProperties.getConf();

            for (int i = 0; i < tasks.length; ++i) {
                tasks[i] = new TaskAsDaemon(i, configuration, hdfs_dir);
            }

            for (TaskAsDaemon task : tasks) {
                executorService.execute(task);
            }
        } else if (option == 2) {
            hdfsProperties hdfsProperties = new hdfsProperties();

            ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(constant.THREAD_POOL_SIZE));

            String hdfs_dir = constant.HDFS_DATA_DIR;
            TaskAsUser[] tasks = new TaskAsUser[Integer.parseInt(constant.THREAD_NUM)];

            Configuration configuration = hdfsProperties.getConf();

            for (int i = 0; i < tasks.length; ++i) {
                tasks[i] = new TaskAsUser(i, configuration, hdfs_dir);
            }

            for (TaskAsUser task : tasks) {
                executorService.execute(task);
            }

            for (;;) {
                if (count.get() == num) {
                    executorService.shutdown();
                    System.out.println(sdf.format(new Date()) + ": ByeBye");
                    break;
                }
            }

            // executorService.shutdown();
        } else {
            System.out.println("Parameter error, program will exit");
        }
    }

    // 主函数
    public static void main(String... args) {
        try {
            System.out.println("Please input a parameter for SuperPut, 1 means daemon process, 2 means process exits when no file need to upload");
            // int option = Integer.parseInt(args[0]);

            Scanner scanner = new Scanner(System.in);
            int option = scanner.nextInt();

            if (option == 1) {
                go(option);
                for (; ; ) {
                    try {
                        addFiles(constant.LOCAL_DATA_DIR.split(";"));
                        Thread.sleep(20000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (option == 2) {
                try {
                    System.out.println(sdf.format(new Date()) + ": SuperPut start!!!");
                    addFiles(constant.LOCAL_DATA_DIR.split(";"));
                    num = blockingQueue.size();
                    go(option);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Parameter error, program will exit");
            }
        } catch (Exception e) {
            System.out.println("Parameter error, program will exit");
        }
    }
}
