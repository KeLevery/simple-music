package com.simple.service;

import com.simple.model.dto.UserAddDTO;
import com.simple.model.dto.UserDTO;
import com.simple.model.dto.UserLoginDTO;
import com.simple.model.dto.UserSearchDTO;
import com.simple.model.vo.UserManagementVO;
import com.simple.result.PageResult;
import com.simple.result.Result;

import java.util.List;

public interface IUserService {

    Result<Long> getAllUsersCount();

    Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO);

    Result addUser(UserAddDTO userAddDTO);

    Result updateUser(UserDTO userDTO);

    Result deleteUser(Long userId);

    Result deleteUsers(List<Long> userIds);

    Result updateUserStatus(Long userId,Integer userStatus);

    Result login(UserLoginDTO userLoginDTO);

    Result getUserInfo();
}
