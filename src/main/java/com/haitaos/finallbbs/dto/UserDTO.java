package com.haitaos.finallbbs.dto;

import com.haitaos.finallbbs.model.UserAccount;
import com.haitaos.finallbbs.model.UserInfo;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String avatarUrl;
    private Integer vipRank;
    private Integer groupId;
    private String groupStr;
    private Long gmtCreate;
    private String gmtCreateStr;
    private Long gmtModified;
    private String gmtModifiedStr;
    private String email;
    private String accountId;
    private UserAccount userAccount;
    private UserInfo userInfo;
    private int score;

//    private String unread;
}
