package com.lemon.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.boot.authentication.AjaxAuthenticationRedirectStrategy;
import org.jasig.cas.client.boot.authentication.CustomAuthenticationFilter;
import org.jasig.cas.client.boot.authentication.MultiPatternUrlPatternMatcherStrategy;
import org.jasig.cas.client.boot.configuration.CasClientConfigurationProperties;
import org.jasig.cas.client.boot.configuration.CasClientConfigurer;
import org.jasig.cas.client.boot.configuration.CasClientConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lemon
 * @description // by develoepr lemon
 * {@link CasClientConfigurer}
 * @date 2020-05-09 07:39
 * @see CasClientConfigurer
 */
@Slf4j
@Component
public class LemonCasClientConfigurerAdapter extends CasClientConfigurerAdapter {
    @Autowired
    CasClientConfigurationProperties configProps;

    @Override
    public void configureAuthenticationFilter(FilterRegistrationBean authenticationFilter) {
        super.configureAuthenticationFilter(authenticationFilter);

        if (log.isDebugEnabled()) {
            log.debug("AuthenticationFilter is replaced by {} to {}", CustomAuthenticationFilter.class, authenticationFilter.getFilter().getClass());
        }

        CustomAuthenticationFilter targetCasAuthnFilter = new CustomAuthenticationFilter();
        targetCasAuthnFilter.setAuthenticationRedirectStrategy(new AjaxAuthenticationRedirectStrategy(this.configProps.getAjaxFilter()));
        authenticationFilter.setFilter(targetCasAuthnFilter);

        Map<String, String> initParams = authenticationFilter.getInitParameters();

        if (StringUtils.isNotBlank(this.configProps.getConfigurationStrategy())) {
            initParams.put("configurationStrategy", this.configProps.getConfigurationStrategy());
        }

        if (StringUtils.isNotBlank(this.configProps.getAuthenticationRedirectStrategyClass())) {
            initParams.put("authenticationRedirectStrategyClass", this.configProps.getAuthenticationRedirectStrategyClass());
        }

        if (MultiPatternUrlPatternMatcherStrategy.class.getName().equals(this.configProps.getIgnoreUrlPatternType())) {
            MultiPatternUrlPatternMatcherStrategy urlPatternMatcherStrategy = new MultiPatternUrlPatternMatcherStrategy();
            urlPatternMatcherStrategy.setPattern(this.configProps.getIgnorePattern());
            targetCasAuthnFilter.setIgnoreUrlPatternMatcherStrategyClass(urlPatternMatcherStrategy);
        } else {
            // ignorePattern 设置哪些URL忽略不认证，可以是正则表达式
            if (StringUtils.isNotBlank(this.configProps.getIgnorePattern())) {
                initParams.put("ignorePattern", this.configProps.getIgnorePattern());
            }

            // ignoreUrlPatternType 可以指定这四种CONTAINS、REGEX、FULL_REGEX、EXACT，也可以指定UrlPatternMatcherStrategy实现类完整路径
            if (StringUtils.isNotBlank(this.configProps.getIgnoreUrlPatternType())) {
                initParams.put("ignoreUrlPatternType", this.configProps.getIgnoreUrlPatternType());
            }
        }
    }

    @Override
    public void configureValidationFilter(FilterRegistrationBean validationFilter) {
        super.configureValidationFilter(validationFilter);
    }

    @Override
    public void configureHttpServletRequestWrapperFilter(FilterRegistrationBean httpServletRequestWrapperFilter) {
        super.configureHttpServletRequestWrapperFilter(httpServletRequestWrapperFilter);
    }

    @Override
    public void configureAssertionThreadLocalFilter(FilterRegistrationBean assertionThreadLocalFilter) {
        super.configureAssertionThreadLocalFilter(assertionThreadLocalFilter);
    }
}
