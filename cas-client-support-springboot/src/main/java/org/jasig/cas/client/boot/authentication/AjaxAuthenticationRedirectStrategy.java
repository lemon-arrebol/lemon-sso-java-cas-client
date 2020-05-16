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
package org.jasig.cas.client.boot.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AuthenticationRedirectStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Implementation of the redirect strategy that can handle a Faces Ajax request in addition to the standard redirect style.
 *
 * @author Scott Battaglia
 * @since 3.3.0
 */
@Slf4j
public final class AjaxAuthenticationRedirectStrategy implements AuthenticationRedirectStrategy {
    /**
     * by develoepr lemon ajax请求响应
     */
    private AjaxAuthenticationRedirect ajaxFilter;

    public AjaxAuthenticationRedirectStrategy(AjaxAuthenticationRedirect ajaxFilter) {
        this.ajaxFilter = ajaxFilter;
    }

    @Override
    public void redirect(final HttpServletRequest request, final HttpServletResponse response,
                         final String potentialRedirectUrl) throws IOException {
        // Ajax请求或者返回指定格式报文
        boolean noRedirect = (StringUtils.isNotBlank(request.getHeader("x-requested-with")) && "XMLHttpRequest".equals(request.getHeader("x-requested-with")))
                || StringUtils.isNotBlank(request.getParameter(ajaxFilter.getParameterName()))
                || StringUtils.isNotBlank(request.getHeader(ajaxFilter.getParameterName()));

        if (noRedirect) {
            if(log.isDebugEnabled()) {
                log.debug("ajax request or response data to {}", potentialRedirectUrl);
            }

            // this is an ajax request - redirect ajaxly
            response.setContentType(this.ajaxFilter.getResponseContentType());
            response.setStatus(this.ajaxFilter.getResponseStatus());

            final PrintWriter writer = response.getWriter();

            if (StringUtils.isNotBlank(this.ajaxFilter.getRedirectUrlPlaceHolder())) {
                writer.write(this.ajaxFilter.getResponseContent().replace(this.ajaxFilter.getRedirectUrlPlaceHolder(), potentialRedirectUrl));
            } else {
                writer.write(String.format(this.ajaxFilter.getResponseContent(), potentialRedirectUrl));
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug("redirect to {}", potentialRedirectUrl);
            }

            response.sendRedirect(potentialRedirectUrl);
        }
    }

    public AjaxAuthenticationRedirect getAjaxFilter() {
        return ajaxFilter;
    }

    public void setAjaxFilter(AjaxAuthenticationRedirect ajaxFilter) {
        this.ajaxFilter = ajaxFilter;
    }
}
