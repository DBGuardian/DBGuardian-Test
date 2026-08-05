package com.dbguardian.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * DBGuardian 测试项目 - Java8 + Spring Boot 2.7 + JPA/Hibernate
 *
 * 技术栈: [JAVA8] [SPRING_BOOT_27] [JPA_HIBERNATE] [MYSQL]
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class
})
@EnableJpaRepositories(basePackages = "com.dbguardian.test.repository")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}