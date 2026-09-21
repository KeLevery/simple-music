package com.simple.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.model.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

}
