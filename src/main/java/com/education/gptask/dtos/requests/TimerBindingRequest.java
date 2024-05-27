package com.education.gptask.dtos.requests;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TimerBindingRequest {
    private Long taskId;
}
