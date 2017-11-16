本例功能是通过HDFS的API的相关操作，从集群上拿下core-site.xml,hdfs-site.xml,yarn-site.xml,hdfs.keytab

superput的配置在setup.xml中，运行SuperPut.java，
提示输入参数，1代表SuperPut为常驻进程（代表会一直监控目录），2代表SuperPut为普通进程（目录内文件上传完即程序结束）
通过SuperPut上传完的文件，前缀名会增加 COMPLETED_
此外，SuperPut不支持扫描子目录文件