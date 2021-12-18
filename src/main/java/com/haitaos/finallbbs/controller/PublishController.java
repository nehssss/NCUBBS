package com.haitaos.finallbbs.controller;

import com.haitaos.finallbbs.annotation.UserLoginToken;
import com.haitaos.finallbbs.cache.TagCache;
import com.haitaos.finallbbs.dto.QuestionDTO;
import com.haitaos.finallbbs.dto.ResultDTO;
import com.haitaos.finallbbs.dto.UserDTO;
import com.haitaos.finallbbs.exception.CustomizeErrorCode;
import com.haitaos.finallbbs.exception.CustomizeException;
import com.haitaos.finallbbs.model.Question;
import com.haitaos.finallbbs.provider.BaiduCloudProvider;
import com.haitaos.finallbbs.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;



@Controller
public class PublishController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private BaiduCloudProvider baiduCloudProvider;

    @UserLoginToken
    @GetMapping("p/publish")
    public String publish2(Model model) {
        model.addAttribute("tags", TagCache.get());
        return "p/add";
    }

    @UserLoginToken
    @PostMapping("p/publish")
    public String doPublish2(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("tag") String tag,
            @RequestParam("column2") Integer column2,
            @RequestParam("permission") Integer permission,
            @RequestParam(value="id",defaultValue = "0") Long id,
            @RequestParam(value = "fileId", required = false) Long fileId,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "fileUrl", required = false) String fileUrl,
            HttpServletRequest request,
            Model model
    ){


        title = title.trim();
        tag = tag.trim();
        model.addAttribute("title",title);
        model.addAttribute("tag",tag);
        model.addAttribute("tags", TagCache.get());
        model.addAttribute("column2", column2);
        model.addAttribute("id", id);
        model.addAttribute("navtype", "publishnav");
        model.addAttribute("permission", permission);
        model.addAttribute("fileId",fileId);
        model.addAttribute("fileName",fileName);
        model.addAttribute("fileUrl",fileUrl);
        UserDTO user = (UserDTO)request.getAttribute("loginUser");

        if (StringUtils.isBlank(title)) {
            model.addAttribute("error", "标题不能为空");
            return "p/add";
        }
        if (description == null ) {
            model.addAttribute("error", "问题补充不能为空");
            return "p/add";
        }
        if (StringUtils.isBlank(tag)) {
            model.addAttribute("error", "标签不能为空");
            return "p/add";
        }
        //审核
        ResultDTO resultDTO = baiduCloudProvider.getTextCensorReult(questionService.getTextDescriptionFromHtml(title+description+tag));
        if(resultDTO.getCode()!=1){
            model.addAttribute("error",resultDTO.getMessage());
            model.addAttribute("description", description);
            return "p/add";
        }


        Question question = new Question();
        question.setPermission(permission);
        question.setColumn2(column2);
        question.setTitle(title);
        question.setDescription(description);
        question.setTag(tag);
        question.setCreator(user.getId());
        question.setId(id);
        questionService.createOrUpdate(question,user,fileId);
        return "redirect:/index";
    }

    @GetMapping("p/publish/{id}")
    public String edit2(@PathVariable(name = "id") Long id,
                        Model model,
                        HttpServletRequest request){
        QuestionDTO question = questionService.getById(id,0L);
        UserDTO user = (UserDTO)request.getAttribute("loginUser");
        if (question.getCreator().longValue() != user.getId().longValue() ){
            throw new CustomizeException(CustomizeErrorCode.CAN_NOT_EDIT_QUESTION);
        }
        model.addAttribute("title", question.getTitle());
        model.addAttribute("description", question.getDescription());
        model.addAttribute("column2", question.getColumn2());
        model.addAttribute("tag", question.getTag());
        model.addAttribute("id", question.getId());
        model.addAttribute("tags", TagCache.get());
        model.addAttribute("navtype", "publishnav");
        if(question.getProject()!=null) {
            model.addAttribute("fileId", question.getProject().getId());
            model.addAttribute("fileName", question.getProject().getFileName());
            model.addAttribute("fileUrl", question.getProject().getFileUrl());
        }
        else {
            model.addAttribute("fileId", null);
            model.addAttribute("fileName", null);
            model.addAttribute("fileUrl", null);
        }
        return "p/add";
    }








}
