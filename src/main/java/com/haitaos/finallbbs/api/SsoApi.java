package com.haitaos.finallbbs.api;


import com.haitaos.finallbbs.annotation.UserLoginToken;
import com.haitaos.finallbbs.cache.IpLimitCache;
import com.haitaos.finallbbs.cache.TemporaryCache;
import com.haitaos.finallbbs.dto.ResultDTO;
import com.haitaos.finallbbs.service.UserService;
import com.haitaos.finallbbs.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/sso")
@ApiIgnore
public class SsoApi {

    @Autowired
    private UserService userService;
    @Autowired
    private CookieUtils cookieUtils;
    @Autowired
    private TemporaryCache temporaryCache;
    @Autowired
    private IpLimitCache ipLimitCache;

    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Object login(HttpServletRequest request,
                        @RequestParam("name") String name,
                        @RequestParam("password") String password,
                        @RequestParam("type") Integer type,
                        HttpServletResponse response) {
        //1为手机号，2为邮箱号
        ResultDTO resultDTO = (ResultDTO)userService.login(type,name,password);
        if(200==resultDTO.getCode()){
            Cookie cookie = cookieUtils.getCookie("token",""+resultDTO.getData(),86400*3);
            response.addCookie(cookie);
        }
        return resultDTO;
    }

    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public Object register(HttpServletRequest request,
                           @RequestParam("name") String name,
                           @RequestParam("password") String password,
                           @RequestParam("type") Integer type,
                           HttpServletResponse response) {
        //1为手机号，2为邮箱号
        ResultDTO resultDTO = (ResultDTO)userService.register(type,name,password);
        if(200==resultDTO.getCode()){
            Cookie cookie = cookieUtils.getCookie("token",""+resultDTO.getData(),86400*3);
            response.addCookie(cookie);
        }
        return resultDTO;
    }

    @UserLoginToken
    @ResponseBody//@ResponseBody返回json格式的数据
    @RequestMapping(value = "/mail/submitMail", method = RequestMethod.POST)
    public Object submitMail(@RequestParam("id") String id,
                             @RequestParam("mail") String mail,
                             @RequestParam("code") String code) {
        System.out.println("mail code"+mail+code);
        if (!code.equals(temporaryCache.getMailCode(mail)))
            return ResultDTO.errorOf("验证码不匹配，可能已经超过5分钟，请重试");        // TODO 自动生成的方法存根
        return userService.updateUserMailById(id, mail);
    }

    @ResponseBody//@ResponseBody返回json格式的数据
    @PostMapping("/mail/registerOrLoginWithMail")
    public Object registerOrLoginWithMail(
            @RequestParam("mail") String mail,
            @RequestParam("code") String code,
            @RequestParam(name = "password", required = false) String password,
            HttpServletResponse response) {
        if (!code.equals(temporaryCache.getMailCode(mail)))
            return ResultDTO.errorOf("验证码不匹配，可能已经超过5分钟，请重试");
        if("null".equals(password)) password=null;
        ResultDTO resultDTO = (ResultDTO)userService.registerOrLoginWithMail(mail,password);
        if(200==resultDTO.getCode()){
            Cookie cookie = cookieUtils.getCookie("token",resultDTO.getMessage(),86400*3);
            response.addCookie(cookie);
        }
        // TODO 自动生成的方法存根
        return resultDTO;
    }
}
