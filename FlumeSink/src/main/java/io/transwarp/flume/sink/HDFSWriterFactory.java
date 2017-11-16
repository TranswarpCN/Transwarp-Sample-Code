package io.transwarp.flume.sink;

import java.io.IOException;

public class HDFSWriterFactory {
  static final String SequenceFileType = "SequenceFile";
  static final String DataStreamType = "DataStream";
  static final String CompStreamType = "CompressedStream";

  public HDFSWriterFactory() {

  }

  public HDFSWriter getWriter(String fileType) throws IOException {
    if (fileType.equalsIgnoreCase(SequenceFileType)) {
      return new HDFSSequenceFile();
    } else if (fileType.equalsIgnoreCase(DataStreamType)) {
      return new HDFSDataStream();
    } else if (fileType.equalsIgnoreCase(CompStreamType)) {
      return new HDFSCompressedDataStream();
    } else {
      throw new IOException("File type " + fileType + " not supported");
    }
  }
}
