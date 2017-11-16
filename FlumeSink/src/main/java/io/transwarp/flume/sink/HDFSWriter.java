package io.transwarp.flume.sink;

import org.apache.flume.Event;
import org.apache.flume.annotations.InterfaceAudience;
import org.apache.flume.annotations.InterfaceStability;
import org.apache.flume.conf.Configurable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.compress.CompressionCodec;

import java.io.IOException;

@InterfaceAudience.Private
@InterfaceStability.Evolving
public interface HDFSWriter extends Configurable {

  public void open(String filePath) throws IOException;

  public void open(String filePath, CompressionCodec codec,
                   CompressionType cType) throws IOException;

  public void append(Event e) throws IOException;

  public void sync() throws IOException;

  public void close() throws IOException;

  public boolean isUnderReplicated();

}
