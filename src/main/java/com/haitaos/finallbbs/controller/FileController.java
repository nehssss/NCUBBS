package com.haitaos.finallbbs.controller;




import com.haitaos.finallbbs.annotation.UserLoginToken;
import com.haitaos.finallbbs.dto.FileDTO;
import com.haitaos.finallbbs.dto.ResultDTO;
import com.haitaos.finallbbs.dto.UserDTO;
import com.haitaos.finallbbs.model.Project;
import com.haitaos.finallbbs.provider.QCloudProvider;
import com.haitaos.finallbbs.service.ProjectService;
import com.haitaos.finallbbs.utils.CookieUtils;
import com.haitaos.finallbbs.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@Slf4j
public class FileController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private QCloudProvider qCloudProvider;
    @Autowired
    private CookieUtils cookieUtils;
    @Autowired
    private TokenUtils tokenUtils;

    @UserLoginToken
    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    @ResponseBody
    public FileDTO upload(HttpServletRequest request,
                          @RequestParam("editormd-image-file") List<MultipartFile> multipartFiles) {

        // MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //MultipartFile file = multipartRequest.getFile("up-file8054188074344137");
        System.out.println(multipartFiles);

        try {

            UserDTO user = (UserDTO)request.getAttribute("loginUser");
           /* InputStream inputStream = multipartFile.getInputStream();
            String contentType = multipartFile.getContentType();
            String filename = getFileName(contentType);
            Long contentLength = multipartFile.getSize();
            System.out.println("类型："+multipartFile.getContentType()+"名字："+user.getId()+"/"+filename);
            String url = qCloudProvider.upload(inputStream,contentType,user.getId()+"/"+filename,contentLength);
            String[] data ={url};*/
            InputStream inputStream;
            String contentType;
            String filename;
            Long contentLength;
            String[] data = new String[multipartFiles.size()];
            int count = 0;
            for (MultipartFile multipartFile : multipartFiles){
                inputStream = multipartFile.getInputStream();
                contentType = multipartFile.getContentType();
                filename = getFileName(contentType);
                contentLength = multipartFile.getSize();
                //System.out.println("原始名字"+multipartFile.getOriginalFilename()+"类型："+multipartFile.getContentType()+"名字："+"upload/user/"+user.getId()+"/"+filename);
                String url = qCloudProvider.upload(inputStream,contentType,user,filename,contentLength);
                data[count] = url;
                count++;
            }
            FileDTO fileDTO = new FileDTO();
            fileDTO.setSuccess(1);
            fileDTO.setUrl(data);
            return fileDTO;
        } catch (Exception e) {
            log.error("upload error", e);
            FileDTO fileDTO = new FileDTO();
            fileDTO.setSuccess(0);
            // fileDTO.setMessage("上传失败");
            return fileDTO;
        }
    }

    //图片上传接口
    @UserLoginToken
    @PostMapping("/file/layUpload")
    @ResponseBody
    public Map<String,Object> uploadLayImage(HttpServletRequest request,@RequestParam("file") MultipartFile file) {
        Map<String,Object> map  = new HashMap<>();
        // String uploadDir = "F:/kdgc_project/Student_Attendence_Application/src/main/webapp/resources/upload/";
        try {
            UserDTO user = (UserDTO)request.getAttribute("loginUser");
           /* if(user==null){
                map.put("code",500);
                map.put("msg","请登录后再上传图片哦");
                map.put("data",null);
                return map;
            }*/
            InputStream inputStream;
            String contentType;
            String filename;
            Long contentLength;
            inputStream = file.getInputStream();
            contentType = file.getContentType();
            filename = getFileName(contentType);
            contentLength = file.getSize();
            //System.out.println("原始名字"+file.getOriginalFilename()+"类型："+file.getContentType()+"名字："+"upload/user/"+user.getId()+"/"+filename);
            // String url = qCloudProvider.upload(inputStream,contentType,"upload/user/"+user.getId()+"/"+filename,contentLength);
            String url = qCloudProvider.upload(inputStream,contentType,user,filename,contentLength);

            map.put("code",0);
            map.put("msg","");
            map.put("data",url);
            // System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("code",500);
            map.put("msg","上传失败");
            map.put("data",null);
            return map;
        }
    }

    //人脸头像上传接口
    @UserLoginToken
    @PostMapping("/file/avatar")
    @ResponseBody
    public Map<String,Object> uploadAvatar(HttpServletResponse response, HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Map<String,Object> map  = new HashMap<>();
        // String uploadDir = "F:/kdgc_project/Student_Attendence_Application/src/main/webapp/resources/upload/";
        try {
            UserDTO user = (UserDTO)request.getAttribute("loginUser");
            InputStream inputStream;
            String contentType;
            String filename;
            Long contentLength;
            inputStream = file.getInputStream();
            contentType = file.getContentType();
            filename = getFileName(contentType);
            contentLength = file.getSize();
            //System.out.println("原始名字"+file.getOriginalFilename()+"类型："+file.getContentType()+"名字："+"upload/user/"+user.getId()+"/"+filename);
            // String url = qCloudProvider.upload(inputStream,contentType,"upload/user/"+user.getId()+"/"+filename,contentLength);
            String url = qCloudProvider.uploadAvatar(inputStream,contentType,user,filename,contentLength);
            map.put("status",0);
            map.put("msg","");
            map.put("url",url);
            user.setAvatarUrl(url);
            Cookie cookie = cookieUtils.getCookie("token",tokenUtils.getToken(user),86400*3);
            response.addCookie(cookie);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status",500);
            map.put("msg","上传失败");
            map.put("url",null);
            return map;
        }
    }



    public String getFileName(String contentType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        String[] ss = contentType.split("/");
        String str = RandomStringUtils.random(2,
                "abcdefghijklmnopqrstuvwxyz1234567890");
        String name = timeStr + "_" +str+"."+ss[1];
        return name;
    }



    @PostMapping("/file/project/upload")
    @ResponseBody
    public Project projectUpload(HttpServletRequest request,@RequestParam("file") MultipartFile file
                        ) {
        try {
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            String filename = file.getOriginalFilename();
            Long contentLength = file.getSize();
            String url = qCloudProvider.upload(inputStream,contentType,filename,contentLength);
            Project project=projectService.upload(file.getOriginalFilename(),url);
            return project;

        } catch (Exception e) {
            Project project=new Project();
            project.setId(Long.valueOf(-1));
            return project;
        }

    }



    @PostMapping("/file/project/delFile")
    @ResponseBody
    public ResultDTO projectDel(HttpServletRequest request, @RequestParam("fileId") Long fileId
    ) {
       ResultDTO resultDTO= projectService.delProject(fileId);
        return resultDTO;
    }


}
