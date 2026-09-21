package com.simple.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ArtistVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 ID
     */
    private Long artistId;

    /**
     * 歌手姓名
     */
    private String artistName;

    /**
     * 性别：0-男，1-女
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birth;

    /**
     * 地区
     */
    private String area;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
