package com.haitaos.finallbbs.dto;

import lombok.Data;

@Data
public class ThumbDTO {
    private Long id;
    private Long targetId;
    private Integer type;
    private UserDTO user;
    private Long gmtCreate;
    private Long gmtModified;

}

