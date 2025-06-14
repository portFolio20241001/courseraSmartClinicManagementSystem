package com.project.back_end.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 全体の共通設定クラス  
 *  - CORS ポリシー  
 *  - PasswordEncoder Bean 登録  ← ★追加
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /* =========================
     * 1. CORS 設定
     * ========================= */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")                       // 例: "http://localhost:5173"
                .allowedMethods("GET","POST","PUT","DELETE")
                .allowedHeaders("*");
    }

    /* =========================
     * 2. PasswordEncoder Bean   
     * ========================= */
    /**
     * アプリ全体で使用する {@link PasswordEncoder} を DI コンテナへ登録する。  
     * デフォルト強度(10)の BCrypt を使用。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
