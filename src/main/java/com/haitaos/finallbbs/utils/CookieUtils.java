package com.haitaos.finallbbs.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;

@Component("CookieUtils")
public class CookieUtils {
    //@Value("${site.main.domain}")
    @Value("localhost:8080")
    private String domain;

    public Cookie getCookie(String key, String value, int time) {
        Cookie cookie = new Cookie(key, value);
//        cookie.setSecure(true);   //服务只能通过https来进行cookie的传递，使用http服务无法提供服务。
//        cookie.setHttpOnly(true);//通过js脚本是无法获取到cookie的信息的。防止XSS攻击。
        cookie.setMaxAge(time);//缩短为三天86400 * 3 * 1
        //cookie.setDomain(domain);
        cookie.setPath("/");
        return cookie;
    }
}
