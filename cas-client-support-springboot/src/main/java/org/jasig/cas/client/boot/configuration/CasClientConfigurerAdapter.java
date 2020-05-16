package org.jasig.cas.client.boot.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;

/**
 * @author lemon
 * @description
 * // by develoepr lemon
 * {@link org.jasig.cas.client.boot.configuration.CasClientConfigurer}
 * @date 2020-05-09 07:39
 * @see org.jasig.cas.client.boot.configuration.CasClientConfigurer
 */
public class CasClientConfigurerAdapter implements CasClientConfigurer {

    @Override
    public void configureAuthenticationFilter(FilterRegistrationBean authenticationFilter) {
        // Noop. Designed to be overridden if necessary to ease plugging in custom configs.
    }

    @Override
    public void configureValidationFilter(FilterRegistrationBean validationFilter) {
        // Noop. Designed to be overridden if necessary to ease plugging in custom configs.
    }

    @Override
    public void configureHttpServletRequestWrapperFilter(FilterRegistrationBean httpServletRequestWrapperFilter) {
        // Noop. Designed to be overridden if necessary to ease plugging in custom configs.
    }

    @Override
    public void configureAssertionThreadLocalFilter(FilterRegistrationBean assertionThreadLocalFilter) {
        // Noop. Designed to be overridden if necessary to ease plugging in custom configs.
    }
}
