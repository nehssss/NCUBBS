package com.haitaos.finallbbs.dto;

import lombok.Data;

@Data
public class QuestionQueryDTO {
    private String search;
    private String tag;
    private String sort;
    private Long time;
    private Integer page;
    private Integer offset;
    private Integer size;
    private Integer column2;
    private Long start;
    private Long end;

}