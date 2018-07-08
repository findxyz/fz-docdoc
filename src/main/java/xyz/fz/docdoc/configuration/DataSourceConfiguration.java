package xyz.fz.docdoc.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = {"xyz.fz.docdoc.repository"})
@PropertySource(value = {"classpath:application.properties"})
public class DataSourceConfiguration {

    private final Environment env;

    @Autowired
    public DataSourceConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(env.getProperty("datasource.driver-class-name"));
        hikariConfig.setJdbcUrl(env.getProperty("datasource.url"));
        hikariConfig.setUsername(env.getProperty("datasource.username"));
        hikariConfig.setPassword(env.getProperty("datasource.password"));
        hikariConfig.setMaximumPoolSize(30);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("springHikariCP");
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    @Autowired
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(Boolean.TRUE);
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("xyz.fz.docdoc.entity");
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", env.getProperty("hibernate.dialect"));
        jpaProperties.put("hibernate.naming.physical-strategy", env.getProperty("hibernate.naming.physical-strategy"));
        jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
        jpaProperties.put("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
        factory.setJpaProperties(jpaProperties);
        return factory;
    }

    @Bean
    @Autowired
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    @Bean
    @Autowired
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Autowired
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
