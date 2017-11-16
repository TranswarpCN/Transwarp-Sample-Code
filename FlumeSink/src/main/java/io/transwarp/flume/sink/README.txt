将工程打jar包，上传至集群/usr/lib/flume/lib目录下
io.transwarp.demo.flume.sink.sink.HDFSSink.java实现了以下flume sink的功能：
1、假设本地文件目录是/x/y/z.txt，通过flume读取后，能够位于如下的agent1.sinks.hdfssink1.hdfs.path/x/y/z.txt
2、集成开源flume的hdfs sink的所有功能

以下sink.conf内容：

agent1.sources = source1
agent1.channels = ch1
agent1.sinks = hdfssink1

agent1.channels.ch1.type = memory
agent1.channels.ch1.capacity = 100000
agent1.channels.ch1.transactionCapacity = 100000

agent1.sources.source1.type = spooldir
agent1.sources.source1.spoolDir = /tmp/tmp/child
agent1.sources.source1.fileHeader = true
agent1.sources.source1.deserializer.outputCharset = UTF-8
agent1.sources.source1.channels = ch1
agent1.sources.source1.deserializer.maxLineLength = 20480

agent1.sinks.hdfssink1.channel = ch1
agent1.sinks.hdfssink1.type = io.transwarp.flume.sink.HDFSSink
agent1.sinks.hdfssink1.hdfs.path = hdfs://172.16.2.95:8020/tmp/flume/tmp/
agent1.sinks.hdfssink1.hdfs.filePrefix = logs_%H-%M-%S
agent1.sinks.hdfssink1.hdfs.useLocalTimeStamp = true
agent1.sinks.hdfssink1.hdfs.writeFormat = Text
agent1.sinks.hdfssink1.hdfs.fileType = DataStream
agent1.sinks.hdfssink1.hdfs.rollInterval = 0
agent1.sinks.hdfssink1.hdfs.rollSize = 6
agent1.sinks.hdfssink1.hdfs.rollCount = 0
agent1.sinks.hdfssink1.hdfs.batchSize = 2
agent1.sinks.hdfssink1.hdfs.idleTimeout=60
agent1.sinks.hdfssink1.hdfs.round = true
agent1.sinks.hdfssink1.hdfs.roundValue = 1
agent1.sinks.hdfssink1.hdfs.roundUnit = hour

在/usr/lib/flume下运行
./bin/flume-ng  agent --conf ./conf/ -f ./conf/sink.conf -n agent1 -Dflume.root.logger=INFO,console

在kerberos模式下，kinit hdfs后，如同简单模式使用即可