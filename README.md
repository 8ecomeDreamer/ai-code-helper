# 🤖 AI‑Code‑Helper

>
> 基于 LangChain4j 实现的 Java AI 助手应用
> 使用 SpringBoot + LangChain4j 实现大模型对话、内容安全防护、私有知识库问答等 AI 能力

## 🚀 项目亮点

- ✅ **智能对话系统**：支持流式 / 普通对话模式
- 🛡️ **安全护栏机制**：自动拦截敏感词，保护模型输入安全
- 📚 **私有知识库**：集成 PDF 文档 RAG 检索问答功能
- 🌐 **RESTful API 接口**：标准接口设计，便于前后端分离开发
- ☕ **JDK 21 技术栈**：基于最新 Java 版本开发，性能更优

## 🛠️ 技术架构

表格

| 组件 | 版本 / 说明 |
| --- | --- |
| Java | JDK 21 (必须) |
| 框架 | Spring Boot 3.x |
| AI 框架 | LangChain4j 最新稳定版 |
| 构建工具 | Maven 3.8+ |
| 支持模型 | OpenAI / 通义千问 / Ollama (本地) |

## 📁 项目结构

```
ai-code-helper/
├── src/main/java/com/example/aicodehelper
│   ├── ai/                   # AI 核心模块
│   │   ├── guardrails/      # 敏感词校验逻辑
│   │   ├── config/          # 模型配置类
│   │   └── service/         # 对话/知识库服务
│   ├── controller/          # 接口层
│   └── AiCodeHelperApplication.java  # 启动类
├── resources/              # 配置文件与静态资源
└── pom.xml                # Maven 构建配置
```

## ⚙️ 环境配置指南

>
> ⚠️ JDK 版本不匹配为高频报错！下面三处环境必须统一设置为 **JDK‑21**

1. **项目设置** `File → Project Structure → Project`
   - Project SDK: `JDK‑21`
   - Language level: `21`
2. **模块设置** `Modules`
   - Module SDK: `JDK‑21`
   - Language level: `21`
3. **Maven 运行时** `Settings → Build Tools → Maven → Runner`
   - JRE: `Project JDK‑21`

### 配置文件示例（本地开发 application‑local.yml）

>
> 线上部署（微信云托管）密钥请使用**控制台环境变量注入**，禁止硬编码密钥到代码包

```
server:
  port: 8081

langchain4j:
  open-ai:
    chat-model:
      api-key: "sk-xxx你的密钥xxx"
      model-name: "gpt-3.5-turbo"
```

>
> 微信云托管生产环境配置示例（读取云托管环境变量）

```
server:
  port: 80
servlet:
  context-path: /api
langchain4j:
  community:
    dashscope:
      chat-model:
        model-name: qwen-max
        api-key: ${LANGCHAIN4J_API_KEY}
```

## ▶️ 启动方式

### 方式一：IDEA 直接运行

找到启动类：
`com.example.aicodehelper.AiCodeHelperApplication`
右键 → `Run` 即可启动项目

### 方式二：Maven 命令打包部署

```
# 清理并打包（跳过单元测试）
mvn clean package -DskipTests

# 运行生成的可执行 Jar 包
java -jar target/ai-code-helper-0.0.1-SNAPSHOT.jar
```

## 🐞 踩坑日志 & 避坑清单

表格

| 问题类型 | 解决方案 |
| --- | --- |
| 🔴 JDK 版本报错 | 确保 pom.xml 中 `maven.compiler.source` / `target` 设置为 21 |
| 📄 PDF 加载失败 (FileSystemDocumentLoader) | 使用绝对路径测试；文件名避免中文、空格、特殊符号；路径基准为项目根目录 |
| 🧱 Jar 包打包后无法运行 | 删除 `spring-boot-maven-plugin` 插件中的 `<skip>true</skip>` |
| 🔒 安全护栏未生效 | 自定义 `SafeInputGuardrail` 需要手动注册注入到 AiService |
| ☁️ 微信云托管读不到 Api‑Key | 环境变量名大写 + 下划线；修改环境变量后**必须重新发布新版本**；SpringBoot 生产端口改为 80 |

## 📚 学习资源

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

>
> 教程来源：鱼皮 LangChain4j Java AI 零基础实战教程

## 📄 协议与许可

本项目采用 **MIT 协议** 开源，欢迎自由使用、修改、分发。
如有问题或建议，欢迎提交 Issue 或 Pull Request！