package com.haitaos.finallbbs.mapper;

import com.haitaos.finallbbs.dto.UserQueryDTO;
import com.haitaos.finallbbs.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface UserExtMapper {
    List<User> selectLatestLoginUser(UserQueryDTO userQueryDTO);
    Integer count(UserQueryDTO userQueryDTO);

    Integer countBySearch(UserQueryDTO userQueryDTO);

    List<User> selectBySearch(UserQueryDTO userQueryDTO);
}
