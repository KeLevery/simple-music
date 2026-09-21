package com.simple.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.constant.JwtClaimsConstant;
import com.simple.constant.MessageConstant;
import com.simple.enumeration.RoleEnum;
import com.simple.enumeration.UserStatusEnum;
import com.simple.mapper.UserMapper;
import com.simple.model.dto.UserAddDTO;
import com.simple.model.dto.UserDTO;
import com.simple.model.dto.UserLoginDTO;
import com.simple.model.dto.UserSearchDTO;
import com.simple.model.entity.User;
import com.simple.model.vo.UserManagementVO;
import com.simple.model.vo.UserVO;
import com.simple.result.PageResult;
import com.simple.result.Result;
import com.simple.service.IUserService;
import com.simple.util.JwtUtil;
import com.simple.util.ThreadLocalUtil;
import com.simple.util.TypeConversionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取所有用户数量
     *
     * @return
     */
    @Override
    public Result<Long> getAllUsersCount() {
        return Result.success(userMapper.selectCount(new QueryWrapper<>()));
    }

    /**
     * 获取所有用户
     * 定义返回结果为PageResult<UserManagementVO>
     *
     * @param userSearchDTO 查询条件
     * @return
     */
    @Override
    public Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO) {
        //分页查询
        Page<User> page = new Page<>(userSearchDTO.getPageNum(), userSearchDTO.getPageSize());
        //设置查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userSearchDTO.getUsername() != null) {
            queryWrapper.like("username", userSearchDTO.getUsername());
        }
        if (userSearchDTO.getPhone() != null) {
            queryWrapper.like("phone", userSearchDTO.getPhone());
        }
        if (userSearchDTO.getUserStatus() != null) {
            queryWrapper.eq("user_status", userSearchDTO.getUserStatus());
        }
        // 倒序排序
        queryWrapper.orderByDesc("create_time");
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);
        //返回查询结果
        if (userPage.getRecords().size() == 0) {
            //返回空结果
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 转换为 UserManagementVO
        List<UserManagementVO> userVOList = userPage.getRecords().stream()
                .map(user -> {
                    UserManagementVO userVo = new UserManagementVO();
                    BeanUtils.copyProperties(user, userVo);
                    return userVo;
                }).toList();

        return Result.success(new PageResult<>(userPage.getTotal(), userVOList));
    }

    /**
     * 添加用户
     *
     * @param userAddDTO
     * @return
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result addUser(UserAddDTO userAddDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userAddDTO.getUsername())
                .or()
                .eq("phone", userAddDTO.getPhone())
                .or()
                .eq("email", userAddDTO.getEmail());

        // 查询已存在的用户
        List<User> existingUsers = userMapper.selectList(queryWrapper);
        // 判断用户名、手机号、邮箱是否已存在
        if (existingUsers != null && !existingUsers.isEmpty()) {
            for (User user : existingUsers) {
                if (user.getUsername().equals(userAddDTO.getUsername())) {
                    return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
                } else if (user.getPhone().equals(userAddDTO.getPhone())) {
                    return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
                } else if (user.getEmail().equals(userAddDTO.getEmail())) {
                    return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
                }
            }
        }

        // 密码加密
        String passwordMD5 = DigestUtils.md5DigestAsHex(userAddDTO.getPassword().getBytes());
        User user = new User();
        user.setUsername(userAddDTO.getUsername())
                .setPassword(passwordMD5)
                .setPhone(userAddDTO.getPhone())
                .setEmail(userAddDTO.getEmail())
                .setIntroduction(userAddDTO.getIntroduction())
                .setUpdateTime(LocalDateTime.now())
                .setCreateTime(LocalDateTime.now());

        // 前端传递的用户状态（1：启用，0：禁用）需反转
        if (userAddDTO.getUserStatus().getId() == 1) {
            user.setUserStatus(UserStatusEnum.ENABLE);  // 数据库（0：启用）
        } else if (userAddDTO.getUserStatus().getId() == 0) {
            user.setUserStatus(UserStatusEnum.DISABLE);    // 数据库（1：禁用）
        }
        // 插入数据库
        if (userMapper.insert(user) == 0) {
            // 插入失败返回错误信息
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }

        // 返回成功信息
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 修改用户信息
     *
     * @param userDTO
     * @return
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUser(UserDTO userDTO) {
        Long userId = userDTO.getUserId();
        // 判断用户名是否已存在
        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", userDTO.getUsername()));
        if (userByUsername != null && !userByUsername.getUserId().equals(userId)) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }
        // 判断手机号是否已存在
        User userByPhone = userMapper.selectOne(new QueryWrapper<User>().eq("phone", userDTO.getPhone()));
        if (userByPhone != null && !userByPhone.getUserId().equals(userId)) {
            return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
        }
        // 判断邮箱是否已存在
        User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", userDTO.getEmail()));
        if (userByEmail != null && !userByEmail.getUserId().equals(userId)) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(LocalDateTime.now());

        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @Override
    public Result deleteUser(Long userId) {
        // 如果删除用户失败，返回错误信息
        if (userMapper.deleteById(userId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除用户
     *
     * @param userIds
     * @return
     */
    @Override
    public Result deleteUsers(List<Long> userIds) {
        // 批量删除，如果删除用户失败，返回错误信息
        if (userMapper.deleteByIds(userIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户状态
     *
     * @param userId     用户id
     * @param userStatus 状态
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserStatus(Long userId, Integer userStatus) {
        UserStatusEnum statusEnum;
        if (userStatus == 0) {
            statusEnum = UserStatusEnum.ENABLE;
        } else if (userStatus == 1) {
            statusEnum = UserStatusEnum.DISABLE;
        } else {
            return Result.error(MessageConstant.USER_STATUS_INVALID);
        }

        // 更新用户状态
        User user = new User();
        user.setUserStatus(statusEnum).setUpdateTime(LocalDateTime.now());

        int rows = userMapper.update(user, new QueryWrapper<User>().eq("id", userId));
        if (rows == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public Result login(UserLoginDTO userLoginDTO) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", userLoginDTO.getEmail()));
        if (user == null) {
            return Result.error(MessageConstant.EMAIL+MessageConstant.ERROR);
        }
        if(user.getUserStatus() != UserStatusEnum.ENABLE){
            return Result.error(MessageConstant.ACCOUNT_LOCKED);
        }

        // 密码校验
        if (DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()).equals(user.getPassword())) {
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
            claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.EMAIL, user.getEmail());
            String token = JwtUtil.generateToken(claims);

            // 将token存入redis
            stringRedisTemplate.opsForValue().set(token, token, 6, TimeUnit.HOURS);

            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
        }

        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    /**
     * 获取用户信息
     * @return
     */
    @Override
    public Result getUserInfo() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        User user = userMapper.selectById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return Result.success(userVO);
    }

}
