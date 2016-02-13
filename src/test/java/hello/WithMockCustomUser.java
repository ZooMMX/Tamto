package hello;

import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * Created by octavioruiz on 11/02/16.
 */
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String username() default "rob";

    String name() default "Rob Winch";
}