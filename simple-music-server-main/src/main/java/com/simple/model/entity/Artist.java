package com.simple.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
/**
 * 禁用父类字段参与 equals 和 hashCode 计算
 */
@EqualsAndHashCode(callSuper = false)
/**
 * 启用链式调用模式，setter 方法返回 this
 */
@Accessors(chain = true)
@TableName("tb_artist")
public class Artist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 歌手 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long artistId;

    /**
     * 歌手姓名
     */
    @TableField("name")
    private String artistName;

    /**
     * 歌手性别：0-男，1-女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 歌手头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 歌手出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("birth")
    private LocalDate birth;

    /**
     * 歌手所处地区
     */
    @TableField("area")
    private String area;

    /**
     * 歌手简介
     */
    @TableField("introduction")
    private String introduction;
}
