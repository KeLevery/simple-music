package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simple.constant.MessageConstant;
import com.simple.mapper.AdminMapper;
import com.simple.model.dto.AdminDTO;
import com.simple.model.entity.Admin;
import com.simple.result.Result;
import com.simple.service.IAdminService;
import com.simple.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AdminServiceImpl implements IAdminService {

    @Autowired
    AdminMapper adminMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result login(AdminDTO adminDTO) {
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", adminDTO.getUsername()));
//        如果不存在就不让登录
        if (admin == null) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ERROR);
        }

        if (DigestUtils.md5DigestAsHex(adminDTO.getPassword().getBytes()).equals(admin.getPassword())) {
            //登录成功
            Map <String, Object> claims = new java.util.HashMap<>();
            claims.put("role", "ROLE_ADMIN");
            claims.put("adminId", admin.getAdminId());
            claims.put("username", admin.getUsername());
            //生成jwt令牌
            String token = JwtUtil.generateToken(claims);

            //将token加入到redis缓存中去
            stringRedisTemplate.opsForValue().set(token, token, 6, TimeUnit.HOURS);

            //返回消息和加密过的token
            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
        }

        //如果密码错误就返回错误信息
        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    /**
     * 登出
     *
     * @param token 认证token
     * @return 结果
     */
    @Override
    public Result logout(String token) {
        //前端登出，告诉后台将token从redis中删除,注销token
        Boolean result = stringRedisTemplate.delete(token);
        if(result != null && result){
            return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
        } else {
            return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
        }
    }

}
