package com.haitaos.finallbbs.admin.controller;


import com.haitaos.finallbbs.annotation.UserLoginToken;
import com.haitaos.finallbbs.dto.*;
import com.haitaos.finallbbs.enums.CommentTypeEnum;
import com.haitaos.finallbbs.mapper.UserAccountMapper;
import com.haitaos.finallbbs.mapper.UserMapper;
import com.haitaos.finallbbs.model.Comment;
import com.haitaos.finallbbs.model.User;
import com.haitaos.finallbbs.model.UserAccount;
import com.haitaos.finallbbs.model.UserAccountExample;
import com.haitaos.finallbbs.service.CommentService;
import com.haitaos.finallbbs.service.LikeService;
import com.haitaos.finallbbs.service.QuestionService;
import com.haitaos.finallbbs.service.UserService;
import com.haitaos.finallbbs.utils.TimeUtils;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@UserLoginToken
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    UserService userService;

    @Autowired
    UserAccountMapper userAccountMapper;

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    @Autowired
    TimeUtils timeUtils;

    @Autowired
    LikeService likeService;

    @Autowired
    private Environment env;

    @Value("${vaptcha.vid}")
    private String vaptcha_vid;

    @GetMapping("/index")
    public String index(HttpServletRequest request){
        UserDTO user = (UserDTO)request.getAttribute("loginUser");
        if (user.getGroupId()<3) return "index";
        return "admin/index";
    }

    @GetMapping("/welcome")
    public String welcome(){
        return "admin/welcome";
    }

    @GetMapping("/welcome1")
    public String welcome1(){
        return "admin/welcome1";
    }

    @GetMapping("/member-list")
    public String memberList(HttpServletRequest request,
                             Model model,
                             @RequestParam(name = "page",defaultValue = "1")Integer page,
                             @RequestParam(name = "size",defaultValue = "6")Integer size,
                             @RequestParam(name = "search", required = false) String search,
                             @RequestParam(name = "start",required = false) String start,
                             @RequestParam(name = "end" ,required = false)String end){
        UserDTO user = (UserDTO)request.getAttribute("loginUser");
        PaginationDTO paginationDTO=userService.listwithColumn(search, page,size,start,end,user);
        model.addAttribute("pagination",paginationDTO);
        return "admin/member-list";
    }


    @GetMapping("/member-edit")
    public String memberEdit(HttpServletRequest request,
                             Model model,
                             @RequestParam(name = "userid")Long userid){
        UserDTO userDTO=userService.selectUserInfo(userid);
        model.addAttribute("userInfo",userDTO);
        return "admin/member-edit";
    }

    @PostMapping("/member/update")
    public String updateStatus(HttpServletRequest request,Model model,@RequestParam(name = "userid") Long  userId,@RequestParam("groupId") Integer gourId){
        UserAccount userAccount=new UserAccount();
        userAccount.setGroupId(gourId);
        UserAccountExample userAccountExample=new UserAccountExample();
        userAccountExample.createCriteria()
                .andUserIdEqualTo(userId);
        userAccountMapper.updateByExampleSelective(userAccount,userAccountExample);


     return "redirect:/admin/member-edit?userid="+userId;

    }


   @PostMapping("/member/del")
   @ResponseBody
    public ResultDTO delUser(HttpServletRequest request, Model model, @RequestParam("id") Long userId){
        log.info(userId.toString());
        userService.delUser(userId);
        questionService.delQuestion(userId);
        return ResultDTO.okOf("删除成功");
   }


    @PostMapping("/member/delAll")
    @ResponseBody
    public ResultDTO delAllUser(HttpServletRequest request, Model model,String[] ids){


        for (String userId: ids) {

            userService.delUser(Long.valueOf(userId));
            questionService.delQuestion(Long.valueOf(userId));
        }



        return ResultDTO.okOf("删除成功");
    }


    @GetMapping("/question-list")
    public String questionList(HttpServletRequest request,
                        Model model,
                        @RequestParam(name = "page",defaultValue = "1")Integer page,
                        @RequestParam(name = "size",defaultValue = "15")Integer size,
                        @RequestParam(name = "search", required = false) String search, @RequestParam(name = "start",required = false) String start, @RequestParam(name = "end" ,required = false)String end
                        ) {


        PaginationDTO pagination = questionService.listwithColumn(search, page,size,start,end);

        model.addAttribute("pagination",pagination);
        return "admin/question-list";
    }


    @GetMapping(value = {"/question-detail/{id}"})
    public String po(@PathVariable(name = "id") Long id, HttpServletRequest request,Model model,@RequestParam(name = "to",required = false)String to){
        UserDTO viewUser = (UserDTO)request.getAttribute("loginUser");
        Long viewUser_id;
        if(viewUser==null){
            viewUser_id=0L;
        }
        else viewUser_id=viewUser.getId();
        QuestionDTO questionDTO = questionService.getById(id,viewUser_id);
        //UserAccount userAccount = (UserAccount)request.getSession().getAttribute("userAccount");
        if(viewUser==null){
            if(questionDTO.getPermission()!=0){
                model.addAttribute("rsTitle", "您无权访问！");
                model.addAttribute("rsMessage", "该贴设置了权限，游客不可见，快去登录吧");
                return "result";
            }
        }
        else if(questionDTO.getPermission()>viewUser.getGroupId()&&questionDTO.getCreator()!=viewUser.getId()){
            model.addAttribute("rsTitle", "您无权访问！");
            model.addAttribute("rsMessage", "该贴仅作者和"+env.getProperty("user.group.r"+questionDTO.getPermission())+"及以上的用户可以访问");
            return "result";
        }
        List<QuestionDTO> relatedQuestions = questionService.selectRelated(questionDTO);
        List<CommentDTO> comments = commentService.listByTargetId(id, CommentTypeEnum.QUESTION);
        for ( CommentDTO c: comments
        ) {
            c.setLikeStaus(likeService.searchExist(c.getId(),viewUser_id));
        }
        //累加阅读数
        questionService.incView(id);
        //这里提取简短描述
        String description=questionDTO.getDescription();
        String textDescription = description.replaceAll("</?[^>]+>", ""); //剔出<html>的标签
        textDescription = textDescription.replaceAll("<a>\\s*|\t|\r|\n</a>", "");//去除字符串中的空格,回车,换行符,制表符
        textDescription = textDescription.replaceAll("&nbsp;", "");//去除&nbsp;
        if(textDescription.length()>100) textDescription=textDescription.substring(0,100);
        //System.out.println(textDescription);
        model.addAttribute("textDescription", questionDTO.getTitle()+".."+textDescription);
        model.addAttribute("question", questionDTO);
        model.addAttribute("comments", comments);
        model.addAttribute("relatedQuestions", relatedQuestions);
        model.addAttribute("navtype", "communitynav");
        model.addAttribute("vaptcha_vid", vaptcha_vid);
        if("article".equals(to)) return "home/detail";
        return "admin/question-details";
    }
    @PostMapping("/member/main1")
    @ResponseBody
    public Object countRegister(){
        Map<String,Object> map=new HashMap<String, Object>();
        ArrayList<Long> arrs=userService.countRegister();
        map.put("register",arrs);
        return map;
    }


    @PostMapping("/member/main2")
    @ResponseBody
    public Object countQuestion(){
        Map<String,Object> map=new HashMap<String, Object>();
        ArrayList<Long> arrs=questionService.countQuestion();
        map.put("question",arrs);
        return map;
    }


    @PostMapping("/member/main3")
    @ResponseBody
    public Object countSex(){
        Map<String,Object> map=new HashMap<String, Object>();
        ArrayList<Long> arrs=userService.countSex();
        Long arr=arrs.get(0);
            Map<String, Object> map1=new HashMap<String, Object>();
            map1.put("value",arr);
            map1.put("name","男性");
       Long arr2=arrs.get(1);
        Map<String, Object> map2=new HashMap<String, Object>();
        map2.put("value",arr2);
        map2.put("name","女性");
        map.put("male",map1);
        map.put("female",map2);
        return map;
    }


    @PostMapping("/member/main4")
    @ResponseBody
    public Object countEducation(){
        Map<String,Object> map=new HashMap<String, Object>();
        ArrayList<Long> arrs=userService.countEducation();
        Long arr=arrs.get(0);
        Map<String, Object> map1=new HashMap<String, Object>();
        map1.put("value",arr);
        map1.put("name","小学");
        Long arr2=arrs.get(1);
        Map<String, Object> map2=new HashMap<String, Object>();
        map2.put("value",arr2);
        map2.put("name","初中");
        Long arr3=arrs.get(2);
        Map<String, Object> map3=new HashMap<String, Object>();
        map3.put("value",arr3);
        map3.put("name","高中");

        Long arr4=arrs.get(3);
        Map<String, Object> map4=new HashMap<String, Object>();
        map4.put("value",arr4);
        map4.put("name","大学及以上");


        map.put("edu1",map1);
        map.put("edu2",map2);
        map.put("edu3",map3);
        map.put("edu4",map4);
        return map;
    }








}
