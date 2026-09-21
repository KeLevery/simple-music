package com.simple.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.model.entity.User;
import com.simple.result.Result;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    Result getUserInfo();
}
