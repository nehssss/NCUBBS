package com.haitaos.finallbbs.controller;


import com.haitaos.finallbbs.cache.HotTagCache;
import com.haitaos.finallbbs.cache.LoginUserCache;
import com.haitaos.finallbbs.dto.HotTagDTO;
import com.haitaos.finallbbs.dto.PaginationDTO;
import com.haitaos.finallbbs.dto.QuestionDTO;
import com.haitaos.finallbbs.dto.UserDTO;
import com.haitaos.finallbbs.model.Question;
import com.haitaos.finallbbs.model.User;
import com.haitaos.finallbbs.model.UserAccount;
import com.haitaos.finallbbs.service.QuestionService;
import com.mysql.cj.xdevapi.Column;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class HomeController {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private LoginUserCache loginUserCache;
    @Autowired
    private HotTagCache hotTagCache;


    @GetMapping(value = {"/show"})
    public String forum(HttpServletRequest request,
                        Model model,
                        @RequestParam(name = "page",defaultValue = "1")Integer page,
                        @RequestParam(name = "size",defaultValue = "15")Integer size,
                        @RequestParam(name = "column", required = false) Integer column2,
                        @RequestParam(name = "search", required = false) String search,
                        @RequestParam(name = "tag", required = false) String tag,
                        @RequestParam(name = "sort", required = false) String sort) {
        UserDTO loginuser = (UserDTO) request.getAttribute("loginUser");
        UserAccount userAccount =null;
        if(loginuser!=null){
            userAccount = new UserAccount();
            BeanUtils.copyProperties(loginuser,userAccount);
            userAccount.setUserId(loginuser.getId());
        }
        List<QuestionDTO> topQuestions = questionService.listTopwithColumn(search, tag, sort,column2);
        PaginationDTO pagination = questionService.listwithColumn(search, tag, sort, page,size,column2,userAccount);
        List<String> tags = hotTagCache.getHots();
        List<User> loginUsers = loginUserCache.getLoginUsers();
        Map<String, String> catalog=new HashMap<>();

        if(column2!=null) {
            if (column2 == 1) {
                catalog.put("catalogName", "前端开发");
                catalog.put("catalogDesc", "HTML5/Vue.js/Node.js");
                catalog.put("catalogImg", "../assets/images/Column1.png");
                catalog.put("catalogType", column2.toString());
            } else if (column2 == 2) {
                catalog.put("catalogName", "后端开发");
                catalog.put("catalogDesc", "Java/Python/Go");
                catalog.put("catalogImg", "../assets/images/Column2.png");
                catalog.put("catalogType", column2.toString());
            } else if (column2 == 3) {
                catalog.put("catalogName", "移动开发");
                catalog.put("catalogDesc", "Flutter/Android/iOS");
                catalog.put("catalogImg", "../assets/images/Column3.png");
                catalog.put("catalogType", column2.toString());
            } else if (column2 == 4) {
                catalog.put("catalogName", "计算机基础");
                catalog.put("catalogDesc", "算法/数学/数据库");
                catalog.put("catalogImg", "../assets/images/Column4.png");
                catalog.put("catalogType", column2.toString());
            } else if (column2 == 5) {
                catalog.put("catalogName", "前沿技术");
                catalog.put("catalogDesc", "AI/大数据/数据分析");
                catalog.put("catalogImg", "../assets/images/Column5.png");
                catalog.put("catalogType", column2.toString());
            } else if (column2 == 6) {
                catalog.put("catalogName", "测试运维");
                catalog.put("catalogDesc", "自动化测试/容器");
                catalog.put("catalogImg", "../assets/images/Column6.png");
                catalog.put("catalogType", column2.toString());
            } else if (column2 == 7) {
                catalog.put("catalogName", "更多方向");
                catalog.put("catalogDesc", "产品设计/UI设计/游戏");
                catalog.put("catalogImg", "../assets/images/Column7.png");
                catalog.put("catalogType", column2.toString());
            } else {
                catalog.put("catalogName", null);
                catalog.put("catalogDesc", null);
                catalog.put("catalogImg", null);
                catalog.put("catalogType", null);
            }
        }else{
            catalog.put("catalogName", null);
            catalog.put("catalogDesc", null);
            catalog.put("catalogImg", null);
            catalog.put("catalogType", null);

        }



        model.addAttribute("loginUsers", loginUsers);
        model.addAttribute("pagination",pagination);
        model.addAttribute("search", search);
        model.addAttribute("tag", tag);
        model.addAttribute("tags", tags);
        model.addAttribute("sort", sort);
        model.addAttribute("column", column2);
        model.addAttribute("topQuestions", topQuestions);
        model.addAttribute("navtype", "communitynav");
        model.addAttribute("catalog",catalog);
        return "show";
    }

    @GetMapping({"/","index"})
    public String index(HttpServletRequest request, HttpServletResponse response,
                        Model model){
        Map<String,Object> mapListPost= questionService.mapListPosts(6);
        model.addAttribute("mapListPost",mapListPost);
        for(int i=1;i<=7;i++){
            String digit= "Column"+(i);
            long count=questionService.getColumnCount(i);
            model.addAttribute(digit,count);
        }
        System.out.println(response);
        return "index";
    }



}

