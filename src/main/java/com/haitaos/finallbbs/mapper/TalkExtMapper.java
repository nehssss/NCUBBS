package com.haitaos.finallbbs.mapper;

import com.haitaos.finallbbs.dto.TalkQueryDTO;
import com.haitaos.finallbbs.model.Talk;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TalkExtMapper {

    Integer count(TalkQueryDTO talkQueryDTO);

    int updateByPrimaryKeySelective(Talk talk);
}
