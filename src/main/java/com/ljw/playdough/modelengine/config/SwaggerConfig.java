package com.ljw.playdough.modelengine.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Model Engine API",
                version = "1.0",
                description = "元数据驱动的数据模型引擎，支持动态模型管理与实例数据操作"
        )
)
@Configuration
public class SwaggerConfig {
}
