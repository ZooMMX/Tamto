package hello;

import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.PermissionEvaluator;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.servlet.MultipartConfigElement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@EnableAutoConfiguration
@Configuration
@ComponentScan
public class Application {

    public static void main(String[] args) throws Throwable {
        //SpringApplication.run(Application.class, args);
	 	ConfigurableApplicationContext context = SpringApplication.run(Application.class);
	
    }

    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("128MB");
        factory.setMaxRequestSize("128MB");
        return factory.createMultipartConfig();
    }

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        TamtoPermissionEvaluator bean = new TamtoPermissionEvaluator();
        return bean;
    }

}
