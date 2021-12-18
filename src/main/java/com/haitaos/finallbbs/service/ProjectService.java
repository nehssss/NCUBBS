package com.haitaos.finallbbs.service;


import com.haitaos.finallbbs.dto.ResultDTO;
import com.haitaos.finallbbs.mapper.ProjectMapper;
import com.haitaos.finallbbs.model.Project;
import com.haitaos.finallbbs.model.ProjectExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    @Autowired
    ProjectMapper projectMapper;

    public Project upload(String fileName,String url){
        Project project=new Project();
        project.setFileName(fileName);
        project.setFileUrl(url);
        project.setId(0L);
        project.setParentId(0L);
        projectMapper.insert(project);
        ProjectExample projectExample=new ProjectExample();
        projectExample.createCriteria().andFileUrlEqualTo(url);
        List<Project> projectList=projectMapper.selectByExample(projectExample);
        return projectList.get(0);
    }


    public Project findProjectByParentId(Long parentId){
        ProjectExample projectExample=new ProjectExample();
        projectExample.createCriteria()
                .andParentIdEqualTo(parentId);
        List<Project> projectList=projectMapper.selectByExample(projectExample);
        if(projectList.size()!=0)
        return projectList.get(0);
        else return null;
    }

    public ResultDTO delProject(Long fileId){
        projectMapper.deleteByPrimaryKey(fileId);
        return ResultDTO.okOf("删除成功");
    }

}
