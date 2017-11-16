package io.transwarp.flume.sink;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import java.util.Collections;

public class HDFSTextSerializer implements SequenceFileSerializer {

  private Text makeText(Event e) {
    Text textObject = new Text();
    textObject.set(e.getBody(), 0, e.getBody().length);
    return textObject;
  }

  @Override
  public Class<LongWritable> getKeyClass() {
    return LongWritable.class;
  }

  @Override
  public Class<Text> getValueClass() {
    return Text.class;
  }

  @Override
  public Iterable<Record> serialize(Event e) {
    Object key = getKey(e);
    Object value = getValue(e);
    return Collections.singletonList(new Record(key, value));
  }

  private Object getKey(Event e) {
    // Write the data to HDFS
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
    return makeText(e);
  }

  public static class Builder implements SequenceFileSerializer.Builder {

    @Override
    public SequenceFileSerializer build(Context context) {
      return new HDFSTextSerializer();
    }

  }

}
