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
package org.jasig.cas.client.boot.logout;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;
import org.jasig.cas.client.authentication.DefaultAuthenticationRedirectStrategy;
import org.jasig.cas.client.configuration.ConfigurationKeys;
import org.jasig.cas.client.session.SessionMappingStorage;
import org.jasig.cas.client.util.AbstractConfigurationFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the Single Sign Out protocol.  It handles registering the session and destroying the session.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
@Slf4j
public final class CustomSingleSignOutFilter extends AbstractConfigurationFilter {
    @Setter
    private String clientHomePageUrl;

    private static final CustomSingleSignOutHandler HANDLER = new CustomSingleSignOutHandler();

    private final AtomicBoolean handlerInitialized = new AtomicBoolean(false);

    @Setter
    private AuthenticationRedirectStrategy redirectStrategy = new DefaultAuthenticationRedirectStrategy();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        if (!isIgnoreInitConfiguration()) {
            setArtifactParameterName(getString(ConfigurationKeys.ARTIFACT_PARAMETER_NAME));
            setLogoutParameterName(getString(ConfigurationKeys.LOGOUT_PARAMETER_NAME));
            setRelayStateParameterName(getString(ConfigurationKeys.RELAY_STATE_PARAMETER_NAME));
            setLogoutCallbackPath(getString(ConfigurationKeys.LOGOUT_CALLBACK_PATH));
            HANDLER.setArtifactParameterOverPost(getBoolean(ConfigurationKeys.ARTIFACT_PARAMETER_OVER_POST));
            HANDLER.setEagerlyCreateSessions(getBoolean(ConfigurationKeys.EAGERLY_CREATE_SESSIONS));
        }
        HANDLER.init();
        handlerInitialized.set(true);
    }

    public void setArtifactParameterName(final String name) {
        HANDLER.setArtifactParameterName(name);
    }

    public void setLogoutParameterName(final String name) {
        HANDLER.setLogoutParameterName(name);
    }

    public void setRelayStateParameterName(final String name) {
        HANDLER.setRelayStateParameterName(name);
    }

    public void setLogoutCallbackPath(final String logoutCallbackPath) {
        HANDLER.setLogoutCallbackPath(logoutCallbackPath);
    }

    public void setSessionMappingStorage(final SessionMappingStorage storage) {
        HANDLER.setSessionMappingStorage(storage);
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        /**
         * <p>Workaround for now for the fact that Spring Security will fail since it doesn't call {@link #init(javax.servlet.FilterConfig)}.</p>
         * <p>Ultimately we need to allow deployers to actually inject their fully-initialized {@link org.jasig.cas.client.session.SingleSignOutHandler}.</p>
         */
        if (!this.handlerInitialized.getAndSet(true)) {
            HANDLER.init();
        }

        // by developer lemon SLO请求返回false，需要重定向要登录页面，需要兼容Ajax请求
        if (HANDLER.process(request, response)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // 退出请求当前未登录或者已经退出重定向到系统首页
            String redirectUrl = this.clientHomePageUrl;
            // by developer lemon
            if(StringUtils.isBlank(redirectUrl)) {
                redirectUrl = String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
            }

            if(log.isDebugEnabled()) {
                log.debug("clientHomePageUrl is {}, redirectUrl is {}", this.clientHomePageUrl, redirectUrl);
            }

            this.redirectStrategy.redirect(request, response, redirectUrl);
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
