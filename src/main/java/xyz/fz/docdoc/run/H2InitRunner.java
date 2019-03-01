package xyz.fz.docdoc.run;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;

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
            try {
                dbInit();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("数据库初始化异常：{}", e.getMessage());
            }
        }
    }

    private void dbInit() {
        String basePath = "/database/schemas/";
        String[] tables = new String[]{
                "table_doc_api.sql",
                "table_doc_api_field.sql",
                "table_doc_api_log.sql",
                "table_doc_api_response_example.sql",
                "table_doc_project.sql",
                "table_user.sql",
                "table_test.sql"
        };
        initTables(basePath, tables);
        LOGGER.info("初始用户admin创建成功，初始密码：docdocadmin，请在用户管理中进行修改");
    }

    private void initTables(String basePath, String[] tables) {
        for (String table : tables) {
            try (InputStream inputStream = H2InitRunner.class.getResourceAsStream(basePath + table)) {
                jdbcTemplate.execute(IOUtils.toString(inputStream, "utf-8"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
