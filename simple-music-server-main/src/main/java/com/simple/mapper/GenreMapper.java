package com.simple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.model.entity.Genre;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GenreMapper extends BaseMapper<Genre> {

}
