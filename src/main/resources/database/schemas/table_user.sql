CREATE TABLE t_user (
  id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY,
  userName VARCHAR(30),
  passWord VARCHAR(200)
);
-- create user admin password docdocadmin
insert into t_user(userName, passWord) values('admin', '$2a$10$o3m1FvmLwY72VJC/cTHyau2gtLjLQ2jrzo8bl5svxddYStbw85etS');
