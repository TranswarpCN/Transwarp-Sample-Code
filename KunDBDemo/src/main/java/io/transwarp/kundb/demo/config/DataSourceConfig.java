//package io.transwarp.kundb.demo.config;
//
//import org.apache.tomcat.jdbc.pool.DataSource;
//import org.apache.commons.dbcp2.BasicDataSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//
//@Configuration
//public class DataSourceConfig {
//	@Value("${spring.datasource.type}")
//	private String dataSourceType;
//
//	@ConfigurationProperties(prefix = "spring.datasource")
//    @Bean(name="dataSource")
//    public DataSource dataSource(){
//////		DataSource datasource = applicationContext.getBean(DataSource.class);
//		DataSource datasource = null;
//		try {
//			Class<?> aClass = Class.forName(dataSourceType);
//			datasource = (DataSource) aClass.getConstructor().newInstance();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		return datasource;
////		return new BasicDataSource();
//    }
//}
