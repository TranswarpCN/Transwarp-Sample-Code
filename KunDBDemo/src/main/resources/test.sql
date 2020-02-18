USE `kundb1`;
CREATE TABLE if not exists `xuser` (
  `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `age` int(11) DEFAULT 0 COMMENT '年龄',
  `is_delete` smallint default 0 COMMENT '删除状态',
  `update_time` timestamp,
  PRIMARY KEY (`Id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 PARTITION BY HASH(name) USING `binary_md5`;
-- create global unique index xuser_id on xuser(id);
-- drop index xuser_id on xuser;
-- CREATE TABLE if not exists `xuser` (
--   `Id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
--   `name` varchar(255) DEFAULT NULL COMMENT '姓名',
--   `age` int(11) DEFAULT 0 COMMENT '年龄',
--   `is_delete` smallint default 0 COMMENT '删除状态',
--   `update_time` timestamp,
--   PRIMARY KEY (`Id`)
-- )ENGINE=InnoDB DEFAULT CHARSET=utf8 PARTITION BY HASH(id) USING `binary_md5`;
