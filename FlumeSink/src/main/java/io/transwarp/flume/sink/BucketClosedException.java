package io.transwarp.flume.sink;

import org.apache.flume.FlumeException;

public class BucketClosedException extends FlumeException{

  private static final long serialVersionUID = -4216667125119540357L;

  public BucketClosedException(String msg) {
    super(msg);
  }
}
