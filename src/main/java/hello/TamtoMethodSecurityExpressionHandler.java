package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

/**
 * Created by octavioruiz on 11/02/16.
 */

@Configuration
public class TamtoMethodSecurityExpressionHandler {
    @Autowired
    PermissionEvaluator tamtoPermissionEvaluator;

    @Bean
    public MethodSecurityExpressionHandler expressionHandler() {
        DefaultMethodSecurityExpressionHandler bean = new DefaultMethodSecurityExpressionHandler();
        bean.setPermissionEvaluator(tamtoPermissionEvaluator);
        return bean;
    }
}