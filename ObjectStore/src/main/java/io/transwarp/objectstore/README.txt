本例功能是通过Hyperbase的LOB Index进行对象存储
0、在自己的/etc/hosts(linux)  ||C:\Windows\System32\drivers\etc(windows)下写上集群的ip和hostname
1、从集群上拿core-site.xml,hbase-site.xml,hdfs-site.xml,hbase.keytab替换resources目录中的文件
2、修改setup.xml中相关配置，此外从集群上拿krb5.conf，在Windows上放到C盘Windows目录，并改名krb5.ini
3、UploadData.java，可以多线程讲文件存储在Hyperbase中，注意这些文件在上传前都变成英文文件名，同时文件名不要重复
4、Download.java，可以将存入Hyperbase中的文件下载