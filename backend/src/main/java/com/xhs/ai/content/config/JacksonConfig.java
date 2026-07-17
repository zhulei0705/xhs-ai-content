package com.xhs.ai.content.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /** 雪花ID以字符串返回，避免JavaScript超过Number安全整数范围后精度丢失。 */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longIdCustomizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance);
    }
}
