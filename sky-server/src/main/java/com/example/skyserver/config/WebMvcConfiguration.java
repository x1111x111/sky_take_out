package com.example.skyserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;


@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;
    @Autowired
    private UserLoginCheckInterceptor
            userLoginCheckInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
    @Override
    public void addInterceptors(
            InterceptorRegistry registry) {


        registry.addInterceptor(loginCheckInterceptor)

                .addPathPatterns("/admin/**")

                .excludePathPatterns(
                        "/admin/employee/login"
                );
        registry.addInterceptor(userLoginCheckInterceptor)

                .addPathPatterns("/user/**")

                .excludePathPatterns(
                        "/user/user/login"
                );
    }
    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        String uploadPath =
                System.getProperty("user.dir")
                        + File.separator
                        + "uploads"
                        + File.separator;

        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "file:" + uploadPath
                );
    }
}