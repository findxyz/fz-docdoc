CREATE TABLE t_doc_api (
  id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY,
  projectId BIGINT,
  name VARCHAR(100),
  requestUrl VARCHAR(200),
  regexUrl VARCHAR(200),
  authType VARCHAR(50),
  contentType VARCHAR(50),
  requestMethod VARCHAR(10),
  dataType VARCHAR(20),
  author VARCHAR(30),
  createTime TIMESTAMP,
  updateTime TIMESTAMP,
  requestExample VARCHAR(3000),
  responseExample VARCHAR(5000),
  status VARCHAR(20),
  version BIGINT,
  isActivity INT
);
