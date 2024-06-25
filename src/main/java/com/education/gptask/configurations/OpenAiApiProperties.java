package com.education.gptask.configurations;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class OpenAiApiProperties {
    @Value("${integrations.openai-api.key}")
    private final String OpenAiKey;
}
