package com.perfectspring.topic03_dataaccess.example;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * EXAMPLE 3 — TRANSACTIONS: ALL OR NOTHING
 *
 * Story: a parent pays N50,000 school fees.
 *   Step 1: take money from the parent
 *   Step 2: 💥 power cut!
 *   Step 3: give money to the school   (never happens)
 *
 * Without a transaction -> the money VANISHES.
 * With @Transactional    -> step 1 is UNDONE (rolled back). Nobody loses money.
 */
public class Example3_Transactions {

    @Configuration
    @EnableTransactionManagement                       // "Spring, please respect @Transactional"
    static class BankConfig {

        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true).setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean                                          // the "bank manager" who commits or rolls back
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        FeesService feesService(JdbcTemplate jdbc) {
            return new FeesService(jdbc);
        }
    }

    @Service
    static class FeesService {
        private final JdbcTemplate jdbc;

        FeesService(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        void setUp() {
            jdbc.execute("CREATE TABLE account (owner VARCHAR(20) PRIMARY KEY, balance INT)");
            jdbc.update("INSERT INTO account VALUES ('Parent', 100000), ('School', 0)");
        }

        /** ❌ NO transaction: each UPDATE is saved immediately, on its own. */
        void payFeesWithoutTransaction(int amount) {
            jdbc.update("UPDATE account SET balance = balance - ? WHERE owner = 'Parent'", amount);
            throw new RuntimeException("BANG! Power cut halfway through!");
            // the "give money to the school" step never runs
        }

        /** ✅ WITH a transaction: if anything fails, EVERYTHING in this method is undone. */
        @Transactional
        public void payFeesWithTransaction(int amount) {
            jdbc.update("UPDATE account SET balance = balance - ? WHERE owner = 'Parent'", amount);
            throw new RuntimeException("BANG! Power cut halfway through!");
        }

        void printBalances(String title) {
            Integer parent = jdbc.queryForObject("SELECT balance FROM account WHERE owner = 'Parent'", Integer.class);
            Integer school = jdbc.queryForObject("SELECT balance FROM account WHERE owner = 'School'", Integer.class);
            System.out.println(title);
            System.out.println("   Parent: N" + parent + "   School: N" + school + "   Total: N" + (parent + school));
        }
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(BankConfig.class)) {
            FeesService fees = context.getBean(FeesService.class);
            fees.setUp();
            fees.printBalances("Start:");

            try {
                fees.payFeesWithoutTransaction(50000);
            } catch (RuntimeException e) {
                System.out.println("\n" + e.getMessage());
            }
            fees.printBalances("After paying WITHOUT a transaction (money VANISHED!):");

            try {
                fees.payFeesWithTransaction(30000);
            } catch (RuntimeException e) {
                System.out.println("\n" + e.getMessage());
            }
            fees.printBalances("After trying to pay N30000 WITH @Transactional (ROLLED BACK: the N30000 was never taken):");
        }
    }
}
