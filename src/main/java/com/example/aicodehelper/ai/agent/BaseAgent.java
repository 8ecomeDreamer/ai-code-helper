package com.example.aicodehelper.ai.agent;

import cn.hutool.core.util.StrUtil;
import com.example.aicodehelper.common.enums.AgentState;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 封装通用agent
 * 定义通用执行流程
 * 1.对话状态管理 （防止同时调用多个agent等）
 * 2.对话上下文管理 （保存上下文，方便交互时调用）
 * 3.步骤控制（防止对话步骤过长）
 * 4.异常处理
 * 5.通用资源清理
 *
 */
@Data
@Slf4j
public abstract class BaseAgent {


    /**
     * 默认定义最大步骤数、sse超时时间、错误前缀
     */
    private static final int Default_Max_Step = 10;
    private static final long SSE_TIMEOUT_MS = 300_000L;
    private static final String ERROR_PREFIX = "执行错误";

    /**
     * 定义agent名称
     */
    private String name;

    /**
     * 定义prompt 用户prompt和系统prompt
     */
    private String systemPrompt;
    private String userPrompt;

    /**
     * 定义agent状态
     */
    private AgentState state = AgentState.IDLE;


    /**
     *  定义初始处理次数和最大处理次数
     */
    private int currentStep = 0;
    private int maxStep = Default_Max_Step;

    /**
     * 定义初始LLM SpringAI还可以使用ChatClient
     */
    private ChatModel chatModel;

    /**
     * 定义初始聊天记忆
     */
    private List<ChatMessage> messageList = new ArrayList<>();


    /**
     * 同步执行任务
     * 流程解释：
     * 1.边界检查
     * 2.保存消息上下文
     * 3.返回结果
     * 4.记录日志
     */
    public String run(String userPrompt) {
        // 1.边界检查
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Agent正在运行中...");
        }
        if(StrUtil.isBlank(userPrompt)){
            throw new RuntimeException("用户输入不能为空");
        }
        // 2.修改状态
        this.state = AgentState.RUNNING;
        // 保存用户需求
        messageList.add(new UserMessage(userPrompt));
        // 返回结果
        List <String> results = new ArrayList<>();

        try {
            // 循环执行step获取结果
            for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("正在执行调用，目前进度： {}/{}", stepNumber, maxStep);
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }

            if (currentStep >= maxStep) {
                state = AgentState.FINISHED;
                results.add("执行完成，执行次数： " + currentStep + " 次");
            }

            return String.join("\n", results);

        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("执行出错：" + e);
            return ERROR_PREFIX + e.getMessage();
        } finally {
            this.clean();
        }
    }

    /**
     * 异步执行任务
     * 流式输出
     */
    public SseEmitter runSteam(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT_MS); // 5-minute timeout
        CompletableFuture.runAsync(() -> {
            // 1.边界检查
            if (this.state != AgentState.IDLE) {
                throw new RuntimeException("Agent正在运行中...");
            }
            if(StrUtil.isBlank(userPrompt)){
                throw new RuntimeException("用户输入不能为空");
            }
            // 2.修改状态
            this.state = AgentState.RUNNING;
            // 保存用户需求
            messageList.add(new UserMessage(userPrompt));
            // 返回结果
            List <String> results = new ArrayList<>();

            try {
                // 循环执行step获取结果
                for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("正在执行调用，目前进度： {}/{}", stepNumber, maxStep);
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                }

                if (currentStep >= maxStep) {
                    state = AgentState.FINISHED;
                    results.add("执行完成，执行次数： " + currentStep + " 次");
                }
                // 完成：替换为sseEmitter的complete
                sseEmitter.complete();

            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("执行出错：" + e);
                // 完成：替换为sseEmitter的complete
                try {
                    sseEmitter.send(ERROR_PREFIX + "：" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                this.clean();
            }
        });

        // 超时异常回调方法
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.clean();
            log.warn("SSE connection timeout");
        });
        // 正常完成回调方法
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.clean();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }

    /**
     * 子类继承，下一步操作
     */
    public abstract String step();

    /**
     * 子类继承，清理方法
     */
    protected void clean(){

    }

}
