package com.haitaos.finallbbs.dto;

import lombok.Data;

@Data
public class FileDTO {
    private Integer success; //错误代码，0 表示没有错误。
    private String[] url; //已上传的图片路径
    // private String layData; //已上传的图片路径

}
