package hello;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by octavioruizcastillo on 03/02/16.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = PiezaRepository.class)
@Import({ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@EntityScan(basePackageClasses = {Pieza.class, HomeController.class})
public class TestApplication {
}
