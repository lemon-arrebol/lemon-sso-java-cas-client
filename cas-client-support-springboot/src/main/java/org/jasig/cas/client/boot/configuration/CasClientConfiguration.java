/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.boot.configuration;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.authentication.Saml11AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.jasig.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter;
import org.jasig.cas.client.validation.Saml11TicketValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.EventListener;

/**
 * Configuration class providing default CAS client infrastructure filters.
 * This configuration facility is typically imported into Spring's Application Context via
 * {@link EnableCasClient} meta annotation.
 * by develoepr lemon add @Import(CasClientSingleSignOutConfiguration.class)
 *
 * @author Dmitriy Kopylenko
 * @since 3.6.0
 */
@Configuration
@EnableConfigurationProperties(CasClientConfigurationProperties.class)
@Import(CasClientSingleSignOutConfiguration.class)
public class CasClientConfiguration {

    @Autowired
    CasClientConfigurationProperties configProps;

    private CasClientConfigurer casClientConfigurer;

    /**
     * @param casUrlParamName
     * @param casUrlParamVal
     * @param clientHostUrlVal
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @description 构造过滤器初始参数
     * @author lemon
     * @date 2020-05-09 13:16
     */
    private static Map<String, String> constructInitParams(final String casUrlParamName, final String casUrlParamVal, final String clientHostUrlVal) {
        final Map<String, String> initParams = new HashMap<>(2);
        initParams.put(casUrlParamName, casUrlParamVal);
        initParams.put("serverName", clientHostUrlVal);
        return initParams;
    }

    /**
     * @param filterRegistrationBean
     * @param targetFilter
     * @param filterOrder
     * @param initParams
     * @param urlPatterns
     * @return void
     * @description 初始化过滤器，设置Filter、InitParameters
     * @author lemon
     * @date 2020-05-09 13:16
     */
    private static void initFilter(final FilterRegistrationBean filterRegistrationBean,
                                   final Filter targetFilter,
                                   final int filterOrder,
                                   final Map<String, String> initParams,
                                   final List<String> urlPatterns) {

        filterRegistrationBean.setFilter(targetFilter);
        filterRegistrationBean.setOrder(filterOrder);
        filterRegistrationBean.setInitParameters(initParams);
        if (!urlPatterns.isEmpty()) {
            filterRegistrationBean.setUrlPatterns(urlPatterns);
        }
    }

    /**
     * @param
     * @return org.springframework.boot.web.servlet.FilterRegistrationBean
     * @description TicketValidation配置，通过 {@link CasClientConfigurer#configureValidationFilter} 可以自定义TicketValidation配置
     * @author lemon
     * @date 2020-05-09 13:18
     */
    @Bean
    @ConditionalOnProperty(prefix = "cas", name = "skipTicketValidation", havingValue = "false", matchIfMissing = true)
    public FilterRegistrationBean casValidationFilter() {
        final FilterRegistrationBean validationFilter = new FilterRegistrationBean();
        final Filter targetCasValidationFilter;
        switch (this.configProps.getValidationType()) {
            case CAS:
                targetCasValidationFilter = new Cas20ProxyReceivingTicketValidationFilter();
                break;
            case SAML:
                targetCasValidationFilter = new Saml11TicketValidationFilter();
                break;
            case CAS3:
            default:
                targetCasValidationFilter = new Cas30ProxyReceivingTicketValidationFilter();
                break;
        }

        initFilter(validationFilter,
            targetCasValidationFilter,
            1,
            constructInitParams("casServerUrlPrefix", this.configProps.getServerUrlPrefix(), this.configProps.getClientHostUrl()),
            this.configProps.getValidationUrlPatterns());

        if (this.configProps.getUseSession() != null) {
            validationFilter.getInitParameters().put("useSession", String.valueOf(this.configProps.getUseSession()));
        }
        if (this.configProps.getRedirectAfterValidation() != null) {
            validationFilter.getInitParameters().put("redirectAfterValidation", String.valueOf(this.configProps.getRedirectAfterValidation()));
        }

        //Proxy tickets validation
        if (this.configProps.getAcceptAnyProxy() != null) {
            validationFilter.getInitParameters().put("acceptAnyProxy", String.valueOf(this.configProps.getAcceptAnyProxy()));
        }
        if (!this.configProps.getAllowedProxyChains().isEmpty()) {
            validationFilter.getInitParameters().put("allowedProxyChains",
                StringUtils.collectionToDelimitedString(this.configProps.getAllowedProxyChains(), " "));
        }
        if (this.configProps.getProxyCallbackUrl() != null) {
            validationFilter.getInitParameters().put("proxyCallbackUrl", this.configProps.getProxyCallbackUrl());
        }
        if (this.configProps.getProxyReceptorUrl() != null) {
            validationFilter.getInitParameters().put("proxyReceptorUrl", this.configProps.getProxyReceptorUrl());
        }

        if (this.casClientConfigurer != null) {
            this.casClientConfigurer.configureValidationFilter(validationFilter);
        }
        return validationFilter;
    }

