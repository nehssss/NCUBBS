package com.haitaos.finallbbs.mapper;

import com.haitaos.finallbbs.dto.QuestionQueryDTO;
import com.haitaos.finallbbs.model.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface QuestionExtMapper {
    int incView(Question record);

    int incCommentCount(Question record);

    List<Question> selectRelated(Question question);

    List<Question> selectTop(QuestionQueryDTO questionQueryDTO);


    Integer countBySearch(QuestionQueryDTO questionQueryDTO);

    Integer countBySearchAll(QuestionQueryDTO questionQueryDTO);

    List<Question> selectBySearch(QuestionQueryDTO questionQueryDTO);

    List<Question> selectBySearchAll(QuestionQueryDTO questionQueryDTO);

    List<Question> selectLatest(@Param("size") int size);

    List<Question> selectLatestComment(@Param("size") int size);

    List<Question> selectHotQuestion(@Param("size") int size);

    List<Question> selectGoodQuestion(@Param("size") int size);

    List<Long> selectGoodMan(@Param("size") int size);




}
