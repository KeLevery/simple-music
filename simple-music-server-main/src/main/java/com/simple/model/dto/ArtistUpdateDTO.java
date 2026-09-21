package com.simple.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ArtistUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 ID（必填，用于标识要更新的歌手）
     */
    private Long artistId;

    /**
     * 歌手名（可选，更新时提供）
     */
    private String artistName;

    /**
     * 性别：0-男歌手，1-女歌手，2-组合/乐队（可选）
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
