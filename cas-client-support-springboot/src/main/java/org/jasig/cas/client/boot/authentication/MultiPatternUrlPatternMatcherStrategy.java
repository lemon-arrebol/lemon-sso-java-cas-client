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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.UrlPatternMatcherStrategy;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * @author lemon
 * @description 自定义请求忽略规则
 * @date 2020-05-09 21:12
 */
@Slf4j
public final class MultiPatternUrlPatternMatcherStrategy implements UrlPatternMatcherStrategy {

    private String[] patterns;

    private PathMatcher pathMarch = new AntPathMatcher();

    @Override
    public boolean matches(final String url) {
        if (log.isDebugEnabled()) {
            log.debug("Request url is {}, patterns is {}", url, StringUtils.join(this.patterns, ","));
        }

        if (ArrayUtils.isEmpty(this.patterns)) {
            return false;
        }

        for (String pattern : this.patterns) {
            if (this.pathMarch.match(pattern, url) || this.pathMarch.match(pattern, url.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setPattern(final String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            this.patterns = pattern.split(",");

            for (int i = 0; i < this.patterns.length; i++) {
                this.patterns[i] = this.patterns[i].trim();

                if (this.patterns[i].startsWith("/")) {
                    this.patterns[i] = "**" + this.patterns[i];
                } else if (!this.patterns[i].startsWith("**")) {
                    this.patterns[i] = "**/" + this.patterns[i];
                }
            }
        }
    }
}
