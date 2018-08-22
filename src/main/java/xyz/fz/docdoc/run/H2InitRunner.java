package xyz.fz.docdoc.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class H2InitRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2InitRunner.class);

    private JdbcTemplate jdbcTemplate;

    public H2InitRunner(ApplicationContext context) {
        this.jdbcTemplate = context.getBean("jdbcTemplate", JdbcTemplate.class);
    }

    @Override
    public void run() {
        try {
            String testSql = "SELECT * FROM t_test ";
            jdbcTemplate.execute(testSql);
            // already init
            LOGGER.debug("Db already init");
        } catch (BadSqlGrammarException badSqlGrammarException) {
            LOGGER.debug("Do db init");
            // do init
            dbInit();
        }
    }

    private void dbInit() {
        initTestTable();
        initUserTable();
        initProjectTable();
        initApiTable();
        initApiFieldTable();
        initApiLogTable();
        initApiResponseExampleTable();
    }

    private void initTestTable() {
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY, ");
        initTable("t_test", fieldList);
    }

    private void initUserTable() {
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY, ");
        fieldList.add("userName VARCHAR(30), ");
        fieldList.add("passWord VARCHAR(30) ");
        initTable("t_user", fieldList);
        String adminSql = "insert into t_user(userName, passWord) values('admin', 'docdocadmin'); ";
        jdbcTemplate.execute(adminSql);
        LOGGER.info("初始用户admin创建成功，初始密码：docdocadmin，请在用户管理中进行修改");
    }

    private void initProjectTable() {
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY, ");
        fieldList.add("name VARCHAR(100), ");
        fieldList.add("isActivity INT ");
        initTable("t_doc_project", fieldList);
    }

    private void initApiTable() {
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY, ");
        fieldList.add("projectId BIGINT, ");
        fieldList.add("name VARCHAR(100), ");
        fieldList.add("requestUrl VARCHAR(200), ");
        fieldList.add("authType VARCHAR(50), ");
        fieldList.add("contentType VARCHAR(50), ");
        fieldList.add("requestMethod VARCHAR(10), ");
        fieldList.add("dataType VARCHAR(20), ");
        fieldList.add("author VARCHAR(30), ");
        fieldList.add("createTime TIMESTAMP, ");
        fieldList.add("updateTime TIMESTAMP, ");
        fieldList.add("requestExample VARCHAR(1000), ");
        fieldList.add("responseExample VARCHAR(5000), ");
        fieldList.add("status VARCHAR(20), ");
        fieldList.add("version BIGINT, ");
        fieldList.add("isActivity INT ");
        initTable("t_doc_api", fieldList);
    }

    private void initApiFieldTable() {
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY, ");
        fieldList.add("apiId BIGINT, ");
        fieldList.add("actionType VARCHAR(10), ");
        fieldList.add("meaning VARCHAR(50), ");
        fieldList.add("name VARCHAR(50), ");
        fieldList.add("paramType VARCHAR(10), ");
        fieldList.add("required INT, ");
        fieldList.add("updateTime TIMESTAMP, ");
        fieldList.add("version BIGINT, ");
        fieldList.add("isActivity INT ");
        initTable("t_doc_api_field", fieldList);
    }

    private void initApiLogTable() {
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY, ");
        fieldList.add("apiId BIGINT, ");
        fieldList.add("author VARCHAR(30), ");
        fieldList.add("createTime TIMESTAMP, ");
        fieldList.add("reason VARCHAR(500), ");
        fieldList.add("isActivity INT ");
        initTable("t_doc_api_log", fieldList);
    }

    private void initApiResponseExampleTable() {
        List<String> fieldList = new ArrayList<>();
        fieldList.add("id BIGINT AUTO_INCREMENT(0, 1) PRIMARY KEY, ");
        fieldList.add("apiId BIGINT, ");
        fieldList.add("ip VARCHAR(30), ");
        fieldList.add("responseExample VARCHAR(5000), ");
        fieldList.add("updateTime TIMESTAMP ");
        initTable("t_doc_api_response_example", fieldList);
    }

    private void initTable(String tableName, List<String> fieldList) {
        String createTableSql = "CREATE TABLE " + tableName + "(#{fieldSql});";
        StringBuilder fieldSql = new StringBuilder();
        for (String field : fieldList) {
            fieldSql.append(field);
        }
        jdbcTemplate.execute((createTableSql).replace("#{fieldSql}", fieldSql.toString()));
    }
}
