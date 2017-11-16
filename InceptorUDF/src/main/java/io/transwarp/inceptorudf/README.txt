jar包所在位置一定要确保hive有读写的权限，否则会一直找不到jar包的，推荐位置在inceptorserver的tmp目录，这个目录是
linux的本地目录，不是hdfs的目录
在执行一下命令的时确保已经进入到inceptor的命令行操作界面.

下面的操作是创建一个临时函数，关于创建持久化的函数，请参考在inceptor中创建持久化函数

hive> add jar jarName.jar;
---备注：5.0推荐走hdfs路径,put 到hdfs上后add jar hdfs://nameservice1/<your hdfs path>
hive> create temporary function ip2num as 'io.transwarp.inceptorudf.InceptorUDF';
hive> select ip2num(t.col1) from t limit 10;
hive> drop temporary function ip2num;