package com.microblog.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Gets profile pictures from the folder "uploads/pfp/"
        registry.addResourceHandler("/pfp/**")
                .addResourceLocations("file:uploads/pfp/");
    }
}
