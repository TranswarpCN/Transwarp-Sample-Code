package io.transwarp.flume.sink;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;

import java.util.Collections;

public class HDFSWritableSerializer implements SequenceFileSerializer {

  private BytesWritable makeByteWritable(Event e) {
    BytesWritable bytesObject = new BytesWritable();
    bytesObject.set(e.getBody(), 0, e.getBody().length);
    return bytesObject;
  }

  @Override
  public Class<LongWritable> getKeyClass() {
    return LongWritable.class;
  }

  @Override
  public Class<BytesWritable> getValueClass() {
    return BytesWritable.class;
  }

  @Override
  public Iterable<Record> serialize(Event e) {
    Object key = getKey(e);
    Object value = getValue(e);
    return Collections.singletonList(new Record(key, value));
  }

  private Object getKey(Event e) {
    String timestamp = e.getHeaders().get("timestamp");
    long eventStamp;

    if (timestamp == null) {
      eventStamp = System.currentTimeMillis();
    } else {
      eventStamp = Long.valueOf(timestamp);
    }
    return new LongWritable(eventStamp);
  }

  private Object getValue(Event e) {
    return makeByteWritable(e);
  }

  public static class Builder implements SequenceFileSerializer.Builder {

    @Override
    public SequenceFileSerializer build(Context context) {
      return new HDFSWritableSerializer();
    }

  }

}
