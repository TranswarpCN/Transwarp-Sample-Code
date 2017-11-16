在Inceptor中新建一张表，并插入数据
create table if not exists User(id int ,name string) clustered by(id) into 10 buckets stored as orc tblproperties("transactional"="true");
insert into table User(id,name) values(1,'bily');