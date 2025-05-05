package com.example.bank;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));
	}

	@Bean
	DynamicPropertyRegistrar dynamicPropertyRegistrar(PostgreSQLContainer<?> postgresContainer) {
		return registry -> {
			registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
			registry.add("spring.datasource.username", postgresContainer::getUsername);
			registry.add("spring.datasource.password", postgresContainer::getPassword);
		};
	}

}
