package io.transwarp.flume.sink;

import org.apache.flume.Context;
import org.apache.flume.Event;

public interface SequenceFileSerializer {

  Class<?> getKeyClass();

  Class<?> getValueClass();

  /**
   * Format the given event into zero, one or more SequenceFile records
   *
   * @param e
   *         event
   * @return a list of records corresponding to the given event
   */
  Iterable<Record> serialize(Event e);

  /**
   * Knows how to construct this output formatter.<br/>
   * <b>Note: Implementations MUST provide a public a no-arg constructor.</b>
   */
  public interface Builder {
    public SequenceFileSerializer build(Context context);
  }

  /**
   * A key-value pair making up a record in an HDFS SequenceFile
   */
  public static class Record {
    private final Object key;
    private final Object value;

    public Record(Object key, Object value) {
      this.key = key;
      this.value = value;
    }

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }
  }

}
