package com.qiyun.dish;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Docker镜像构建验证测试 - Dish Service
 */
@Tag("docker")
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
public class DockerImageTest {
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Dockerfile Validation")
    public void testDockerfileExists() {
        Path dockerfilePath = Paths.get("Dockerfile");
        assertTrue(Files.exists(dockerfilePath), "Dockerfile应该存在");
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Dockerfile Content")
    public void testDockerfileContent() throws Exception {
        Path dockerfilePath = Paths.get("Dockerfile");
        String content = Files.readString(dockerfilePath);
        
        assertTrue(content.contains("FROM maven:3.9-eclipse-temurin-21 AS builder"), 
            "应该使用Maven 3.9和Java 21作为构建阶段");
        assertTrue(content.contains("FROM eclipse-temurin:21-jre-alpine"), 
            "应该使用Java 21 JRE Alpine作为运行阶段");
        assertTrue(content.contains("HEALTHCHECK"), "应该配置健康检查");
        assertTrue(content.contains("EXPOSE 8082"), "应该暴露8082端口");
        assertTrue(content.contains("USER spring:spring"), "应该使用非root用户运行");
    }
}
