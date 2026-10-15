package com.example.aicodehelper.ai.agent;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Tag(name="CompetitorAgent", description = "竞品报价agent")
public class CompetitorAgent {

    private String modelName;

    private String apiKey;
}
