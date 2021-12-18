package com.haitaos.finallbbs.controller;

import com.haitaos.finallbbs.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/sso")
public class SSOController {

    @Value("${vaptcha.vid}")
    private String vaptcha_vid;
//    @Value("${sms.enable}")
    @Value("0")
    private Integer smsEnable;


    @RequestMapping("/{action}")
    public String aouth(HttpServletRequest request,
                        HttpServletResponse response,
                        @PathVariable(name = "action") String action,
                        Model model) {
        // System.out.println("请求"+request.getAttribute("isOk"));
        // if(isOk==null) return "redirect:/";
        //System.out.println(isOk);
        UserDTO user = (UserDTO)request.getAttribute("loginUser");
        if(user != null) {
            return "redirect:/index";
        }
        model.addAttribute("vaptcha_vid", vaptcha_vid);
        if("login".equals(action)){
            model.addAttribute("initOssType", 3);
            model.addAttribute("section", "login");
            model.addAttribute("sectionName", "登录");
            // return "/user/login";
        }
        else if("register".equals(action)){
            model.addAttribute("initOssType", 2);
            model.addAttribute("section", "register");
            model.addAttribute("sectionName", "注册");
            //  return "/user/reg";
        }
        else if("reset".equals(action)){
            model.addAttribute("initOssType", 2);
            model.addAttribute("section", "register");
            model.addAttribute("sectionName", "重置密码");
            //  return "/user/reg";
        }
        else {
            return "redirect:/index";
        }
        model.addAttribute("smsEnable", smsEnable);
        return "user/sso";
    }
}
