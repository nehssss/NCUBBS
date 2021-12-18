package com.haitaos.finallbbs.controller;

import com.haitaos.finallbbs.dto.AccessTokenDTO;
import com.haitaos.finallbbs.dto.GithubUser;
import com.haitaos.finallbbs.dto.UserDTO;
import com.haitaos.finallbbs.model.User;
import com.haitaos.finallbbs.provider.GithubProvider;
import com.haitaos.finallbbs.service.UserService;
import com.haitaos.finallbbs.utils.CookieUtils;
import com.haitaos.finallbbs.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


@Slf4j
@Controller
public class AuthorizeController {
    @Autowired
    private GithubProvider githubProvider;
    @Autowired
    private CookieUtils cookieUtils;

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.url}")
    private String redirectUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenUtils tokenUtils;



    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = cookieUtils.getCookie("token",null,0);
        response.addCookie(cookie);
        return "redirect:/index";
    }


    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletResponse response) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();

        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_url(redirectUrl);
        accessTokenDTO.setState(state);

        String accessToken = githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = githubProvider.getUser(accessToken);
        if (githubUser != null && githubUser.getId() != null) {
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setGmtModified(System.currentTimeMillis());
            user.setAvatarUrl("/images/avatar/" + (int) (Math.random() * 11) + ".jpg");
            user = userService.createOrUpdate(user);
            UserDTO userDTO = userService.getUserDTO(user);
            Cookie cookie = cookieUtils.getCookie("token",tokenUtils.getToken(userDTO),86400*3);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            return "redirect:/index";
        }
    }
}
