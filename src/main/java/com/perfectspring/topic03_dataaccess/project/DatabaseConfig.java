package com.perfectspring.topic03_dataaccess.project;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * The SET-UP of the records room. Done ONCE for the whole app.
 *
 * Note: we use @Bean here (not @Component) because DataSource, JdbcTemplate and
 * DataSourceTransactionManager are classes from Spring/H2. We can't add badges to them.
 */
@Configuration
@ComponentScan                       // finds StudentRepository and StudentService
@EnableTransactionManagement         // switches @Transactional on
public class DatabaseConfig {

    /** The key desk: an in-memory H2 database, built from our SQL scripts. */
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .addScript("topic03/schema.sql")    // from src/main/resources
                .addScript("topic03/data.sql")
                .build();
        // To use MySQL instead, you would return a pooled DataSource
        // (e.g. HikariDataSource) with a URL, username and password. Nothing else changes.
    }

    /** The records clerk. */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /** The bank manager who commits or rolls back transactions. */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
