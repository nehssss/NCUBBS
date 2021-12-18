package com.haitaos.finallbbs.service;

import com.haitaos.finallbbs.mapper.UserAccountMapper;
import com.haitaos.finallbbs.model.UserAccount;
import com.haitaos.finallbbs.model.UserAccountExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAccountService {

    @Autowired
    private UserAccountMapper userAccountMapper;


    public UserAccount selectUserAccountByUserId(Long userId) {
        //  Long id = Long.parseLong(userId);
        UserAccountExample userAccountExample = new UserAccountExample();
        userAccountExample.createCriteria().andUserIdEqualTo(userId);
        List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
        UserAccount userAccount = userAccounts.get(0);
        return userAccount;
    }

    public boolean isAdminByUserId(Long userId){
        if(selectUserAccountByUserId(userId).getGroupId()>=3) return true;
        else return false;

    }

}
