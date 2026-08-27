# 🤖 AI‑Code‑Helper

>
> 基于 LangChain4j 实现的企业级 Java AI 助手应用
> 使用 Spring Boot 3.x + LangChain4j 1.7.1 实现大模型对话、Agent工具调用、内容安全防护、私有知识库问答等 AI 能力
>
> [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
> [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
> [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
> [![LangChain4j](https://img.shields.io/badge/LangChain4j-1.7.1-blue.svg)](https://github.com/langchain4j/langchain4j)

## 📋 目录

- [项目简介](#项目简介)
- [核心功能](#核心功能)
- [技术架构](#技术架构)
- [项目结构](#项目结构)
- [环境配置](#环境配置)
- [快速开始](#快速开始)
- [API接口文档](#api接口文档)
- [部署指南](#部署指南)
- [常见问题](#常见问题)
- [学习资源](#学习资源)
- [贡献指南](#贡献指南)

## 🎯 项目简介

AI-Code-Helper 是一个基于 LangChain4j 框架开发的企业级 AI 代码助手后端服务。项目整合了大语言模型、Agent 工具调用、RAG（检索增强生成）、安全护栏等先进技术，为开发者提供智能的编程辅助体验。

### 应用场景

- 🛠️ **代码生成与优化**：智能生成代码片段，优化现有代码结构
- 🐛 **问题诊断**：分析错误日志，提供解决方案
- 📖 **技术问答**：基于私有知识库的精准技术问答
- 🤖 **工具调用**：集成外部工具，扩展 AI 能力边界
- 🛡️ **内容安全**：自动过滤敏感内容，确保输出合规

## ✨ 核心功能

### 🎨 智能对话系统
- **流式响应**：基于 Reactor 实现的 SSE（Server-Sent Events）实时流式对话
- **会话记忆**：支持多会话管理，保存对话上下文
- **System Prompt**：内置专业的软件开发助手人设，精准定位问题

### 🔧 Agent 工具调用
- **内置工具**：数学计算工具（求和、平方根等）
- **扩展能力**：基于 MCP（Model Context Protocol）的工具生态集成
- **智能决策**：AI 自动判断是否需要调用工具

### 🛡️ 安全护栏机制
- **敏感词拦截**：自动检测并过滤输入输出中的敏感内容
- **多层防护**：输入验证 + 内容审查 + 输出过滤
- **自定义规则**：支持扩展自定义安全规则

### 📚 知识库增强（RAG）
- **PDF 文档解析**：支持 PDF 格式的私有知识库导入
- **向量检索**：基于向量的语义搜索，精准定位相关内容
- **混合检索**：结合关键词检索和语义检索，提升准确性

### 🌐 RESTful API
- **标准化接口**：遵循 REST 设计原则
- **跨域支持**：配置全局 CORS 策略
- **响应式编程**：基于 WebFlux 的异步非阻塞处理

## 🏗️ 技术架构

| 技术栈 | 版本 | 说明 |
|--------|------|------|
| **开发语言** | Java 21 | 基于最新 JDK，支持虚拟线程、记录类型等新特性 |
| **Web 框架** | Spring Boot 3.5.3 | 企业级应用开发框架，支持响应式编程 |
| **AI 框架** | LangChain4j 1.7.1 | Java 领域领先的 AI 应用开发框架 |
| **AI 模型** | 通义千问 qwen-max | 阿里云大语言模型，支持中文优化 |
| **工具协议** | MCP 1.0.0-beta5 | Model Context Protocol，工具调用标准 |
| **响应式** | Reactor | 异步非阻塞编程模型 |
| **HTML 解析** | Jsoup 1.22.2 | 灵活的 HTML 解析库 |
| **构建工具** | Maven 3.8+ | 项目构建和依赖管理 |
| **部署平台** | 微信云托管 / Docker | 支持云原生部署 |

### 架构设计原则

- **模块化设计**：清晰的分层架构，各模块职责明确
- **响应式编程**：充分利用 Reactor 的异步能力，提升并发性能
- **可扩展性**：支持自定义工具、安全规则、知识库扩展
- **安全性**：多层安全防护，保护敏感数据和内容安全
- **云原生**：容器化部署，支持云平台弹性伸缩

## 📂 项目结构

```
ai-code-helper/
├── src/main/java/com/example/aicodehelper
│   ├── ai/                            # AI 核心模块
│   │   ├── AiCodeHelper.java         # AI 助手核心类
│   │   ├── AiCodeHelperFactory.java  # AI 服务工厂
│   │   ├── AiCodeHelperService.java  # AI 业务服务
│   │   ├── config/                    # AI 配置类
│   │   │   ├── QwenChatModelConfig.java  # 通义千问模型配置
│   │   │   ├── RagConfig.java             # RAG 知识库配置
│   │   │   └── McpConfig.java             # MCP 工具协议配置
│   │   ├── guardrails/                # 安全护栏
│   │   │   └── ProfanityGuardrail.java   # 敏感词过滤
│   │   ├── listener/                  # 事件监听器
│   │   │   └── ChatModelListenerConfig.java  # 聊天模型监听配置
│   │   ├── rag/                       # RAG 检索增强
│   │   │   └── RagConfig.java            # 知识库检索配置
│   │   ├── tools/                     # Agent 工具
│   │   │   └── MathTools.java            # 数学计算工具
│   │   └── mcp/                       # MCP 协议
│   │       └── McpConfig.java            # MCP 配置
│   ├── config/                        # 全局配置
│   │   └── GlobalCorsConfig.java      # 跨域配置
│   ├── controller/                    # 接口层
│   │   └── AiController.java          # AI 接口控制器
│   └── AiCodeHelperApplication.java   # 启动类
├── src/main/resources/
│   ├── application.yml               # 主配置文件
│   ├── application-local.yml        # 本地开发环境配置
│   ├── application-prod.yml         # 生产环境配置
│   └── static/                       # 静态资源
├── Dockerfile                        # Docker 镜像构建文件
├── pom.xml                          # Maven 构建配置
└── README.md                        # 项目文档
```

### 核心模块说明

- **ai/ai-core**：AI 能力核心模块，包含对话、工具调用、RAG 等核心功能
- **ai/config**：AI 模型、工具、知识库等配置管理
- **ai/guardrails**：内容安全防护，敏感词检测和过滤
- **ai/tools**：Agent 工具集，支持自定义扩展
- **controller**：RESTful API 接口层
- **config**：全局配置，如跨域、端口等

## ⚙️ 环境配置

### 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 21+ | 必须使用 JDK 21，项目使用了虚拟线程等新特性 |
| Maven | 3.8+ | 项目构建和依赖管理 |
| IDE | IntelliJ IDEA 2023+ | 推荐使用 IDEA，需要安装 Lombok 插件 |
| Docker | 20.10+ | 可选，用于容器化部署 |
| 内存 | 4GB+ | 建议 8GB+，运行 AI 模型需要较多内存 |

### IDE 配置步骤

> ⚠️ **JDK 版本不匹配为高频报错！** 下面三处环境必须统一设置为 **JDK‑21**

1. **项目设置** `File → Project Structure → Project`
   - Project SDK: `JDK‑21`
   - Language level: `21`

2. **模块设置** `Modules`
   - Module SDK: `JDK‑21`
   - Language level: `21`

3. **Maven 运行时** `Settings → Build Tools → Maven → Runner`
   - JRE: `Project JDK‑21`

### 本地开发配置（application-local.yml）

>
> 线上部署（微信云托管）密钥请使用**控制台环境变量注入**，禁止硬编码密钥到代码包

```yaml
# 应用服务 WEB 访问端口
server:
  port: 8081
  servlet:
    context-path: /api

# 大模型 API 配置
langChain4j:
  community:
    dashscope:
      chat-model:
        model-name: qwen-max
        api-key: "sk-你的通义千问API密钥"
        temperature: 0.7  # 温度参数，控制回答的随机性
        max-tokens: 2000  # 最大令牌数
        timeout: 60s      # 请求超时时间

# MCP 工具协议配置
mcp:
  baiduZP:
    apiKey: "你的百度智能云API密钥"
```

### 生产环境配置（application-prod.yml）

>
> 🚨 **安全提醒**：生产环境禁止硬编码密钥到代码包，请使用**云平台环境变量注入**

```yaml
# 应用服务 WEB 访问端口（微信云托管通常需要 80 端口）
server:
  port: 80
  servlet:
    context-path: /api

# 大模型 API 配置（从环境变量读取）
langChain4j:
  community:
    dashscope:
      chat-model:
        model-name: qwen-max
        api-key: ${LANGCHAIN4J_API_KEY}
        temperature: 0.7
        max-tokens: 2000
        timeout: 60s
      embedding-model:
        model-name: text-embedding-async-v1
        api-key: ${LANGCHAIN4J_API_KEY}
      streaming-chat-model:
        model-name: qwen-max
        api-key: ${LANGCHAIN4J_API_KEY}

# MCP 工具协议配置
mcp:
  baiduZP:
    apiKey: ${BAIDUZP_API_KEY}  # 百度智能云 API Key
```

#### 微信云托管环境变量配置

| 环境变量名 | 说明 | 示例值 |
|-----------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 激活的环境配置 | `prod` |
| `LANGCHAIN4J_API_KEY` | 通义千问 API 密钥 | `sk-xxx...` |
| `BAIDUZP_API_KEY` | 百度智能云 API 密钥 | `xxx...` |
| `SERVER_PORT` | 服务端口（可选） | `80` |

## 🚀 快速开始

### 方式一：IDEA 直接运行（推荐开发调试）

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/ai-code-helper.git
   cd ai-code-helper
   ```

2. **配置 API 密钥**
   - 修改 `src/main/resources/application-local.yml`
   - 填入你的通义千问 API 密钥

3. **启动项目**
   - 找到启动类：`com.example.aicodehelper.AiCodeHelperApplication`
   - 右键 → `Run` 即可启动项目

4. **验证服务**
   ```bash
   curl http://localhost:8081/api/ai/chat?memoryId=1&message=你好
   ```

### 方式二：Maven 命令行运行

```bash
# 清理并打包（跳过单元测试）
mvn clean package -DskipTests

# 运行生成的可执行 Jar 包
java -jar target/ai-code-helper-0.0.1-SNAPSHOT.jar

# 指定环境运行
java -jar -Dspring.profiles.active=prod target/ai-code-helper-0.0.1-SNAPSHOT.jar
```

### 方式三：Docker 容器化运行

```bash
# 构建镜像
docker build -t ai-code-helper:latest .

# 运行容器
docker run -d \
  -p 8081:8081 \
  -e LANGCHAIN4J_API_KEY="你的API密钥" \
  --name ai-code-helper \
  ai-code-helper:latest
```

## 📡 API 接口文档

### 基础信息

- **基础 URL**：`http://localhost:8081/api`
- **数据格式**：JSON
- **字符编码**：UTF-8

### 对话接口

#### 流式对话（SSE）

**接口地址**：`GET /ai/chat`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| memoryId | Integer | 是 | 会话记忆 ID，用于区分不同会话 |
| message | String | 是 | 用户消息内容 |

**请求示例**：
```bash
curl -N "http://localhost:8081/api/ai/chat?memoryId=1&message=帮我写一个Spring Boot的Hello World"
```

**响应格式**：Server-Sent Events (SSE) 流式响应

**响应示例**：
```
data: 好的，我来帮你编写一个 Spring Boot 的 Hello World 程序。

data: 首先，我们需要创建一个 Spring Boot 应用程序。

data: 下面是一个简单的 Hello World 控制器示例：

data: ```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
```
```

**响应代码**：

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功，返回流式数据 |
| 400 | 请求参数错误 |
| 500 | 服务器内部错误 |

## 🚢 部署指南

### 微信云托管部署

1. **项目准备**
   ```bash
   # 确保代码已提交到 git 仓库
   git add .
   git commit -m "准备部署到微信云托管"
   git push
   ```

2. **微信云托管配置**

   - **服务配置**
     - 运行环境：Java 21
     - 构建命令：`mvn clean package -DskipTests`
     - 启动命令：`java -jar app.jar`
     - 端口映射：80

   - **环境变量配置**
     ```yaml
     SPRING_PROFILES_ACTIVE: prod
     LANGCHAIN4J_API_KEY: 你的通义千问API密钥
     BAIDUZP_API_KEY: 你的百度智能云API密钥
     ```

3. **部署验证**
   ```bash
   # 测试部署后的服务
   curl https://你的服务地址/api/ai/chat?memoryId=1&message=你好
   ```

### 传统服务器部署

```bash
# 1. 上传 jar 包到服务器
scp target/ai-code-helper-0.0.1-SNAPSHOT.jar user@server:/app/

# 2. 创建启动脚本
cat > /app/start.sh << 'EOF'
#!/bin/bash
export SPRING_PROFILES_ACTIVE=prod
export LANGCHAIN4J_API_KEY="你的API密钥"
export BAIDUZP_API_KEY="你的百度API密钥"
java -Xms512m -Xmx2g -jar ai-code-helper-0.0.1-SNAPSHOT.jar
EOF

# 3. 赋予执行权限并启动
chmod +x /app/start.sh
nohup /app/start.sh > /app/logs/app.log 2>&1 &
```

## 🐛 常见问题与解决方案

| 问题类型 | 症状 | 解决方案 |
|---------|------|----------|
| 🔴 **JDK 版本报错** | `Unsupported class file major version 65` | 确保 pom.xml、项目设置、Maven 运行时都配置为 JDK 21 |
| 📄 **PDF 加载失败** | `FileSystemDocumentLoader` 无法读取文件 | 使用绝对路径测试；文件名避免中文、空格、特殊符号；路径基准为项目根目录 |
| 🧱 **Jar 包无法运行** | `no main manifest attribute` | 检查 `spring-boot-maven-plugin` 插件配置，确保没有 `<skip>true</skip>` |
| 🔒 **安全护栏未生效** | 敏感词未被过滤 | 确认 `ProfanityGuardrail` 已正确注册注入到 `AiService` |
| ☁️ **微信云托管 API Key 失效** | 返回 401 或认证失败 | 环境变量名必须大写 + 下划线；修改环境变量后**必须重新发布新版本** |
| 🌐 **CORS 跨域问题** | 浏览器报跨域错误 | 检查 `GlobalCorsConfig.java` 中的域名配置是否包含前端地址 |
| 🔄 **流式响应不工作** | SSE 连接断开或无数据 | 检查防火墙配置，确保支持长连接；验证 Reactor 配置是否正确 |
| 🧠 **RAG 检索无结果** | 知识库问答返回空结果 | 检查 PDF 文档是否正确加载；验证向量模型配置；调整相似度阈值 |

### 调试技巧

1. **启用详细日志**
   ```yaml
   # application.yml
   logging:
     level:
       dev.langchain4j: DEBUG
       com.example.aicodehelper: DEBUG
   ```

2. **监控内存使用**
   ```bash
   # 启动时添加 JVM 参数
   java -Xms512m -Xmx2g -XX:+PrintGCDetails -jar app.jar
   ```

3. **查看请求日志**
   ```bash
   # 查看实时日志
   tail -f logs/app.log

   # 搜索错误日志
   grep "ERROR" logs/app.log
   ```

## 📚 学习资源

### 官方文档
- [LangChain4j 官方文档](https://docs.langchain4j.dev/) - 最权威的 LangChain4j 使用指南
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot) - Spring Boot 官方教程
- [通义千问 API 文档](https://help.aliyun.com/zh/dashscope/) - 阿里云大模型 API 文档

### 教程与视频
- [鱼皮 LangChain4j Java AI 零基础实战教程](https://github.com/liyupi) - 本项目的教程来源
- [Spring Boot 响应式编程指南](https://spring.io/guides) - WebFlux 相关教程
- [MCP 协议规范](https://modelcontextprotocol.io/) - Model Context Protocol 标准

### 社区资源
- [LangChain4j GitHub](https://github.com/langchain4j/langchain4j) - 源码仓库
- [Spring 中文社区](https://springcn.io/) - Spring 中文技术社区
- [Stack Overflow - LangChain4j](https://stackoverflow.com/questions/tagged/langchain4j) - 问答社区

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. **Fork 项目**
   ```bash
   # 点击 GitHub 页面的 Fork 按钮
   git clone https://github.com/your-username/ai-code-helper.git
   ```

2. **创建功能分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **提交变更**
   ```bash
   git add .
   git commit -m "Add your feature description"
   ```

4. **推送分支**
   ```bash
   git push origin feature/your-feature-name
   ```

5. **提交 Pull Request**
   - 在 GitHub 上创建 Pull Request
   - 详细描述你的变更内容
   - 等待代码审查

### 开发规范

- **代码风格**：遵循阿里巴巴 Java 开发手册
- **提交信息**：使用约定式提交格式（Conventional Commits）
  - `feat:` 新功能
  - `fix:` 修复 bug
  - `docs:` 文档更新
  - `style:` 代码格式调整
  - `refactor:` 代码重构
  - `test:` 测试相关
  - `chore:` 构建/工具相关

### Bug 反馈

如果你发现了 bug，请提交 Issue 并包含以下信息：

- 问题描述和复现步骤
- 期望行为和实际行为
- 运行环境（操作系统、JDK 版本等）
- 相关日志和错误信息
- 如果可能，提供最小复现示例

## 🗺️ 项目路线图

- [x] 基础对话功能
- [x] 流式响应（SSE）
- [x] 安全护栏机制
- [x] Agent 工具调用
- [x] RAG 知识库
- [x] 微信云托管部署
- [ ] 多模型支持（OpenAI、Claude 等）
- [ ] 多轮对话记忆优化
- [ ] 图片识别与理解
- [ ] 代码执行环境
- [ ] 性能监控与优化
- [ ] 国际化支持
- [ ] 移动端适配

## 📄 许可证

本项目采用 **MIT 协议** 开源，欢迎自由使用、修改、分发。

```
MIT License

Copyright (c) 2024 AI-Code-Helper Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


## 🙏 致谢

感谢以下开源项目和技术社区的支持：

- [LangChain4j](https://github.com/langchain4j/langchain4j) - 强大的 Java AI 开发框架
- [Spring Boot](https://spring.io/projects/spring-boot) - 企业级 Java 开发框架
- [通义千问](https://tongyi.aliyun.com/) - 阿里云大语言模型
- [Reactor](https://projectreactor.io/) - 响应式编程库

特别感谢 **鱼皮** 提供的 LangChain4j 实战教程，为项目开发提供了重要指导。

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star 支持一下！**

Made with ❤️ by AI-Code-Helper Team

</div>