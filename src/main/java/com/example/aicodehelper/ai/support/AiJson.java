package com.example.aicodehelper.ai.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 大模型 JSON 输出解析工具。
 *
 * <p>LLM 返回的 JSON 常常被 ```json ... ``` 代码围栏包裹，或前后夹带解释性文字，
 * 直接反序列化会失败。这里统一做「提取 + 容错解析」，供报价/调研/合规/疵点等
 * 需要结构化输出的业务复用。</p>
 */
@Slf4j
public final class AiJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AiJson() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * 从任意文本中截取第一个完整的 JSON 对象 / 数组片段。
     * 优先剥离 markdown 代码围栏，其次按括号配对扫描。
     *
     * @param raw LLM 原始输出
     * @return 纯 JSON 字符串；无法识别时返回原文去除首尾空白
     */
    public static String extract(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        // 1. 剥离 ```json ... ``` 或 ``` ... ``` 围栏
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            }
            int fenceEnd = text.lastIndexOf("```");
            if (fenceEnd >= 0) {
                text = text.substring(0, fenceEnd);
            }
            text = text.trim();
        }
        // 2. 括号配对扫描，截取首个完整 JSON 片段
        int start = firstBracket(text);
        if (start < 0) {
            return text;
        }
        char open = text.charAt(start);
        char close = open == '{' ? '}' : ']';
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == open) {
                depth++;
            } else if (c == close) {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return text.substring(start);
    }

    private static int firstBracket(String text) {
        int obj = text.indexOf('{');
        int arr = text.indexOf('[');
        if (obj < 0) {
            return arr;
        }
        if (arr < 0) {
            return obj;
        }
        return Math.min(obj, arr);
    }

    /**
     * 将 LLM 输出解析为 JsonNode；失败返回 null（调用方据此降级）。
     */
    public static JsonNode parseTree(String raw) {
        String json = extract(raw);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            log.warn("解析大模型 JSON 失败，原文片段：{}", abbreviate(json), e);
            return null;
        }
    }

    /**
     * 将 LLM 输出解析为指定类型；失败返回 null。
     */
    public static <T> T parse(String raw, Class<T> type) {
        String json = extract(raw);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            log.warn("解析大模型 JSON 到 {} 失败，原文片段：{}", type.getSimpleName(), abbreviate(json), e);
            return null;
        }
    }

    private static String abbreviate(String text) {
        return text.length() > 300 ? text.substring(0, 300) + "..." : text;
    }
}
