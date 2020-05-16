package org.jasig.cas.client.boot.authentication;

import lombok.Data;

/**
 * @author lemon
 * @description
 * @date 2020-05-09 18:13
 */
@Data
public class AjaxAuthenticationRedirect {
    /**
     * 响应码
     */
    private int responseStatus;

    /**
     * 响应内容类型
     */
    private String responseContentType;

    /**
     * 响应内容
     */
    private String responseContent;

    /**
     * 请求头或请求中包含指定参数名返回响应报文
     */
    private String parameterName;

    private String redirectUrlPlaceHolder;
}