# 构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 添加非root用户
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 复制jar包
COPY --from=builder /app/target/*.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# JVM参数优化
ENV JAVA_OPTS="-XX:+UseZGC \
  -XX:+UseStringDeduplication \
  -Xms512m \
  -Xmx1024m \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0"

# 启动应用
ENV SKYWALKING_AGENT_ENABLED="false"
ENV SKYWALKING_AGENT_PATH="/skywalking/agent/skywalking-agent.jar"
ENV SKYWALKING_AGENT_OPTS=""
ENTRYPOINT ["sh", "-c", "if [ \"$SKYWALKING_AGENT_ENABLED\" = \"true\" ] && [ -f \"$SKYWALKING_AGENT_PATH\" ]; then exec java -javaagent:$SKYWALKING_AGENT_PATH $SKYWALKING_AGENT_OPTS $JAVA_OPTS -jar app.jar; else exec java $JAVA_OPTS -jar app.jar; fi"]

# 暴露端口
EXPOSE 8084
