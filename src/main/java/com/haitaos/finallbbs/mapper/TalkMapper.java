package com.haitaos.finallbbs.mapper;



import java.util.List;

import com.haitaos.finallbbs.model.Talk;
import com.haitaos.finallbbs.model.TalkExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
@Mapper
public interface TalkMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    long countByExample(TalkExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    int deleteByExample(TalkExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    int insert(Talk record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    int insertSelective(Talk record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    List<Talk> selectByExampleWithRowbounds(TalkExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    List<Talk> selectByExample(TalkExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    Talk selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    int updateByExampleSelective(@Param("record") Talk record, @Param("example") TalkExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    int updateByExample(@Param("record") Talk record, @Param("example") TalkExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    int updateByPrimaryKeySelective(Talk record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table talk
     *
     * @mbg.generated Wed Sep 23 18:14:48 CST 2020
     */
    int updateByPrimaryKey(Talk record);
}