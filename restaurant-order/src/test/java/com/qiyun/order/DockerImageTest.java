package com.qiyun.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Docker镜像构建验证测试
 * 
 * 验证Dockerfile配置的正确性和镜像构建的可行性
 */
@Tag("docker")
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
public class DockerImageTest {
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Dockerfile Validation")
    public void testDockerfileExists() {
        // 验证Dockerfile文件存在
        Path dockerfilePath = Paths.get("Dockerfile");
        assertTrue(Files.exists(dockerfilePath), "Dockerfile应该存在");
        
        // 验证Dockerfile不为空
        try {
            long fileSize = Files.size(dockerfilePath);
            assertTrue(fileSize > 0, "Dockerfile不应该为空");
        } catch (Exception e) {
            fail("无法读取Dockerfile: " + e.getMessage());
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Dockerfile Content")
    public void testDockerfileContent() throws Exception {
        // 读取Dockerfile内容
        Path dockerfilePath = Paths.get("Dockerfile");
        String content = Files.readString(dockerfilePath);
        
        // 验证多阶段构建
        assertTrue(content.contains("FROM maven:3.9-eclipse-temurin-21 AS builder"), 
            "应该使用Maven 3.9和Java 21作为构建阶段");
        assertTrue(content.contains("FROM eclipse-temurin:21-jre-alpine"), 
            "应该使用Java 21 JRE Alpine作为运行阶段");
        
        // 验证健康检查
        assertTrue(content.contains("HEALTHCHECK"), "应该配置健康检查");
        assertTrue(content.contains("/actuator/health"), "健康检查应该使用actuator端点");
        
        // 验证JVM参数
        assertTrue(content.contains("JAVA_OPTS"), "应该配置JVM参数");
        assertTrue(content.contains("-XX:+UseZGC"), "应该使用ZGC垃圾收集器");
        assertTrue(content.contains("-Xms512m"), "应该配置最小堆内存");
        assertTrue(content.contains("-Xmx1024m"), "应该配置最大堆内存");
        
        // 验证端口暴露
        assertTrue(content.contains("EXPOSE 8081"), "应该暴露8081端口");
        
        // 验证安全性（非root用户）
        assertTrue(content.contains("adduser"), "应该创建非root用户");
        assertTrue(content.contains("USER spring:spring"), "应该使用非root用户运行");
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Dockerignore Validation")
    public void testDockerignoreExists() {
        // 验证.dockerignore文件存在
        Path dockerignorePath = Paths.get(".dockerignore");
        assertTrue(Files.exists(dockerignorePath), ".dockerignore应该存在");
        
        try {
            String content = Files.readString(dockerignorePath);
            
            // 验证忽略target目录
            assertTrue(content.contains("target/"), "应该忽略target目录");
            
            // 验证忽略IDE配置
            assertTrue(content.contains(".idea/") || content.contains("*.iml"), 
                "应该忽略IDE配置文件");
        } catch (Exception e) {
            fail("无法读取.dockerignore: " + e.getMessage());
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Build Context Size")
    public void testBuildContextSize() throws Exception {
        // 验证构建上下文大小合理（排除target目录）
        Path projectRoot = Paths.get(".");
        long totalSize = calculateDirectorySize(projectRoot.toFile(), true);
        
        // 构建上下文应该小于100MB（排除target）
        long maxSize = 100 * 1024 * 1024; // 100MB
        assertTrue(totalSize < maxSize, 
            String.format("构建上下文大小应该小于100MB，当前: %.2fMB", totalSize / 1024.0 / 1024.0));
    }
    
    /**
     * 计算目录大小
     */
    private long calculateDirectorySize(File directory, boolean excludeTarget) {
        long size = 0;
        
        if (directory.isFile()) {
            return directory.length();
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                // 排除target目录
                if (excludeTarget && file.getName().equals("target")) {
                    continue;
                }
                // 排除隐藏目录
                if (file.getName().startsWith(".")) {
                    continue;
                }
                
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += calculateDirectorySize(file, excludeTarget);
                }
            }
        }
        
        return size;
    }
}
