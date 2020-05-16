package com.lemon.controller;

import org.jasig.cas.client.boot.configuration.CasClientConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author lemon
 * @description
 * @date 2020-05-16 15:50
 */
@Controller
public class CASController {
    @Autowired
    CasClientConfigurationProperties configProps;

    /**
     * 主页
     *
     * @param map
     * @return
     */
    @RequestMapping("/index")
    @ResponseBody
    public String index(ModelMap map) {
        map.addAttribute("name", "clien A");
        return "index";
    }

    /**
     * hello
     *
     * @return
     */
    @RequestMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }

    /**
     * 退出
     *
     * @param session
     * @return
     */
    @RequestMapping("/logoutOne")
    public String logoutOne(HttpSession session) {
        // 销毁session
        session.invalidate();
        // 使用cas退出成功后跳转到http://cas.client1.com:9001/logout/success
        return "redirect:" + configProps.getServerUrlPrefix() + "/logout";
    }

    /**
     * 退出
     *
     * @param session
     * @return
     */
    @RequestMapping("/logoutTwo")
    public String logoutTwo(HttpSession session) {
        // 销毁session
        session.invalidate();
        // 使用cas退出成功后跳转到http://cas.client1.com:9001/logout/success
        return "redirect:" + configProps.getServerUrlPrefix() + "/logout?service=" + configProps.getClientHostUrl()
                + configProps.getLogoutCallbackPath() + "?" + configProps.getLogoutParameterName()
                + "=" + System.currentTimeMillis();
    }

    /**
     * 退出成功页
     *
     * @param session
     * @return
     */
    @RequestMapping("/logout/success")
    @ResponseBody
    public String logoutsuccess(HttpSession session) {
        return "logout success";
    }
}