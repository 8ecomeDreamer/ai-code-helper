package com.example.aicodehelper.ai.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Value("${mcp.baiduZP.apiKey}")
    private String apiKey;

    @Bean
    public McpToolProvider mcpToolProvider() {

        // MCP 传输， 这里有两种传输方式（SSE和Stdio， 前者就是http连接）
        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("http://appbuilder.baidu.com/v2/ai_search/mcp/sse?api_key=" + apiKey)
                .logRequests(true) // 打印请求
                .logResponses(true) // 打印响应
                .build();

        // MCP 客户端
        McpClient mcpClient = new DefaultMcpClient.Builder()
                .key(apiKey)
                .transport(transport)
                .build();

        // MCP 工具提供者
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();

        return toolProvider;
    }

}
