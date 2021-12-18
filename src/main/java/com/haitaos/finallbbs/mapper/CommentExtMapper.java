package com.haitaos.finallbbs.mapper;


import com.haitaos.finallbbs.dto.CommentQueryDTO;
import com.haitaos.finallbbs.model.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentExtMapper {
    int incCommentCount(Comment comment);

    Integer countBySearch(CommentQueryDTO commentQueryDTO);

}
