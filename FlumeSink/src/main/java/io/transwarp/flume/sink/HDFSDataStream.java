package io.transwarp.flume.sink;

import com.google.common.annotations.VisibleForTesting;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.serialization.EventSerializer;
import org.apache.flume.serialization.EventSerializerFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HDFSDataStream extends AbstractHDFSWriter {

  private static final Logger logger =
      LoggerFactory.getLogger(HDFSDataStream.class);

  private FSDataOutputStream outStream;
  private String serializerType;
  private Context serializerContext;
  private EventSerializer serializer;
  private boolean useRawLocalFileSystem;

  @Override
  public void configure(Context context) {
    super.configure(context);

    serializerType = context.getString("serializer", "TEXT");
    useRawLocalFileSystem = context.getBoolean("hdfs.useRawLocalFileSystem",
        false);
    serializerContext =
        new Context(context.getSubProperties(EventSerializer.CTX_PREFIX));
    logger.info("Serializer = " + serializerType + ", UseRawLocalFileSystem = "
        + useRawLocalFileSystem);
  }

  @VisibleForTesting
  protected FileSystem getDfs(Configuration conf,
    Path dstPath) throws IOException{
    return  dstPath.getFileSystem(conf);
  }

  protected void doOpen(Configuration conf,
    Path dstPath, FileSystem hdfs) throws
    IOException {
    if(useRawLocalFileSystem) {
      if(hdfs instanceof LocalFileSystem) {
        hdfs = ((LocalFileSystem)hdfs).getRaw();
      } else {
        logger.warn("useRawLocalFileSystem is set to true but file system " +
            "is not of type LocalFileSystem: " + hdfs.getClass().getName());
      }
    }

    boolean appending = false;
    if (conf.getBoolean("hdfs.append.support", false) == true && hdfs.isFile
            (dstPath)) {
      outStream = hdfs.append(dstPath);
      appending = true;
    } else {
      outStream = hdfs.create(dstPath);
    }

    serializer = EventSerializerFactory.getInstance(
        serializerType, serializerContext, outStream);
    if (appending && !serializer.supportsReopen()) {
      outStream.close();
      serializer = null;
      throw new IOException("serializer (" + serializerType +
          ") does not support append");
    }

    // must call superclass to check for replication issues
    registerCurrentStream(outStream, hdfs, dstPath);

    if (appending) {
      serializer.afterReopen();
    } else {
      serializer.afterCreate();
    }
  }

  @Override
  public void open(String filePath) throws IOException {
    Configuration conf = new Configuration();
    Path dstPath = new Path(filePath);
    FileSystem hdfs = getDfs(conf, dstPath);
    doOpen(conf, dstPath, hdfs);
  }

  @Override
  public void open(String filePath, CompressionCodec codec,
                   CompressionType cType) throws IOException {
    open(filePath);
  }

  @Override
  public void append(Event e) throws IOException {
    serializer.write(e);
  }

  @Override
  public void sync() throws IOException {
    serializer.flush();
    outStream.flush();
    hflushOrSync(outStream);
  }

  @Override
  public void close() throws IOException {
    serializer.flush();
    serializer.beforeClose();
    outStream.flush();
    hflushOrSync(outStream);
    outStream.close();

    unregisterCurrentStream();
  }

}
