package io.transwarp.flumesource;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileSource extends AbstractSource implements Configurable,EventDrivenSource{
    // 输出日志
    private static final Logger logger = LoggerFactory.getLogger(LocalFileSource.class);
    // 本地文件路径
    private String localPath;
    // 任务监控执行线程
    private ScheduledExecutorService monitor;
    // 重复扫描次数
    private int DELAY;
    // 上次扫描的时间
    private long lastCheckTime = 0;
    // 是否递归查询文件 默认是不递归查找
    private boolean isRecurse = false;
    // 每个批次文件的文件数量，目前还没有实现
    private int batchSize = 100;
    // 忽略的文件格式，将不上传
    private String ignorePatternStr;
    // 忽略的文件格式，将不上传
    private Pattern ignorePattern;
    // 文件后缀名 例如 .complete
    private String fileSuffix;

    // 实现Configurable的configure方法，获取配置参数
    public void configure(Context context) {
        this.localPath = context.getString("local.path");
        this.DELAY = Integer.parseInt(context.getInteger("delay", 10) + "");
        this.isRecurse = context.getBoolean("recurse", false);
        String lastModifyTime = context.getString("lastModifyTime");
        //this.batchSize = context.getInteger("batchSize", 100);
        this.ignorePatternStr = context.getString("ignorePattern", "^$");
        this.ignorePattern = Pattern.compile(ignorePatternStr);
        this.fileSuffix=context.getString("fileSuffix",Constants.SUFFIX);

        if (lastModifyTime == null || lastModifyTime.equals("")) {
            lastCheckTime = 0L;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
            try {
                Date date = sdf.parse(lastModifyTime);
                lastCheckTime = date.getTime();
            } catch (ParseException e) {
                logger.error(
                        "lastModifyTime's format should be yyyyMMddHHmm, your value is "
                                + lastModifyTime, e);
                System.exit(-1);
            }
        }

    }

    /*
     * 启动source 启动任务调度
     * 重写AbstractSource 的start方法
     */
    @Override
    public synchronized void start() {
        String formattedName = "local-dir-monitor-%d";
        monitor = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                        .setNameFormat(formattedName).build());
        DirMonitor runner = new DirMonitor();
        monitor.scheduleAtFixedRate(runner, 0, DELAY, TimeUnit.SECONDS);
        super.start();
    }

    /*
     * 停止source 停止任务调度
     * 重写AbstractSource 的stop方法
     */
    @Override
    public synchronized void stop() {
        monitor.shutdown();
        try {
            monitor.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            logger.info("Interrupted while awaiting termination", ex);
        }
        monitor.shutdownNow();

        super.stop();
        logger.info("STOP LocalFileSource");
    }

    private class DirMonitor implements Runnable {
        private List<File> findFile(File path, boolean isRecurse) {
            // 递归查找文件
            List<File> files = new ArrayList<File>();
            if (path.isDirectory()) {
                File[] candidates = path.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if(name.endsWith(".swp")||name.endsWith(fileSuffix)||ignorePattern.matcher(name).matches()){
                            return false;
                        }
                        return true;
                    }
                });
                for (int i = 0; i < candidates.length; i++) {
                    if (candidates[i].isDirectory() && isRecurse) {
                        files.addAll(findFile(candidates[i], isRecurse));
                    } else {
                        files.add(candidates[i]);
                    }
                }
            }
            return files;
        }

        public void run() {
            long currenTime = System.currentTimeMillis();
            List<File> needFiles = findFile(new File(localPath), isRecurse);
            List<Event> events = new ArrayList<Event>();
            for (Iterator<File> iterator = needFiles.iterator(); iterator
                    .hasNext();) {
                File file = (File) iterator.next();
                if (file.isFile()) {
                    String complete = file.getAbsolutePath() + fileSuffix;
                    file.renameTo(new File(complete));
                    Event event = new SimpleEvent();
                    event.getHeaders().put(Constants.FILE_PATH_KEY, complete);
                    event.getHeaders().put(Constants.LOCATION_KEY,
                            Constants.LOCAL_TYPE_VALUE);
                    event.getHeaders().put(Constants.ROOT_PATH_KEY, localPath);
                    events.add(event);
                }
            }

            for (Iterator<Event> iterator = events.iterator(); iterator
                    .hasNext();) {
                Event event = (Event) iterator.next();
                try {
                    getChannelProcessor().processEvent(event);
                } catch (ChannelException e) {
                    logger.error(e.getMessage(), e);
                    // TODO: 如果写事件失败，把文件名重命名为原始文件名，使得下个周期能检测到该文件继续上传该文件
                    String fileName = event.getHeaders().get(
                            Constants.FILE_PATH_KEY);
                    String oriName = fileName.substring(0, fileName.length()
                            - fileSuffix.length());
                    new File(fileName).renameTo(new File(oriName));
                }
            }
            lastCheckTime = currenTime;
        }
    }
}
