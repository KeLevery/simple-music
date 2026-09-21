package com.simple.config;
import com.simple.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer { // ← 这里实现 WebMvc
    //    自定义了一个拦截器
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 加上 OPTIONS
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // 只有实现了 WebMvcConfigurer，才能重写这个方法
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录接口和注册接口不拦截
        //InterceptorRegistry自定义一个拦截器
        registry.addInterceptor(loginInterceptor)  // 添加一个名为 loginInterceptor 的拦截器
                .addPathPatterns("/**")            // 拦截所有路径的请求
                .excludePathPatterns(              // 但是排除以下路径（不拦截）
                        "/admin/login", "/admin/logout", "/admin/register",
                        "/user/login", "/user/logout", "/user/register",
                        "/user/sendVerificationCode", "/user/resetUserPassword",
                        "/banner/getBannerList",
                        "/playlist/getAllPlaylists", "/playlist/getRecommendedPlaylists",
                        "/playlist/getPlaylistDetail/**",
                        "/artist/getAllArtists", "/artist/getArtistDetail/**",
                        "/song/getAllSongs", "/song/getRecommendedSongs",
                        "/song/getSongDetail/**",
                        "/test");
    }
}
