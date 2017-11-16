package io.transwarp.flume.sink;

import com.google.common.base.Preconditions;
import org.apache.flume.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceFileSerializerFactory {

  private static final Logger logger =
      LoggerFactory.getLogger(SequenceFileSerializerFactory.class);

  /**
   * {@link Context} prefix
   */
  static final String CTX_PREFIX = "writeFormat.";

  @SuppressWarnings("unchecked")
  static SequenceFileSerializer getSerializer(String formatType,
                                              Context context) {

    Preconditions.checkNotNull(formatType,
        "serialize type must not be null");

    // try to find builder class in enum of known formatters
    SequenceFileSerializerType type;
    try {
      type = SequenceFileSerializerType.valueOf(formatType);
    } catch (IllegalArgumentException e) {
      logger.debug("Not in enum, loading builder class: {}", formatType);
      type = SequenceFileSerializerType.Other;
    }
    Class<? extends SequenceFileSerializer.Builder> builderClass =
        type.getBuilderClass();

    // handle the case where they have specified their own builder in the config
    if (builderClass == null) {
      try {
        Class c = Class.forName(formatType);
        if (c != null && SequenceFileSerializer.Builder.class.isAssignableFrom(c)) {
          builderClass = (Class<? extends SequenceFileSerializer.Builder>) c;
        } else {
          logger.error("Unable to instantiate Builder from {}", formatType);
          return null;
        }
      } catch (ClassNotFoundException ex) {
        logger.error("Class not found: " + formatType, ex);
        return null;
      } catch (ClassCastException ex) {
        logger.error("Class does not extend " +
            SequenceFileSerializer.Builder.class.getCanonicalName() + ": " +
            formatType, ex);
        return null;
      }
    }

    // build the builder
    SequenceFileSerializer.Builder builder;
    try {
      builder = builderClass.newInstance();
    } catch (InstantiationException ex) {
      logger.error("Cannot instantiate builder: " + formatType, ex);
      return null;
    } catch (IllegalAccessException ex) {
      logger.error("Cannot instantiate builder: " + formatType, ex);
      return null;
    }

    return builder.build(context);
  }

}
