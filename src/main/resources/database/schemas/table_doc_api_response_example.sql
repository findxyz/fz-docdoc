CREATE TABLE t_doc_api_response_example (
  id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY,
  apiId BIGINT,
  owner VARCHAR(30),
  responseExample VARCHAR(5000),
  updateTime TIMESTAMP
);
