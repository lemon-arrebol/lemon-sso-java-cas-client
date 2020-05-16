package org.jasig.cas.client.boot.mdc;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * @author lemon
 * @description logback 日志注入参数
 * @date 2020-05-09 15:59
 */
public class LogMdcFilter implements Filter {

    private static final String UNKNOWN = "unknown";

    /**
     * @param filterConfig
     * @return void
     * @description
     * @author lemon
     * @date 2020-05-09 15:59
     */
    @Override
    public void init(FilterConfig filterConfig) {
    }

    /**
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @return void
     * @description
     * @author lemon
     * @date 2020-05-09 15:59
     */
    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            this.insertIntoMDC(servletRequest);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            this.clearMDC();
        }
    }

    /**
     * @param
     * @return void
     * @description
     * @author lemon
     * @date 2020-05-09 15:59
     */
    @Override
    public void destroy() {
    }

    /**
     * @param request
     * @return void
     * @description
     * @author lemon
     * @date 2020-05-09 15:59
     */
    synchronized void insertIntoMDC(ServletRequest request) {
        MDC.put("request.id", UUID.randomUUID().toString());
        MDC.put("req.remoteHost", LogMdcFilter.getIpAddress((HttpServletRequest) request));

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            MDC.put("req.requestURI", httpServletRequest.getRequestURI());
            StringBuffer requestURL = httpServletRequest.getRequestURL();

            if (requestURL != null) {
                MDC.put("req.requestURL", requestURL.toString());
            }

            MDC.put("req.method", httpServletRequest.getMethod());
            MDC.put("req.queryString", httpServletRequest.getQueryString());
            MDC.put("req.userAgent", httpServletRequest.getHeader("User-Agent"));
            MDC.put("req.xForwardedFor", httpServletRequest.getHeader("X-Forwarded-For"));
        }

    }

    /**
     * @description
     * @author Mcdull
     * @date 2018/10/19
     */
    void clearMDC() {
        MDC.remove("request.id");
        MDC.remove("req.remoteHost");
        MDC.remove("req.requestURI");
        MDC.remove("req.queryString");
        MDC.remove("req.requestURL");
        MDC.remove("req.method");
        MDC.remove("req.userAgent");
        MDC.remove("req.xForwardedFor");
    }

    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * <p>
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？ 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * <p>
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
     * <p>
     * 用户真实IP为： 192.168.1.110
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if (LogMdcFilter.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (LogMdcFilter.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (LogMdcFilter.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        if (LogMdcFilter.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (LogMdcFilter.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (LogMdcFilter.isBlank(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多次反向代理后会有多个ip值，第一个ip才是真实ip
        int index = ip.indexOf(',');

        if (index != -1) {
            String[] ipArray = ip.split(",");

            for (int i = 0; i < ipArray.length; i++) {
                if (!UNKNOWN.equalsIgnoreCase(ipArray[i].trim())) {
                    ip = ipArray[i].trim();
                    break;
                }
            }
        }

        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            ip = ServerIpConverter.getServerIp();
        }

        return ip;
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }
}
