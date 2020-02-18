package io.transwarp.kundb.demo.config;

//import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * mybatis没有kundb方言，因此要自定义entitymanagerconfig，指定使用mysql方言
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
public class EntityManageConfig {
	@Autowired
	private DataSource dataSource;
	@Primary
	@Bean(name = "entityManagerPrimary")
	public EntityManager entityManager(EntityManagerFactoryBuilder builder){
		return entityManageFactory(builder).getObject().createEntityManager();
	}

	@Primary
	@Bean(name = "entityManageFactoryPrimary")
	public LocalContainerEntityManagerFactoryBean entityManageFactory(EntityManagerFactoryBuilder builder){
		LocalContainerEntityManagerFactoryBean entityManagerFactory =  builder.dataSource(dataSource)
				.packages("com.hope.monsterlan.entity").build();
		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
		jpaProperties.put("hibernate.physical_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
		jpaProperties.put("hibernate.connection.charSet", "utf-8");
		jpaProperties.put("hibernate.show_sql", "true");
		entityManagerFactory.setJpaProperties(jpaProperties);
		return entityManagerFactory;
	}

	@Primary
	@Bean(name = "transactionManagerPrimary")
	public PlatformTransactionManager transactionManager(EntityManagerFactoryBuilder builder) {
		return new JpaTransactionManager(entityManageFactory(builder).getObject());
	}
}
