将工程打jar包，上传至集群/usr/lib/flume/lib目录下
io.transwarp.demo.flume.interceptor.SearchAndReplaceAndFilterInterceptor$Builder.java实现了以下flume interceptor的功能：
1、对某个文件，先按正则匹配进行行的过滤，再按列下标进行列的过滤，最后按正则匹配做替换
2、各参数：
filterByRow，是否进行行过滤
regex，行过滤正则匹配项
filterByCol，是否进行列过滤
colSeparator，列分隔符
index，保留的列的下标，从1开始
searchAndReplace，是否进行替换
searchPattern，需要被替换的项，通过正则匹配
replaceString，被替换成的字符
charset，字符编码

以下interceptor.conf内容：

agent1.sources = source1
agent1.channels = ch1
agent1.sinks = hdfssink1

agent1.channels.ch1.type = memory
agent1.channels.ch1.capacity = 100000
agent1.channels.ch1.transactionCapacity = 100000

agent1.sources.source1.type = spooldir
agent1.sources.source1.spoolDir = /tmp/tmp/sss
agent1.sources.source1.fileHeader = true
agent1.sources.source1.deserializer.outputCharset = UTF-8
agent1.sources.source1.channels = ch1
agent1.sources.source1.deserializer.maxLineLength = 20480

agent1.sources.source1.interceptors = i1
agent1.sources.source1.interceptors.i1.type = io.transwarp.flume.interceptor.SearchAndReplaceAndFilterInterceptor$Builder
agent1.sources.source1.interceptors.i1.filterByRow = true
agent1.sources.source1.interceptors.i1.regex = ^filterByRow*
agent1.sources.source1.interceptors.i1.filterByCol = true
agent1.sources.source1.interceptors.i1.colSeparator = \\t
agent1.sources.source1.interceptors.i1.index = 2,3
agent1.sources.source1.interceptors.i1.searchAndReplace = true
agent1.sources.source1.interceptors.i1.searchPattern = [0-9]+
agent1.sources.source1.interceptors.i1.replaceString = TMP
agent1.sources.source1.interceptors.i1.charset = UTF-8

agent1.sinks.hdfssink1.channel = ch1
agent1.sinks.hdfssink1.type = hdfs
agent1.sinks.hdfssink1.hdfs.path = hdfs://172.16.2.61:8020/tmp/flume/
agent1.sinks.hdfssink1.hdfs.filePrefix = logs_%H-%M-%S
agent1.sinks.hdfssink1.hdfs.useLocalTimeStamp = true
agent1.sinks.hdfssink1.hdfs.writeFormat = Text
agent1.sinks.hdfssink1.hdfs.fileType = DataStream
agent1.sinks.hdfssink1.hdfs.rollInterval = 0
agent1.sinks.hdfssink1.hdfs.rollSize = 6
agent1.sinks.hdfssink1.hdfs.rollCount = 0

在/usr/lib/flume下运行
./bin/flume-ng  agent --conf ./conf/ -f ./conf/inceptor.conf -n agent1 -Dflume.root.logger=INFO,console

在kerberos模式下，kinit hdfs后，如同简单模式使用即可