package com.example.bank.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.batch.BatchTransactionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.batch.datasource.url")
public class DataSourceConfig {

	@Bean
	@ConfigurationProperties("spring.datasource")
	public DataSourceProperties defaultDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties("spring.datasource.hikari")
	public HikariDataSource defaultDataSource(
			@Qualifier("defaultDataSourceProperties") DataSourceProperties properties) {
		HikariDataSource dataSource = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		dataSource.setPoolName("default-pool");
		return dataSource;
	}

	@Bean
	@ConfigurationProperties("spring.batch.datasource")
	public DataSourceProperties batchDataSourceProperties() {
		return new DataSourceProperties();
	}

	@BatchDataSource
	@Bean(defaultCandidate = false)
	@ConfigurationProperties("spring.batch.datasource.hikari")
	public HikariDataSource batchDataSource(@Qualifier("batchDataSourceProperties") DataSourceProperties properties) {
		HikariDataSource dataSource = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		dataSource.setPoolName("batch-pool");
		return dataSource;
	}

	@BatchTransactionManager
	@Bean(defaultCandidate = false)
	public PlatformTransactionManager batchTransactionManager(@BatchDataSource DataSource dataSource) {
		return new JdbcTransactionManager(dataSource);
	}

}
