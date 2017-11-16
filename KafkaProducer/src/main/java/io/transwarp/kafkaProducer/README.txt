1、修改/resources/setup.xml

2、在集群上/usr/lib/kafka/bin下运行
4.7及其以下
./kafka-create-topic.sh --zookeeper 172.16.2.93:2181 --partition 3 --topic kafka1st
--broker 172.16.2.93 172.16.2.94 172.16.2.95

4.8&5.0
./kafka-topics.sh --create --zookeeper 172.16.2.61:2181,172.16.2.62:2181,172.16.2.64:2181  --replication-factor 3 --partition 1 --topic kafka1st
注意topic、ip的替换

如果开启了kerberos，在终端 kinit kafka
./kafka-create-topic.sh --broker transwarp-8:9092 transwarp-7:9092 transwarp-9:9092
--zookeeper transwarp-9:2181 --principal kafka --keytab /mnt/disk1/kafka.keytab --partition 3 --topic kafka1st
注意keytab得去TDH Manager上下载kafka用户的keytab，然后分别放在Linux和Windows上

3、运行SampleCode.java的main函数



