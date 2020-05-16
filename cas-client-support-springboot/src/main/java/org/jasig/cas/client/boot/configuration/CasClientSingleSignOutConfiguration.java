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

import lombok.extern.slf4j.Slf4j;
import org.jasig.cas.client.boot.authentication.AjaxAuthenticationRedirectStrategy;
import org.jasig.cas.client.boot.logout.CustomSingleSignOutFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class providing default CAS client infrastructure filters.
 * This configuration facility is typically imported into Spring's Application Context via
 * {@link EnableCasClient} meta annotation.
 * by develoepr lemon add @Import(CasClientSingleSignOutConfiguration.class)
 *
 * @author Dmitriy Kopylenko
 * @since 3.6.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(CasClientConfigurationProperties.class)
public class CasClientSingleSignOutConfiguration {

    @Autowired
    CasClientConfigurationProperties configProps;

    /**
     * @param
     * @return org.springframework.boot.web.servlet.FilterRegistrationBean
     * @description 单点退出过滤器
     * @author lemon
     * @date 2020-05-09 13:13
     */
    @Bean
    @ConditionalOnProperty(prefix = "cas", value = "custom-single-logout-enabled", havingValue = "true")
    public FilterRegistrationBean customSingleSignOutFilter() {
        Assert.isTrue(configProps.getSingleLogout() == null || !configProps.getSingleLogout().isEnabled(), "casSingleSignOutFilter conflicts with current  customSingleSignOutFilter");

        if(log.isDebugEnabled()) {
            log.debug("Register SingleSignOutFilter {}", CustomSingleSignOutFilter.class);
        }

        final FilterRegistrationBean singleSignOutFilter = new FilterRegistrationBean();
        CustomSingleSignOutFilter customSingleSignOutFilter = new CustomSingleSignOutFilter();
        customSingleSignOutFilter.setRedirectStrategy(new AjaxAuthenticationRedirectStrategy(this.configProps.getAjaxFilter()));
        customSingleSignOutFilter.setClientHomePageUrl(this.configProps.getClientHomePageUrl());
        singleSignOutFilter.setFilter(customSingleSignOutFilter);

        Map<String, String> initParameters = new HashMap<>(1);
        initParameters.put("casServerUrlPrefix", configProps.getServerUrlPrefix());
        // by developer lemon start
        initParameters.put("artifactParameterName", this.configProps.getValidationType().name());
        initParameters.put("logoutParameterName", this.configProps.getLogoutParameterName());
        initParameters.put("logoutCallbackPath", this.configProps.getLogoutCallbackPath());

        if (!CollectionUtils.isEmpty(this.configProps.getLogoutFilterUrlPatterns())) {
            singleSignOutFilter.addUrlPatterns(this.configProps.getLogoutFilterUrlPatterns().toArray(new String[this.configProps.getLogoutFilterUrlPatterns().size()]));
        }
        // by developer lemon end
        singleSignOutFilter.setInitParameters(initParameters);
        singleSignOutFilter.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return singleSignOutFilter;
    }
}
