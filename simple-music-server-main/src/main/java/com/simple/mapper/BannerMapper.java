package com.simple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.model.entity.Banner;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BannerMapper extends BaseMapper<Banner> {
}
