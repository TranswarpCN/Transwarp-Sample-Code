在Inceptor中新建一张表，并插入数据
drop table if exists User;
create table if not exists User(id int ,userName string,userAge string,userAddress string) clustered by(id) into 10 buckets stored as orc tblproperties("transactional"="true");
insert into table User(id,userName,userAge,userAddress) values(1,'bily','50','pudong');