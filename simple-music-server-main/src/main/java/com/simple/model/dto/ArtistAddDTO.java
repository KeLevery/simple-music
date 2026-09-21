package com.simple.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ArtistAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手名
     */
    private String artistName;

    /**
     * 性别：0-男歌手，1-女歌手，2-组合/乐队，默认 0
     */
    private Integer gender;

    /**
     * 出生日期，格式：YYYY-MM-DD（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String birth;

    /**
     * 国籍/地区（可选）
     */
    private String area;

    /**
     * 歌手简介（可选）
     */
    private String introduction;
}
