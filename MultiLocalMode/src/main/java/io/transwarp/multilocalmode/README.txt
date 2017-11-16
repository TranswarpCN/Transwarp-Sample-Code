1、需要修改的配置都在/resources/setup.xml中,本例演示Hyperbase外表的高并发查询，执行SampleCode.java，可多线程执行查询

##########################################################################
##########################################################################

以下是配置Inceptor的Local Mode：
在InceptorServer2 下配置local 模式。 测试集群baogang(172.16.1.86~172.16.1.89)
前提： 机器上要通过yum安装好hive、ngmr两组包，并保证在/usr/lib/hive/lib下面有正确的mysql jar包：mysql-connector-java.jar

二、配置方法
（以下拷贝的文件源均取自本集群Cluster模式的InceptorServer的对应文件，均带inceptor1文件标示）
1、拷贝service脚本
cp /etc/init.d/hive-server-inceptorsql1 /etc/init.d/hive-server-inceptorsql2

2、修改hive-server-inceptor2文件
vim /etc/init.d/hive-server-inceptorsql2
2.1 把其中带有inceptor1标示的目录改成inceptorsql2
============================
CONF_DIR="/etc/inceptorsql1/conf"
PID_FILE="/var/run/inceptorsq1/${NAME}${SUFFIX}.pid"
LOCKFILE="/var/lock/subsys/inceptorsql1/${NAME}${SUFFIX}"
LOG_FILE="/var/log/inceptorsql1/${NAME}${SUFFIX}.log"
=========>>
CONF_DIR="/etc/inceptorsql2/conf"
PID_FILE="/var/run/inceptorsq2/${NAME}${SUFFIX}.pid"
LOCKFILE="/var/lock/subsys/inceptorsql2/${NAME}${SUFFIX}"
LOG_FILE="/var/log/inceptorsql2/${NAME}${SUFFIX}.log"
============================
PS： 这里不需要修改PORT这个参数，因为这个参数在hiveServer2下不起作用

3、拷贝配置文件目录(CONF_DIR)
cp -rf /etc/inceptorsql1/ /etc/inceptorsql2

4、
修改 /etc/inceptorsql2/conf/ngmr-context-env.sh
把 INCEPTOR_UI_PORT 从4040 改成其他端口号，例如： 4041
修改/etc/inceptorsql2/conf/hive-site.xml，在最后面添加：
<property>
    <name>ngmr.exec.mode</name>
    <value>local</value>
  </property>
    <property>
        <name>hive.server2.thrift.port</name>
        <value>10001</value>
    </property>

5、创建存放pid的目录(PID_FILE)并赋775权限、hive:hadoop的用户组
mkdir /var/run/inceptorsql2
chmod 775 /var/run/inceptorsql2
chown hive:hadoop /var/run/inceptorsql2

6、拷贝存放lock文件的目录(LOCKFILE)
mkdir /var/lock/subsys/inceptorsql2
7、拷贝存放日志的目录(LOG_FILE)并赋775权限、hive:hadoop的用户组
mkdir /var/log/inceptorsql2
chmod 775 /var/log/inceptorsql2
chown hive:hadoop /var/log/inceptorsql2
８、创建local dir和 ramdisk dir
打开你的 /etc/inceptorsql2/conf/ngmr-env.sh
里面的两个参数 NGMR_LOCALDIR 和 NGMR_FASTDISK_DIR 分别配置了local dir 和 ramdisk dir
创建对应目录并赋值权限 777
mkdir /mnt/disk1/hadoop/ngmr/inceptorsql2
chmod 777 /mnt/disk1/hadoop/ngmr/inceptorsql2
mkdir /mnt/disk2/hadoop/ngmr/inceptorsql2
chmod 777 /mnt/disk2/hadoop/ngmr/inceptorsql2

9、启动Local模式的InceptorServer(hive-server-inceptorsql2)
service hive-server-inceptorsql2 start

10、指定我们之前自定义的端口号(10001)来连接Local模式的InceptorServer，配置完成
0: jdbc:hive2://baogang3:10001/default>
PS：不要忘了运行之前运行kinit 进行身份验证哦