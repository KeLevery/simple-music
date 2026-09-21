package com.simple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.model.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}
