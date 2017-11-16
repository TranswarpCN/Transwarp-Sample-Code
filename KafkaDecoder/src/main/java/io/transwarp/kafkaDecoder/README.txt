未启用kerberos
/usr/lib/kafka/bin/kafka-create-topic.sh --broker transwarp-9:9092
--zookeeper transwarp-9:2181 --partition 1 --topic input

5.0
./kafka-topics.sh --create --zookeeper 172.16.2.61:2181,172.16.2.62:2181,172.16.2.64:2181  --replication-factor 3 --partition 1 --topic kafka1st




启用kerberos
kinit kafka
/usr/lib/kafka/bin/kafka-grant-permission.sh RWCD
--zookeeper transwarp-9:2181 --user hive --principal kafka --key /mnt/disk1/kafka.keytab
/usr/lib/kafka/bin/kafka-create-topic.sh --broker transwarp-9:9092
--zookeeper transwarp-9:2181 --principal hive --keytab /mnt/disk1/hive.keytab --partition 1 --topic input
注意kafka.keytab,hive.keytab从TDH Manager上下载

进入Inceptor，如开kerberos，执行set role admin;

add jar /mnt/disk1/kafkadecoder.jar;

5.0
add jar hdfs://nameservice1/tmp/tanliming/kafkaDecoder.jar



创建使用自定义Decoder的流表
drop stream if exists input_stream;
create stream input_stream (
a int,b int,c int,d int,
type1 string,type2 string,
status string,in_date int,
in_time int,status_value int,
ftime string)
row format delimited fields terminated by ',' tblproperties(
"topic" = "kafka1st",
--"transwarp.stream.kafka.keytab"="/mnt/disk1/hive.keytab", // 开启kerberos需配置
--"transwarp.stream.kafka.principal"="hive", // 开启kerberos需配置
"kafka.zookeeper" = "172.16.2.61:2181",
"kafka.broker.list" = "172.16.2.61:9092",
"kafka.decoder" = "io.transwarp.kafkadecoder.timeformatdecoder");

创建表保存统计结果
drop table if exists output_table;
create table output_table (
a int,b int,c int,d int,
type1 string,type2 string,
status string,in_date int,
in_time int,status_value int,
ftime string);

/kafka里的测试数据,倒数第三列为状态字段，倒数第二为日期字段，倒数第一为时间字段
/usr/lib/kafka/bin/kafka-console-producer.sh --broker-list transwarp-9:9092 --principal hive
--keytab /mnt/disk1/hive.keytab --topic input

1,1,1,1,t1,t2,A,1451577600,0
1,1,1,1,t1,t2,B,1451577600,1
1,1,1,1,t1,t2,A,1451577600,2
1,1,1,1,t1,t2,B,1451577600,3
1,1,1,1,t1,t2,A,1451577600,4
1,1,1,1,t1,t2,B,1451577600,5
1,1,1,1,t1,t2,A,1451577600,6

启动流任务
insert into output_table select * from input_stream;

select * from output_table;