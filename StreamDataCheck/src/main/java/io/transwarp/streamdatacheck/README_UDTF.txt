kafka+StreamSQL+Hyperbase的实时数据比对

适用场景：实时过滤黑名单，避免传统的join，通过编写Hyperbase的API的UDTF来过滤

1、创建一个Hyperbase对比表 //之所以用hbase表是由于调用hbase api的查询效率相比jdbc更高
hbase shell
create 'bi', 'ps', 'ct', 'bl'
插入数据
put 'bi', '0001', 'ps:nm', 'Zhang San'
put 'bi', '0001', 'ps:pw', '1234'
put 'bi', '0001', 'ct:em', 'zs@mail.com'
put 'bi', '0001', 'ct:cp', '12345678912'
put 'bi', '0001', 'bl:bl', '10000.00'
put 'bi', '0002', 'ps:nm', 'Li Si'
put 'bi', '0002', 'ps:pw', '2468'
put 'bi', '0002', 'ct:em', 'ls@school.edu'
put 'bi', '0002', 'ct:cp', '13513572468'
put 'bi', '0002', 'bl:bl', '1000.00'
put 'bi', '0003', 'ps:nm', 'Wang Wu'
put 'bi', '0003', 'ps:pw', '1357'
put 'bi', '0003', 'ct:em', 'ww@hmail.com'
put 'bi', '0003', 'ct:cp', '13612345678'
put 'bi', '0003', 'bl:bl', '500.00'
put 'bi', '0001', 'ps:pw', '5678'
put 'bi', '0002', 'ct:em', 'ls@sample.com'
put 'bi', '0002', 'bl:bl', 56000
查看数据
scan 'bi'
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/usr/lib/hbase/lib/slf4j-log4j12-1.6.4.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/usr/lib/hadoop/lib/slf4j-log4j12-1.7.5.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
ROW                               COLUMN+CELL
 0001                             column=bl:bl, timestamp=1456010668891, value=10000.00
 0001                             column=ct:cp, timestamp=1456010668864, value=12345678912
 0001                             column=ct:em, timestamp=1456010668836, value=zs@mail.com
 0001                             column=ps:nm, timestamp=1456010668614, value=Zhang San
 0001                             column=ps:pw, timestamp=1456010669235, value=5678
 0002                             column=bl:bl, timestamp=1456010669993, value=56000
 0002                             column=ct:cp, timestamp=1456010669011, value=13513572468
 0002                             column=ct:em, timestamp=1456010669259, value=ls@sample.com
 0002                             column=ps:nm, timestamp=1456010668918, value=Li Si
 0002                             column=ps:pw, timestamp=1456010668950, value=2468
 0003                             column=bl:bl, timestamp=1456010669209, value=500.00
 0003                             column=ct:cp, timestamp=1456010669183, value=13612345678
 0003                             column=ct:em, timestamp=1456010669155, value=ww@hmail.com
 0003                             column=ps:nm, timestamp=1456010669073, value=Wang Wu
 0003                             column=ps:pw, timestamp=1456010669107, value=1357
3 row(s) in 0.2850 seconds

2、编写UDTF，详见udtfCheck.java
注意修改UDTF中hbase的表名
注意udtfCheckKerberos中53行，每个节点都要放从TDH Manager下载的hbase.keytab

3、本工程打成jar包，上传至集群

4、进入inceptor
add jar /mnt/disk1/streamDataCheck.jar;
create temporary function udtfcheck as 'io.transwarp.streamdatacheck.udtfCheck';
开启kerberos使用：
create temporary function udtfcheck as 'io.transwarp.streamdatacheck.udtfCheckKerberos'

5、一般udtf写死了针对的是哪个表的某些字段，所以如果想针对其他表，需要重新改写自己udtf

7、创建结果表
CREATE TABLE bi_result_2(
id STRING,
name string)
STORED AS ORC;

8、创建kafka topic
/usr/lib/kafka/bin/kafka-create-topic.sh --zookeeper 172.16.2.95:2181 --broker 172.16.2.95:9092 --topic bi_result_2

9、创建stream
CREATE STREAM source_2(
value STRING)
row format delimited fields terminated by '^'
tblproperties("topic"="bi_result_2","kafka.zookeeper"="172.16.2.95:2181","kafka.broker.list"="172.16.2.95:9092");

10、启动流
INSERT INTO bi_result_2
SELECT udtfcheck(value) as (id,name) FROM source_2;

11、kafka添加数据
cd /usr/lib/kafka
./bin/kafka-console-producer.sh --broker-list 172.16.2.95:9092 --topic bi_result
0001,Ann Aike
0002,Bob Blues
0005,Dick Eike

12、查看结果
0: jdbc:hive2://transwarp-4:10000/default> select * from bi_result_2;
+-------+-------+
|  id   | name  |
+-------+-------+
| 0005  | Dick  |
| 0005  | Eike  |
+-------+-------+
2 rows selected (3.115 seconds)
由于0001，0002在hbase检索的到，所以认为触发比对条件，不进行插入，结果正确