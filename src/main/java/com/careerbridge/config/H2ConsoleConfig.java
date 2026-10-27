package com.careerbridge.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * H2 Web Console Configuration.
 * 
 * Registers the H2 JakartaWebServlet to provide interactive SQL console access
 * at /h2-console for database inspection, verification, and live queries.
 */
@Configuration
public class H2ConsoleConfig {

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2ConsoleServletRegistration() {
        ServletRegistrationBean<JakartaWebServlet> registration = new ServletRegistrationBean<>(
                new JakartaWebServlet(), 
                "/h2-console/*", 
                "/h2-console"
        );
        registration.setName("H2Console");
        return registration;
    }
}
