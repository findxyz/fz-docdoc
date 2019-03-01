CREATE TABLE t_doc_api_log (
  id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY,
  apiId BIGINT,
  author VARCHAR(30),
  createTime TIMESTAMP,
  reason VARCHAR(500),
  isActivity INT
);
