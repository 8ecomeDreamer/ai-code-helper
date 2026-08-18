# ---------------------- 构建阶段 ----------------------
FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /app

# 优先复制pom.xml，缓存依赖层（加速构建）
COPY pom.xml .
RUN mvn dependency:go-offline

# 复制全部源码
COPY src ./src
COPY settings.xml /root/.m2/settings.xml

# 打包跳过测试
RUN mvn package -DskipTests

# ---------------------- 运行阶段（最终镜像） ----------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# 从构建阶段拷贝打好的jar包
COPY --from=builder /app/target/ai-code-helper-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
