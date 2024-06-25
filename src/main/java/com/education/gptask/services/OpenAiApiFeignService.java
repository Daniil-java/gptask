package com.education.gptask.services;

import com.education.gptask.configurations.OpenAiApiProperties;
import com.education.gptask.dtos.TaskDto;
import com.education.gptask.dtos.openai.OpenAiChatCompletionRequest;
import com.education.gptask.dtos.openai.OpenAiChatCompletionResponse;
import com.education.gptask.integrations.OpenAiFeignClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiApiFeignService {
    private final OpenAiFeignClient openAiFeignClient;
    private final OpenAiApiProperties openAiApiProperties;

    public List<TaskDto> generateSubtasks(String name, String comment) throws JsonProcessingException {

        String request = String.format(
                "В конце данного запроса будут указаны: название задачи и комментарий к ней. У задачи есть 4 типа приоритета (MUST, SHOULD, COULD, WOULD).  \n" +
                "Твоя задача декомпозировать задачу на меньшие фрагменты.\n" +
                "Не пиши ничего лишнего, никаких объяснений. В качестве ответа, ты должен использовать ТОЛЬКО JSON сообщение следующего формата:\n" +
                "[\n" +
                "  {\n" +
                "    \"name\": \"Sample Task 1\",\n" +
                "    \"priority\": \"MUST\",\n" +
                "    \"comment\": \"This is the first sample task.\"\n" +
                "  },\n" +
                "  ...\n" +
                "]\n" +
                "\n" +
                "Имя задачи: \"%s\"\n" +
                "Комментарий: \"%s\"", name, comment);
        OpenAiChatCompletionResponse response =
                openAiFeignClient.generate(
                        openAiApiProperties.getOpenAiKey(),
                        OpenAiChatCompletionRequest.makeRequest(
                                request)
                );

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(
                response.getChoices().get(0).getMessage().getContent(),
                new TypeReference<List<TaskDto>>()
                {});
    }
}
