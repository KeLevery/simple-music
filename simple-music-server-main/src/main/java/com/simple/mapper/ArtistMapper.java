package com.simple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.model.entity.Artist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArtistMapper extends BaseMapper<Artist> {
}
