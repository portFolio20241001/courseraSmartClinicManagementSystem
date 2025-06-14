package com.project.back_end.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 用の設定クラス
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI のメタ情報を設定（タイトルやバージョン）
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Clinic API")
                        .description("Spring Boot によるクリニック管理 REST API")
                        .version("1.0.0"));
    }
}
