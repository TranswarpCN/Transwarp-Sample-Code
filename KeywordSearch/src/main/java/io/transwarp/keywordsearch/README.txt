1、首先运行Extractor.java，注意修改/resources/setup.xml的前面两项配置

2、将生成的txt文件传到Linux机器，然后上传到HDFS上

3、在Inceptor中创建HDFS外表，然后在HBase中创建全文索引（由于HBase,ES相关API不对外开放，需要通过命令行实现）
use fulltxt_sqd;

create external table output_6 (
path string,
rownum int,
txt string
)
row format delimited fields terminated by '|'
location '/tmp/tanliming';

location是txt文件所在目录

create table output_62 (
key string,
path string,
rownum int,
txt string
) stored by 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
with serdeproperties
("hbase.columns.mapping"=":key,
c1:path,
c1:rownum,
c1:txt")
tblproperties ("hbase.table.name"="output_62");

insert into output_62
select /*+USE_BULKLOAD*/ concat(path,'-',uniq()) key,
path,rownum,txt from output_6 order by key;

create fulltext index on output_62 (path ,rownum ,txt ) shard num 1;


hbase-shell:
describeInJson 'output_62', 'true', '/tmp/p.txt'

{
  "tableName" : "output_62",
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
  	"tableName":"output_62",
  	"allowUpdate":true,
  	"ttl":63072000000,
  	"source":true,
  	"all":false,
  	"storeAsSoucre":false,
  	"storeFamily":"",
  	"fields":[{
  		"family":"c1",
  		"qualifier":"path",
  		"encode_as_string":false,
  		"attributes":{
  			"index":"not_analyzed",
  			"store":"false",
  			"doc_values":"false",
  			"type":"string",
  			"analyzer":"ik",
  			"search_analyzer":"ik"
  		}
  	},{
      "family":"c1",
      "qualifier":"rownum",
      "encode_as_string":false,
      "attributes":{
        "index":"not_analyzed",
        "store":"false",
        "doc_values":"false",
        "type":"int",
        "analyzer":"ik",
        "search_analyzer":"ik"
      }
    },{
  		"family":"c1",
  		"qualifier":"txt",
  		"encode_as_string":false,
  		"attribues":{
  			"index":"analyzed",
  			"store":"false",
  			"doc_values":"false",
  			"type":"string",
  			"analyzer":"ik",
  			"search_analyzer":"ik"
  		}
  	}],
  		"settings": {
  			"index.number_of_replicas":"0",
  			"index.number_of_shards":"12"
  		}
  },
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

alterUseJson 'output_62', '/tmp/p1.txt'


rebuild_fulltext_index 'output_62'

4、修改/resources/setup.xml下相关配置，运行SampleCode.java，注意本程序中在查询全文索引时可以多线程执行