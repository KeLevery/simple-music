package com.simple.interceptor;

import com.simple.config.RolePermissionManager;
import com.simple.constant.JwtClaimsConstant;
import com.simple.constant.MessageConstant;
import com.simple.constant.PathConstant;
import com.simple.util.JwtUtil;
import com.simple.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONStringer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/***
 * 1.先cors预检查请求
 * 2.从请求头中获取token
 * 3.获取spring的pathmatcher
 * 4.定义允许访问的路径
 * 5.检查路径是否匹配
 * 6.如果用户未登录，先允许访问那些可以访问的路径。
 * 7.如果用户登录了，先从redis中获取用户的token，
 * 如果不存在就提示token过期
 * 如果存在token，那么先通过jwt解析token，然后从中获取用户的权限以及用户的发送请求的地址
 *
 * 然后通过rolePermissionManager.hasPermission方法，
 * 判断当前角色是否有权限访问这个地址，如果有将业务数据存储到ThreadLocal中
 * 如果没有就报错
 ***/
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RolePermissionManager rolePermissionManager;

    // 发送错误响应
    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8"); // 设置响应的Content-Type
        response.getWriter().write(message);
    }

    // 预检查请求
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理 CORS 预检请求
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String path = request.getRequestURI();

        // 获取spring的pathmatcher
        //AntPathMatcher 支持 Ant 风格的路径模式,如 /api/**、/user/* 等
        PathMatcher pathMatcher = new AntPathMatcher();

        //定义允许访问的路径
        List<String> allowedPaths = Arrays.asList(
                PathConstant.PLAYLIST_DETAIL_PATH,
                PathConstant.ARTIST_DETAIL_PATH,
                PathConstant.SONG_LIST_PATH,
                PathConstant.SONG_DETAIL_PATH
        );

        // 检查路径是否匹配
        boolean isAllowedPath = allowedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        // 如果用户未登录，先允许访问那些可以访问的路径
        if (token == null || token.isEmpty()) {
            if (isAllowedPath) {
                return true; // 允许未登录用户访问这些路径
            }

            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN); // 缺少令牌
            return false;
        }

        try {
            // 从redis中获取用户的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                throw new RuntimeException();
            }

            // 通过jwt解析token，然后从中获取用户的权限以及用户的发送请求的
            Map<String, Object> claims = JwtUtil.parseToken(token);
            String role = (String) claims.get(JwtClaimsConstant.ROLE);
            // 获取当前请求的url
            String requestURI = request.getRequestURI();
            // 判断当前角色是否具有权限访问这个url
            if (rolePermissionManager.hasPermission(role, requestURI)) {
                ThreadLocalUtil.set(claims);
                return true;
            } else {
                sendErrorResponse(response, 403, MessageConstant.NO_PERMISSION); // 无权限访问
                return false;
            }
        } catch (Exception e) {
            sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED); // 令牌无效
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        ThreadLocalUtil.remove();
    }
}
