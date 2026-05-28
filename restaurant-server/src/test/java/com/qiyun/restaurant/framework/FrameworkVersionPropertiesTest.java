package com.qiyun.restaurant.framework;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.SpringBootVersion;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * 框架版本验证属性测试
 * 
 * 本测试类验证系统使用正确的Spring Boot和Java版本
 * 使用jqwik进行属性测试，每个属性至少运行100次迭代
 */
public class FrameworkVersionPropertiesTest {

    private static final String EXPECTED_SPRING_BOOT_MAJOR_VERSION = "3";
    private static final String EXPECTED_SPRING_BOOT_MINOR_VERSION = "2";
    private static final String EXPECTED_JAVA_VERSION = "21";

    private String springBootVersion;
    private String javaVersion;

    @BeforeProperty
    void setUp() {
        // 获取Spring Boot版本
        springBootVersion = SpringBootVersion.getVersion();
        
        // 获取Java版本
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        javaVersion = runtimeMXBean.getSpecVersion();
    }

    /**
     * Property 1: Spring Boot版本验证
     * 
     * **Validates: Requirements 1.1**
     * 
     * For any 系统启动实例，Spring Boot版本应该是3.2.x系列
     * 
     * 验证策略：
     * - 检查Spring Boot主版本号为3
     * - 检查Spring Boot次版本号为2
     * - 确保版本字符串格式正确
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 1: Spring Boot版本验证")
    void springBootVersionShouldBe3_2_x(@ForAll("systemStartupScenarios") int scenario) {
        // 验证Spring Boot版本不为空
        Assume.that(springBootVersion != null && !springBootVersion.isEmpty());
        
        // 解析版本号
        String[] versionParts = springBootVersion.split("\\.");
        
        // 验证版本格式：至少包含主版本号和次版本号
        Assume.that(versionParts.length >= 2);
        
        String majorVersion = versionParts[0];
        String minorVersion = versionParts[1];
        
        // Property: Spring Boot主版本应该是3
        assert majorVersion.equals(EXPECTED_SPRING_BOOT_MAJOR_VERSION) : 
            String.format("Spring Boot主版本应该是%s，实际是%s", 
                EXPECTED_SPRING_BOOT_MAJOR_VERSION, majorVersion);
        
        // Property: Spring Boot次版本应该是2
        assert minorVersion.equals(EXPECTED_SPRING_BOOT_MINOR_VERSION) : 
            String.format("Spring Boot次版本应该是%s，实际是%s", 
                EXPECTED_SPRING_BOOT_MINOR_VERSION, minorVersion);
        
        // 验证完整版本字符串以3.2开头
        assert springBootVersion.startsWith("3.2") : 
            String.format("Spring Boot版本应该以3.2开头，实际版本是%s", springBootVersion);
    }

    /**
     * Property 2: Java版本验证
     * 
     * **Validates: Requirements 1.2**
     * 
     * For any 系统运行实例，Java运行时版本应该是21
     * 
     * 验证策略：
     * - 检查Java规范版本为21
     * - 确保使用Java 21 LTS版本
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 2: Java版本验证")
    void javaVersionShouldBe21(@ForAll("systemStartupScenarios") int scenario) {
        // 验证Java版本不为空
        Assume.that(javaVersion != null && !javaVersion.isEmpty());
        
        // Property: Java版本应该是21
        assert javaVersion.equals(EXPECTED_JAVA_VERSION) : 
            String.format("Java版本应该是%s，实际是%s", 
                EXPECTED_JAVA_VERSION, javaVersion);
        
        // 额外验证：检查系统属性中的Java版本
        String javaRuntimeVersion = System.getProperty("java.version");
        assert javaRuntimeVersion != null : "Java运行时版本不应为空";
        assert javaRuntimeVersion.startsWith("21") : 
            String.format("Java运行时版本应该以21开头，实际版本是%s", javaRuntimeVersion);
    }

    /**
     * 提供系统启动场景的生成器
     * 
     * 由于版本检查是静态的（不依赖于输入），我们生成多个场景来模拟
     * 不同的系统启动实例，确保属性在多次迭代中保持一致
     * 
     * @return 系统启动场景的任意值生成器
     */
    @Provide
    Arbitrary<Integer> systemStartupScenarios() {
        // 生成1-100之间的场景编号，模拟不同的系统启动实例
        return Arbitraries.integers().between(1, 100);
    }

    /**
     * 额外的版本一致性验证
     * 
     * 验证在多次查询中，版本信息保持一致
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 1: Spring Boot版本验证")
    void versionsShouldBeConsistentAcrossMultipleQueries(@ForAll("systemStartupScenarios") int scenario) {
        // 多次获取版本信息
        String version1 = SpringBootVersion.getVersion();
        String version2 = SpringBootVersion.getVersion();
        
        // Property: 版本信息应该保持一致
        assert version1.equals(version2) : 
            "多次查询的Spring Boot版本应该保持一致";
        
        // 验证Java版本一致性
        RuntimeMXBean runtime1 = ManagementFactory.getRuntimeMXBean();
        RuntimeMXBean runtime2 = ManagementFactory.getRuntimeMXBean();
        
        assert runtime1.getSpecVersion().equals(runtime2.getSpecVersion()) : 
            "多次查询的Java版本应该保持一致";
    }

    /**
     * 版本格式验证
     * 
     * 验证版本字符串符合语义化版本规范
     */
    @Property(tries = 100)
    @Tag("Feature: restaurant-tech-stack-upgrade, Property 1: Spring Boot版本验证")
    void versionFormatShouldBeValid(@ForAll("systemStartupScenarios") int scenario) {
        Assume.that(springBootVersion != null);
        
        // Property: 版本应该符合语义化版本格式 (major.minor.patch)
        String versionPattern = "^\\d+\\.\\d+\\.\\d+.*$";
        assert springBootVersion.matches(versionPattern) : 
            String.format("Spring Boot版本格式应该符合语义化版本规范，实际版本是%s", 
                springBootVersion);
    }
}
