package com.example.aicodehelper.controller;

import com.example.aicodehelper.ai.AiCodeHelperService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {


    @Resource
    private AiCodeHelperService aiCodeHelperService;


    @GetMapping("/chat")
    public  Flux<ServerSentEvent<String>> chat(@RequestParam("memoryId") int memoryId, @RequestParam("message") String message) {
        return aiCodeHelperService.chatStream(memoryId, message)
                .map(chunk -> ServerSentEvent.builder(chunk).data(chunk).build());
    }
}
