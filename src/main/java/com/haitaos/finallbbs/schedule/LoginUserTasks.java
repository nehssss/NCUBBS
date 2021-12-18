package com.haitaos.finallbbs.schedule;



import com.haitaos.finallbbs.cache.LoginUserCache;
import com.haitaos.finallbbs.dto.UserQueryDTO;
import com.haitaos.finallbbs.mapper.UserExtMapper;
import com.haitaos.finallbbs.mapper.UserMapper;
import com.haitaos.finallbbs.model.User;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@EnableScheduling
public class LoginUserTasks {

    @Autowired
    private UserExtMapper userExtMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginUserCache loginUserCache;


    @Scheduled(fixedRate = 1000 * 60 * 10)
    public void loginUserSchedule() {
        log.info("loginUserSchedule start {}", new Date());
        //从cache中读取最近登录用户
        ExpiringMap<Long, Long> loginUserMap = loginUserCache.getLoginUserMap();
        if(loginUserMap.size()!=0){
            User user = new User();
            for(Long key : loginUserMap.keySet()){
                user.setId(key.longValue());
                user.setGmtModified(loginUserMap.get(key));
                userMapper.updateByPrimaryKeySelective(user);
            }
        }

        // System.out.println("loginUserMap.size():"+loginUserMap.size());

        int offset = 0;
        int limit = 12;
        List<User> list = new ArrayList<>();
        UserQueryDTO userQueryDTO = new UserQueryDTO();
        userQueryDTO.setOffset(offset);
        userQueryDTO.setSize(limit);
        list = userExtMapper.selectLatestLoginUser(userQueryDTO);
        loginUserCache.updateLoginUsers(list);
        log.info("loginUserSchedule stop {}", new Date());

    }
}

