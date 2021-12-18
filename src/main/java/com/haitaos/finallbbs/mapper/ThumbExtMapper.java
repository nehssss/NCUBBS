package com.haitaos.finallbbs.mapper;


import com.haitaos.finallbbs.dto.LikeQueryDTO;
import com.haitaos.finallbbs.model.Comment;
import com.haitaos.finallbbs.model.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ThumbExtMapper {
    int incLikeCount(Comment comment);

    int incQuestionLikeCount(Question question);

    Integer count(LikeQueryDTO likeQueryDTO);

    int searchExist(@Param("targetId")Long targetId, @Param("liker")Long liker);
}
