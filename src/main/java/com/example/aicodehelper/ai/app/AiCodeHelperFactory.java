package com.example.aicodehelper.ai.app;

import com.example.aicodehelper.ai.mcp.McpConfig;
//import com.example.aicodehelper.ai.model.TextOnlyStreamingChatModel;
import com.example.aicodehelper.ai.tools.MathTools;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeHelperFactory
{

    @Resource
    private ChatModel myQwenChatModel; // 这个chatModel携带了监听器，相当于仿制了一个chatModel
//    private ChatModel qwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;
    @Resource
    private McpConfig mcpConfig;
    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private InputGuardrail inputGuardrail;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

//    @Resource
//    private ChatMemoryStore chatMemoryStore;

    @Bean
    public AiCodeHelperService aiCodeHelperService() {
        // 构建AI服务
        return AiServices.builder(AiCodeHelperService.class)
                .chatModel(myQwenChatModel)
                // 文本模型回放共享记忆时剥离图片等多模态内容，避免 qwen-max 报错
//                .streamingChatModel(new TextOnlyStreamingChatModel(qwenStreamingChatModel)) // 流式输出
//                // 与多模态链路共享同一份按 memoryId 隔离的会话记忆
//                .chatMemoryProvider(chatMemoryStore::get) // 每个会话独立存储
                .contentRetriever(contentRetriever) // Rag检索生成
                .tools(new MathTools()) // 工具调用
//                .toolProvider(mcpToolProvider) // mcp工具调用
                .inputGuardrails(inputGuardrail)
                .build();
    }
}





//public class Main {
//
//    // 以下为华北2（北京）地域的URL，调用时请将WorkspaceId替换为真实的业务空间ID，各地域的URL不同。
//    static {Constants.baseHttpApiUrl="https://{WorkspaceId}.cn-beijing.maas.aliyuncs.com/api/v1";}
//
//    public static void streamCall()
//            throws ApiException, NoApiKeyException, UploadFileException {
//        MultiModalConversation conv = new MultiModalConversation();
//        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
//                .content(Arrays.asList(Collections.singletonMap("image", "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20241022/emyrja/dog_and_girl.jpeg"),
//                        Collections.singletonMap("text", "图中描绘的是什么景象？"))).build();
//        MultiModalConversationParam param = MultiModalConversationParam.builder()
//                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
//                // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
//                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
//                .model("qwen3-vl-plus")  // 可按需更换为其它多模态模型，并修改相应的 messages
//                .messages(Arrays.asList(userMessage))
//                .incrementalOutput(true)
//                .build();
//        Flowable<MultiModalConversationResult> result = conv.streamCall(param);
//        result.blockingForEach(item -> {
//            try {
//                List<Map<String, Object>> content = item.getOutput().getChoices().get(0).getMessage().getContent();
//                // 判断content是否存在且不为空
//                if (content != null &&  !content.isEmpty()) {
//                    System.out.println(content.get(0).get("text"));
//                }
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
//        });
//    }
//
//    public static void main(String[] args) {
//        try {
//            streamCall();
//        } catch (ApiException | NoApiKeyException | UploadFileException e) {
//            System.out.println(e.getMessage());
//        }
//        System.exit(0);
//    }
//}