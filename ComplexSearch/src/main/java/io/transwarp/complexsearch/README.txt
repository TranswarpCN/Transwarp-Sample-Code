1、修改/resource/setup.xml中配置，本例演示对Hyperbase+ES,ESdrive,Hyperdrive的综合搜索查询

2、执行以下前置条件，在命令行中
ES+Hyperbase的综合查询的前置条件：
# 建立HDFS外表
create external table o_test (
  path string,
  txt  string)
row format delimited fields terminated by '\t'
stored as textfile location '/tmp/train.csv';

create external table o_test_2 (
  path string,
  txt  string)
row format delimited fields terminated by '|'
STORED AS CSVFILE
location '/tmp/full2';
# 建立Hyperbase外表
create table tmp_o_test_3 (
key string,
path string,
txt string
) STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler' WITH SERDEPROPERTIES
("hbase.columns.mapping"=":key,
  c1:path,
  c1:txt")
TBLPROPERTIES ("hbase.table.name"="tmp_o_test_3");

# 将HDFS数据导入Hyperbase中
insert into tmp_o_test
select /*+USE_BULKLOAD*/ concat(substr(path,15,11),'-',substr(path,28,2),'-',uniq()) key,
path, txt from o_test order by key;

insert into tmp_o_test_3
select /*+USE_BULKLOAD*/ concat(substr(path,37,15),'-',uniq()) key,
path, txt from o_test_2 order by key;

# 在Hyperbase中，导出Json文件，并更改fulltextindex中相关配置
describeInJson 'tmp_o_test_3', 'true', '/tmp/p.txt'

{
  "tableName" : "tmp_o_test_3",
  "base" : {
    "families" : [ {
      "FAMILY" : "c1",
      "DATA_BLOCK_ENCODING" : "PREFIX",
      "BLOOMFILTER" : "ROW",
      "REPLICATION_SCOPE" : "0",
      "VERSIONS" : "1",
      "COMPRESSION" : "SNAPPY",
      "MIN_VERSIONS" : "0",
      "TTL" : "2147483647",
      "KEEP_DELETED_CELLS" : "FALSE",
      "BLOCKSIZE" : "65536",
      "IN_MEMORY" : "false",
      "BLOCKCACHE" : "true"
    } ],
    "THEMIS_ENABLE" : false
  },
  "fulltextindex" : {
    "tableName" : "tmp_o_test_3",
    "allowUpdate" : true,
    "ttl" : 63072000000,
    "source" : true,
    "all" : true,
    "storeAsSource" : false,
    "storeFamily" : "",
    "fields" : [ {
      "family" : "c1",
      "qualifier" : "path",
      "encode_as_string" : false,
      "attributes" : {
        "index" : "analyzed",
        "store" : "true",
        "doc_values" : "false",
        "type" : "string",
		"analyzer" : "ik",
		"search_analyzer" : "ik"
      }
    },{
      "family" : "c1",
      "qualifier" : "txt",
      "encode_as_string" : false,
      "attributes" : {
        "index" : "analyzed",
        "store" : "true",
        "doc_values" : "false",
        "type" : "string",
		"analyzer" : "ik",
		"search_analyzer" : "ik"
      }
    }],
    "settings" : {
      "index.number_of_replicas" : "0",
      "index.number_of_shards" : "12",
      "index.refresh_interval" : "30000"
    }
  }
  ,
  "globalindex" : {
    "indexs" : [ ]
  },
  "lob" : {
    "indexs" : [ ]
  },
  "localindex" : {
    "indexs" : [ ]
  }
}

# 将修改后的Json文件导入Hyperbase表中
alterUseJson 'tmp_o_test_3', '/tmp/p.txt'

# 此时，http://192.168.41.103:9200/_plugin/head/，可以看到ES图形化界面（任意以节点:9200/_plugin/head）
# 创建Hyperbase表的全文索引
rebuild_fulltext_index 'tmp_o_test_3'

# 进入Inceptor，开启local mode
set ngmr.exec.mode=local;

# 创建具备全文索引的Hyperbase外表
CREATE external TABLE ttmp_o_test
(
rowkey string,
path string,
txt string
)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key, c1:path, c1:txt")
TBLPROPERTIES ("hbase.table.name" = "tmp_o_test_3");

# 进行全文索引的查询

ESDrive的综合查询的前置条件
create table es_table_sql_test(
   key string,
   content string,
   tint int,
   tfloat float,
   tbool boolean)
stored as ES
with shard number 16
replication 4;

insert into table es_table_sql_test(key, content,tint, tfloat, tbool) values("1", "matches test", 1 ,1.1, true);
insert into table es_table_sql_test(key, content,tint, tfloat, tbool) values("3", "esdrive",3,3.3, true);
insert into table es_table_sql_test(key, content,tint, tfloat, tbool) values("4", "cond udf test",4,4.4, false);
insert into table es_table_sql_test(key, content,tint, tfloat, tbool) values("5", "contains test ", 5,5.5, false);
insert into table es_table_sql_test(key, content,tint, tfloat, tbool) values("6", "test", 6,6.6, true);
insert into table es_table_sql_test(key, content,tint, tfloat, tbool) values("7", "first second ",7,7.7, false);

Hyperdrive的综合查询的前置条件
--建表
CREATE TABLE hbase_index_demo(key INT, dt INT, hphm STRING)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES ('hbase.columns.mapping'
=':key#b,dt:int#b,cf:val');

--建全文索引
CREATE FULLTEXT INDEX ON hbase_index_demo(dt,hphm) SHARD NUM 1;

3、执行SampleCode.java，可多线程执行查询，如开kerberos安全，需要从集群拿hive.keytab以及krb5.conf