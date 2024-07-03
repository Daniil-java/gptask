package com.education.gptask.integrations;

import com.education.gptask.dtos.openai.OpenAiChatCompletionRequest;
import com.education.gptask.dtos.openai.OpenAiChatCompletionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        value = "open-ai-feign-client",
        url = "${integrations.openai-api.url}"
)
public interface OpenAiFeignClient  {
    @PostMapping("chat/completions")
    OpenAiChatCompletionResponse generate(@RequestHeader("Authorization") String key,
                                          @RequestBody OpenAiChatCompletionRequest request);
}
