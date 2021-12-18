package com.haitaos.finallbbs.dto;

import lombok.Data;

@Data
public class LikeQueryDTO {
    private Long id;
    private Long targetId;
    private Integer type;
    private Long liker;
    private Integer page;
    private Integer size;
    private Integer offset;
    private String sort;
    private String order;
    public void convert(){//对非法输入的参数进行转化
        if(this.page==null||this.page<=0) this.page=1;
        if(this.size==null||this.size<=0||this.size>20) this.size= 5;
        if(this.sort==null||!("gmt_modified".equals(this.sort)||"gmt_create".equals(this.sort))) this.sort= "gmt_create";
        if(this.order==null||!("desc".equals(this.order)||"asc".equals(this.order))) this.order= "desc";
    }

}
