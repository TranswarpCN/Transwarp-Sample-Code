1、修改/resources/setup.xml

2、在集群上/usr/lib/kafka/bin下运行

4.7版本及其以下
./kafka-create-topic.sh --zookeeper 172.16.2.93:2181 172.16.2.94:2181 172.16.2.95:2181
--partition 6 --topic kafka4 --broker 172.16.2.93 172.16.2.94 172.16.2.95
注意topic、ip的替换

5.0
./kafka-topics.sh --create --topic kafak1st --zookeeper 172.16.140.24:2181,172.16.140.25:2181,172.16.140.26:2181 --replication-factor 3 --partitions 1
注意topic、ip的替换


如果开启了kerberos，在终端 kinit kafka
./kafka-create-topic.sh --zookeeper transwarp-4:2181 transwarp-5:2181 transwarp-6:2181
--partition 6 --topic kafka4 --broker transwarp-4 transwarp-5 transwarp-6
注意keytab得去TDH Manager上下载kafka用户的keytab，然后分别放在Linux和Windows上

3、运行SampleCode.java的main函数

