jar包所在位置一定要确保hive有读写的权限，否则会一直找不到jar包的，推荐位置在inceptorserver的tmp目录，这个目录是
linux的本地目录，不是hdfs的目录
在执行一下命令的时确保已经进入到inceptor的命令行操作界面.

下面的操作是创建一个临时函数，关于创建持久化的函数，请参考在inceptor中创建持久化函数
hive> add jar jarName.jar;
hive> create temporary function test_group as 'io.transwarp.inceptorudaf.InceptorUDAF';
hive> select test_group(field3,field4,field5,field6) from t;
hive> drop temporary function test_group;
hive> quit;