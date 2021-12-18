package com.haitaos.finallbbs.intercepter;






//import cn.niter.forum.annotation.UserLoginToken;
//import cn.niter.forum.cache.LoginUserCache;
//import cn.niter.forum.dto.ResultDTO;
//import cn.niter.forum.dto.UserDTO;
//import cn.niter.forum.exception.CustomizeErrorCode;
//import cn.niter.forum.exception.CustomizeException;
//import cn.niter.forum.mapper.UserAccountMapper;
//import cn.niter.forum.mapper.UserMapper;
//import cn.niter.forum.service.AdService;
//import cn.niter.forum.util.TokenUtils;
import com.haitaos.finallbbs.annotation.UserLoginToken;
import com.haitaos.finallbbs.cache.LoginUserCache;
import com.haitaos.finallbbs.dto.ResultDTO;
import com.haitaos.finallbbs.dto.UserDTO;
import com.haitaos.finallbbs.exception.CustomizeErrorCode;
import com.haitaos.finallbbs.exception.CustomizeException;
import com.haitaos.finallbbs.mapper.UserAccountMapper;
import com.haitaos.finallbbs.mapper.UserMapper;
import com.haitaos.finallbbs.service.NotificationService;
import com.haitaos.finallbbs.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author wadao
 * @version 2.0
 * @date 2020/5/1 15:17
 * @site niter.cn
 */
@Service
public class SessionInterceptor implements HandlerInterceptor {
//    @Autowired
//    private UserMapper userMapper;
//    @Autowired
//    private UserAccountMapper userAccountMapper;
    @Autowired
    private LoginUserCache loginUserCache;
//    @Autowired
//    private AdService adService;
    @Autowired
    TokenUtils tokenUtils;

    @Autowired
    NotificationService notificationService;
   /* @Value("${github.redirect.uri}")
    private String redirectUri;
    @Value("${baidu.redirect.uri}")
    private String baiduRedirectUri;
    @Value("${weibo.redirect.uri}")
    private String weiboRedirectUri;
    @Value("${qq.redirect.uri}")
    private String qqRedirectUri;*/

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String referer = request.getHeader("referer");//告知服务器请求的原始资源的URI，其用于所有类型的请求，并且包括：协议+域名+查询参数（注意，不包含锚点信息）。因为原始的URI中的查询参数可能包含ID或密码等敏感信息，如果写入referer，则可能导致信息泄露。

        String host = request.getHeader("host");//客户端指定自己想访问的WEB服务器的域名/IP 地址和端口号。 在任何类型请求中，request都会包含此header信息。
        //处理静态资源
        if (handler instanceof ResourceHttpRequestHandler){
            if(referer!=null&&(!host.equals(referer.split("//")[1].split("/")[0]))){//静态资源防盗链
                response.setStatus(403);
                return false;
            }
            return true;
        }

        HandlerMethod handlerMethod=(HandlerMethod)handler;
        Method method=handlerMethod.getMethod();
        String token=null;
        ResultDTO resultDTO=null;
        Cookie[] cookies = request.getCookies();
        boolean hashToken = false;
        if(cookies!=null&&cookies.length!=0){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("token")){
                    token=cookie.getValue();
                    if(token!=null) {
                        hashToken=true;
                        resultDTO = tokenUtils.verifyToken(token);
                        if(resultDTO.getCode()==200){
                            UserDTO userDTO = (UserDTO) resultDTO.getData();
                            request.setAttribute("loginUser",userDTO);
                   
                            loginUserCache.putLoginUser(userDTO.getId(),System.currentTimeMillis());
                            Long unreadCount = notificationService.unreadCount(userDTO.getId());
                            request.getSession().setAttribute("unread", unreadCount);
                            //return true;
                        }
                    }

                    break;
                }
            }
        }


        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                // 执行认证
                if ((!hashToken)||resultDTO.getCode()!=200) {
                    throw new CustomizeException(CustomizeErrorCode.NO_LOGIN);
                }
            }
        }



        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }
}
