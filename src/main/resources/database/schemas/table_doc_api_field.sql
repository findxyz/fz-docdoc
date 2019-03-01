CREATE TABLE t_doc_api_field (
  id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY,
  apiId BIGINT,
  actionType VARCHAR(10),
  meaning VARCHAR(50),
  name VARCHAR(50),
  paramType VARCHAR(10),
  required INT,
  updateTime TIMESTAMP,
  version BIGINT,
  isActivity INT
);
