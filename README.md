# Transwarp Sample Code

This repository contains the example projects for componetnts on [Transwarp Data Hub](http://www.transwarp.cn/product/tdh).

![](./png/tdh.png)

* Transwarp's Distribution for Apache Hadoop for Enterprise has a five-layer architecture. Different applications provide customized support via flexible combination and efficient coordination among components.

* Transwarp Inceptor in-memory analysis engine provides high-speed interactive SQL queries.

* Transwarp Hyperbase real-time data processing engine, based on Apache HBase, is optimal for highly concurrent online business systems for enterprises.

* Transwarp Stream real-time streaming processing engine, based on Apache Spark, possesses powerful stream processing abilities.

* Transwarp Discover machine learning engine provides data mining via R.

## Project List

You can read the [user-guide.txt](https://github.com/Transwarp-DE/Transwarp-Sample-Code/blob/master/user-guide.txt) for how to use the projects with a quick view.

**BatchInsert.**  Example for batch insert data by Inceptor JDBC. 

- [x] Kerberos Support

**ComplexSearch.** Example for fulltext search by Inceptor JDBC (index builded by Elasticsearch). 

- [x] Kerberos Support

**Elasticsearch.** Example for usage of Elasticsearch Java API. 

- [ ] Kerberos Support

**FlumeInterceptor.** Example for Flume Interceptor, contanins row and column (regex) filter. 

- [x] Kerberos Support

**FlumeSink.** Example for Flume Sink, synchronising folders and files. 

- [x] Kerberos Support

**FlumeSource.** Example for Flume Source. 

- [x] Kerberos Support

**Hibernate.** Example for Inceptor work with Hibernate 3.X. 

- [x] Kerberos Support

**Hibernate4.** Example for Inceptor work with Hibernate 4.X. 

- [x] Kerberos Support

**Hibernate5.** Example for Inceptor work with Hibernate 5.X. 

- [x] Kerberos Support

**InceptorUDAF.** Example for Inceptor UDAF. 

- [x] Kerberos Support

**InceptorUDF.** Example for Inceptor UDF. 

- [x] Kerberos Support

**InceptorUDTF.** Example for Inceptor UDTF. 

- [x] Kerberos Support

**KafkaConsumer.** Example for Kafka Consumer with multi-threading. 

- [x] Kerberos Support

**KafkaDecoder.** Example for Kafka Decoder. 

- [x] Kerberos Support

**KafkaProducer.** Example for Kafka Producer with multi-threading. 

- [x] Kerberos Support

**KeywordSearch.** Example for keyword search by Inceptor JDBC (index builded by Elasticsearch), and contains PDF/PPT/Word/Excel to txt convert function. 

- [x] Kerberos Support

**MultiLocalMode.** Example for multi-threading query with Hyperbase external table by Inceptor JDBC. 

- [x] Kerberos Support

**ObjectStore.** Example for storing non-text (such as image, video, etc) files in Hyperbase with with multi-threading. 

- [x] Kerberos Support

**StreamDataCheck.** Example for black-list filter with StreamSQL UDF/UDTF. 

- [x] Kerberos Support

**WebHDFS.** Example for Web HDFS Java API. 

- [x] Kerberos Support

**iBatis.** Example for Inceptor work with iBatis. 

- [x] Kerberos Support

**rJava.** Example for Spark Scala API. 

- [x] Kerberos Support

## Building and Running

To build the code, you will first need to have installed Maven and Java. Then import projects to your favourite IDE (If you use IntelliJ, every project isn't a IntelliJ Maven project's module, the whole project isn't IntelliJ project either. So you need import every project as existing module to your IntelliJ one by one). 

The Transwarp Data Hub Maven repository, you can download [here](http://support.transwarp.cn/t/sdk-maven-tdh-repository/546).

## Support

* You can post bug reports and feature requests at the [Issue Page](https://github.com/Transwarp-DE/Transwarp-Sample-Code/issues)
