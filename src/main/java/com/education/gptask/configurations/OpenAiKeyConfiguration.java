package com.education.gptask.configurations;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Getter
@Configuration
public class OpenAiKeyConfiguration {
    private final String key;

    @Autowired
    public OpenAiKeyConfiguration(Environment environment) {
        this.key = environment.getProperty("GENERATION_TOKEN");
        log.info("Generation key initiated (openai)");
    }
}
