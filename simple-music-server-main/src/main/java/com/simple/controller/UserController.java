package com.simple.controller;

import com.simple.mapper.UserMapper;
import com.simple.model.dto.UserLoginDTO;
import com.simple.result.Result;
import com.simple.service.IUserService;
import com.simple.util.BindingResultUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    IUserService userService;

    /**
     * 用户登录
     *
     * @param userLoginDTO  用户登录参数
     * @param bindingResult 用于接收和存储校验结果
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody @Valid UserLoginDTO userLoginDTO, BindingResult bindingResult) {
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.login(userLoginDTO);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("/getUserInfo")
    public Result getUserInfo() {
        return userService.getUserInfo();
    }
}
