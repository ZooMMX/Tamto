package hello;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by octavioruizcastillo on 08/02/16.
 */
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
@Configuration
@ComponentScan
public class IntegrationTestApplication {
}
