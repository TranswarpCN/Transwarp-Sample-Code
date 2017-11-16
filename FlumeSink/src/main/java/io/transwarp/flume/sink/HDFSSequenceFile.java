package io.transwarp.flume.sink;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HDFSSequenceFile extends AbstractHDFSWriter {

  private static final Logger logger =
      LoggerFactory.getLogger(HDFSSequenceFile.class);
  private SequenceFile.Writer writer;
  private String writeFormat;
  private Context serializerContext;
  private SequenceFileSerializer serializer;
  private boolean useRawLocalFileSystem;
  private FSDataOutputStream outStream = null;

  public HDFSSequenceFile() {
    writer = null;
  }

  @Override
  public void configure(Context context) {
    super.configure(context);

    // use binary writable serialize by default
    writeFormat = context.getString("hdfs.writeFormat",
      SequenceFileSerializerType.Writable.name());
    useRawLocalFileSystem = context.getBoolean("hdfs.useRawLocalFileSystem",
        false);
    serializerContext = new Context(
            context.getSubProperties(SequenceFileSerializerFactory.CTX_PREFIX));
    serializer = SequenceFileSerializerFactory
            .getSerializer(writeFormat, serializerContext);
    logger.info("writeFormat = " + writeFormat + ", UseRawLocalFileSystem = "
        + useRawLocalFileSystem);
  }

  @Override
  public void open(String filePath) throws IOException {
    open(filePath, null, CompressionType.NONE);
  }

  @Override
  public void open(String filePath, CompressionCodec codeC,
      CompressionType compType) throws IOException {
    Configuration conf = new Configuration();
    Path dstPath = new Path(filePath);
    FileSystem hdfs = dstPath.getFileSystem(conf);
    open(dstPath, codeC, compType, conf, hdfs);
  }

  protected void open(Path dstPath, CompressionCodec codeC,
      CompressionType compType, Configuration conf, FileSystem hdfs)
          throws IOException {
    if(useRawLocalFileSystem) {
      if(hdfs instanceof LocalFileSystem) {
        hdfs = ((LocalFileSystem)hdfs).getRaw();
      } else {
        logger.warn("useRawLocalFileSystem is set to true but file system " +
            "is not of type LocalFileSystem: " + hdfs.getClass().getName());
      }
    }
    if (conf.getBoolean("hdfs.append.support", false) == true && hdfs.isFile
            (dstPath)) {
      outStream = hdfs.append(dstPath);
    } else {
      outStream = hdfs.create(dstPath);
    }
    writer = SequenceFile.createWriter(conf, outStream,
        serializer.getKeyClass(), serializer.getValueClass(), compType, codeC);

    registerCurrentStream(outStream, hdfs, dstPath);
  }

  @Override
  public void append(Event e) throws IOException {
    for (SequenceFileSerializer.Record record : serializer.serialize(e)) {
      writer.append(record.getKey(), record.getValue());
    }
  }

  @Override
  public void sync() throws IOException {
    hflushOrSync(outStream);
  }

  @Override
  public void close() throws IOException {
    writer.close();
    outStream.close();
    unregisterCurrentStream();
  }
}
