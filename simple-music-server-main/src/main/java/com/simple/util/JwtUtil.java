package com.simple.util;

//jwt工具

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtil {
    //设置密钥
    private static final String SECRET_KEY = "SIMPLE_MUSIC";
    //设置JWT过期时间
    private static final long EXPIRATION_TIME = 60 * 60 * 1000;

    /**
     * 生成JWT
     *
     * @param claims 自定义的业务数据
     * @return JWT
     */
    public static String generateToken(Map<String, Object> claims) {
        //生成JWT
        return JWT.create()
                .withClaim("claims", claims) // 自定义的业务数据
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 设置过期时间
                .sign(Algorithm.HMAC256(SECRET_KEY)); // 使用 HMAC256 算法加密
    }

    /**
     * 解析JWT
     *
     * @param token JWT
     * @return 自定义的业务数据
     */
    public static Map<String,Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}