    /**
     * @param
     * @return org.springframework.boot.web.servlet.FilterRegistrationBean
     * @description 创建认证过滤器，通过 {@link CasClientConfigurer#configureAuthenticationFilter} 可以自定义认证过滤器配置
     * @author lemon
     * @date 2020-05-09 13:13
     */
    @Bean
    public FilterRegistrationBean casAuthenticationFilter() {
        final FilterRegistrationBean authnFilter = new FilterRegistrationBean();
        final Filter targetCasAuthnFilter =
            this.configProps.getValidationType() == EnableCasClient.ValidationType.CAS
                || configProps.getValidationType() == EnableCasClient.ValidationType.CAS3
                ? new AuthenticationFilter()
                : new Saml11AuthenticationFilter();

        initFilter(authnFilter,
            targetCasAuthnFilter,
            2,
            constructInitParams("casServerLoginUrl", this.configProps.getServerLoginUrl(), this.configProps.getClientHostUrl()),
            this.configProps.getAuthenticationUrlPatterns());

        if (this.configProps.getGateway() != null) {
            authnFilter.getInitParameters().put("gateway", String.valueOf(this.configProps.getGateway()));
        }

        if (this.casClientConfigurer != null) {
            this.casClientConfigurer.configureAuthenticationFilter(authnFilter);
        }
        return authnFilter;
    }

    /**
     * @param
     * @return org.springframework.boot.web.servlet.FilterRegistrationBean
     * @description 创建ServletRequestWrapper，通过 {@link CasClientConfigurer#configureHttpServletRequestWrapperFilter} 可以自定义ServletRequestWrapper配置
     * @author lemon
     * @date 2020-05-09 13:14
     */
    @Bean
    public FilterRegistrationBean casHttpServletRequestWrapperFilter() {
        final FilterRegistrationBean reqWrapperFilter = new FilterRegistrationBean();
        reqWrapperFilter.setFilter(new HttpServletRequestWrapperFilter());
        if (!this.configProps.getRequestWrapperUrlPatterns().isEmpty()) {
            reqWrapperFilter.setUrlPatterns(this.configProps.getRequestWrapperUrlPatterns());
        }
        reqWrapperFilter.setOrder(3);

        if (this.casClientConfigurer != null) {
            this.casClientConfigurer.configureHttpServletRequestWrapperFilter(reqWrapperFilter);
        }
        return reqWrapperFilter;
    }

    /**
     * @param
     * @return org.springframework.boot.web.servlet.FilterRegistrationBean
     * @description 创建AssertionThreadLocal，通过 {@link CasClientConfigurer#configureAssertionThreadLocalFilter} 可以自定义AssertionThreadLocal配置
     * @author lemon
     * @date 2020-05-09 13:15
     */
    @Bean
    public FilterRegistrationBean casAssertionThreadLocalFilter() {
        final FilterRegistrationBean assertionTLFilter = new FilterRegistrationBean();
        assertionTLFilter.setFilter(new AssertionThreadLocalFilter());
        if (!this.configProps.getAssertionThreadLocalUrlPatterns().isEmpty()) {
            assertionTLFilter.setUrlPatterns(this.configProps.getAssertionThreadLocalUrlPatterns());
        }
        assertionTLFilter.setOrder(4);

        if (this.casClientConfigurer != null) {
            this.casClientConfigurer.configureAssertionThreadLocalFilter(assertionTLFilter);
        }
        return assertionTLFilter;
    }

    /**
     * @param configurers
     * @return void
     * @description 注入CasClientConfigurer，只能有一个
     * @author lemon
     * @date 2020-05-09 13:16
     */
    @Autowired(required = false)
    void setConfigurers(final Collection<CasClientConfigurer> configurers) {
        if (CollectionUtils.isEmpty(configurers)) {
            return;
        }
        if (configurers.size() > 1) {
            throw new IllegalStateException(configurers.size() + " implementations of " +
                "CasClientConfigurer were found when only 1 was expected. " +
                "Refactor the configuration such that CasClientConfigurer is " +
                "implemented only once or not at all.");
        }
        this.casClientConfigurer = configurers.iterator().next();
    }

    /**
     * @param
     * @return org.springframework.boot.web.servlet.FilterRegistrationBean
     * @description 单点退出过滤器
     * @author lemon
     * @date 2020-05-09 13:13
     */
    @Bean
    @ConditionalOnProperty(prefix = "cas", value = "single-logout.enabled", havingValue = "true")
    public FilterRegistrationBean casSingleSignOutFilter() {
        final FilterRegistrationBean singleSignOutFilter = new FilterRegistrationBean();
        singleSignOutFilter.setFilter(new SingleSignOutFilter());
        Map<String,String> initParameters = new HashMap<>(1);
        initParameters.put("casServerUrlPrefix", configProps.getServerUrlPrefix());
        singleSignOutFilter.setInitParameters(initParameters);
        singleSignOutFilter.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return singleSignOutFilter;
    }

    /**
     * @param
     * @return org.springframework.boot.web.servlet.ServletListenerRegistrationBean<java.util.EventListener>
     * @description 单点退出监听器
     * @author lemon
     * @date 2020-05-09 13:12
     */
    @Bean
    @ConditionalOnProperty(prefix = "cas", value = "single-logout.enabled", havingValue = "true")
    public ServletListenerRegistrationBean<EventListener> casSingleSignOutListener(){
        ServletListenerRegistrationBean<EventListener> singleSignOutListener = new ServletListenerRegistrationBean<>();
        singleSignOutListener.setListener(new SingleSignOutHttpSessionListener());
        singleSignOutListener.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return singleSignOutListener;
    }
}
