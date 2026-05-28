package com.qiyun.restaurant.docker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Docker Compose配置验证测试
 * 
 * 验证docker-compose.yml配置的正确性和完整性
 */
@Tag("docker")
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true")
public class DockerComposeTest {
    
    private static final String DOCKER_COMPOSE_FILE = "docker-compose-full.yml";
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Docker Compose File Exists")
    public void testDockerComposeFileExists() {
        Path composePath = Paths.get(DOCKER_COMPOSE_FILE);
        assertTrue(Files.exists(composePath), "docker-compose-full.yml应该存在");
        
        try {
            long fileSize = Files.size(composePath);
            assertTrue(fileSize > 0, "docker-compose-full.yml不应该为空");
        } catch (Exception e) {
            fail("无法读取docker-compose-full.yml: " + e.getMessage());
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Docker Compose Services")
    public void testDockerComposeServices() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(DOCKER_COMPOSE_FILE)) {
            Map<String, Object> compose = yaml.load(inputStream);
            
            // 验证版本
            assertTrue(compose.containsKey("version"), "应该包含version字段");
            String version = (String) compose.get("version");
            assertTrue(version.startsWith("3."), "应该使用Docker Compose 3.x版本");
            
            // 验证services
            assertTrue(compose.containsKey("services"), "应该包含services字段");
            @SuppressWarnings("unchecked")
            Map<String, Object> services = (Map<String, Object>) compose.get("services");
            
            // 验证基础设施服务
            assertTrue(services.containsKey("mysql-master"), "应该包含mysql-master服务");
            assertTrue(services.containsKey("redis"), "应该包含redis服务");
            assertTrue(services.containsKey("rabbitmq"), "应该包含rabbitmq服务");
            assertTrue(services.containsKey("nacos"), "应该包含nacos服务");
            
            // 验证应用服务
            assertTrue(services.containsKey("gateway"), "应该包含gateway服务");
            assertTrue(services.containsKey("order-service"), "应该包含order-service服务");
            assertTrue(services.containsKey("dish-service"), "应该包含dish-service服务");
            assertTrue(services.containsKey("member-service"), "应该包含member-service服务");
            assertTrue(services.containsKey("admin-service"), "应该包含admin-service服务");
            
            // 验证监控服务
            assertTrue(services.containsKey("prometheus"), "应该包含prometheus服务");
            assertTrue(services.containsKey("grafana"), "应该包含grafana服务");
            
            // 验证服务总数
            assertTrue(services.size() >= 11, "应该至少包含11个服务");
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Service Health Checks")
    public void testServiceHealthChecks() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(DOCKER_COMPOSE_FILE)) {
            Map<String, Object> compose = yaml.load(inputStream);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> services = (Map<String, Object>) compose.get("services");
            
            // 验证基础设施服务的健康检查
            verifyHealthCheck(services, "mysql-master");
            verifyHealthCheck(services, "redis");
            verifyHealthCheck(services, "rabbitmq");
            verifyHealthCheck(services, "nacos");
            
            // 验证应用服务的健康检查
            verifyHealthCheck(services, "gateway");
            verifyHealthCheck(services, "order-service");
            verifyHealthCheck(services, "dish-service");
            verifyHealthCheck(services, "member-service");
            verifyHealthCheck(services, "admin-service");
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Service Dependencies")
    public void testServiceDependencies() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(DOCKER_COMPOSE_FILE)) {
            Map<String, Object> compose = yaml.load(inputStream);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> services = (Map<String, Object>) compose.get("services");
            
            // 验证Gateway依赖
            @SuppressWarnings("unchecked")
            Map<String, Object> gateway = (Map<String, Object>) services.get("gateway");
            assertTrue(gateway.containsKey("depends_on"), "gateway应该配置depends_on");
            @SuppressWarnings("unchecked")
            Map<String, Object> gatewayDeps = (Map<String, Object>) gateway.get("depends_on");
            assertTrue(gatewayDeps.containsKey("nacos"), "gateway应该依赖nacos");
            assertTrue(gatewayDeps.containsKey("redis"), "gateway应该依赖redis");
            
            // 验证Order Service依赖
            @SuppressWarnings("unchecked")
            Map<String, Object> orderService = (Map<String, Object>) services.get("order-service");
            assertTrue(orderService.containsKey("depends_on"), "order-service应该配置depends_on");
            @SuppressWarnings("unchecked")
            Map<String, Object> orderDeps = (Map<String, Object>) orderService.get("depends_on");
            assertTrue(orderDeps.containsKey("mysql-master"), "order-service应该依赖mysql-master");
            assertTrue(orderDeps.containsKey("redis"), "order-service应该依赖redis");
            assertTrue(orderDeps.containsKey("rabbitmq"), "order-service应该依赖rabbitmq");
            assertTrue(orderDeps.containsKey("nacos"), "order-service应该依赖nacos");
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Service Ports")
    public void testServicePorts() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(DOCKER_COMPOSE_FILE)) {
            Map<String, Object> compose = yaml.load(inputStream);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> services = (Map<String, Object>) compose.get("services");
            
            // 验证端口映射
            verifyPort(services, "gateway", "8080");
            verifyPort(services, "order-service", "8081");
            verifyPort(services, "dish-service", "8082");
            verifyPort(services, "member-service", "8083");
            verifyPort(services, "admin-service", "8084");
            verifyPort(services, "mysql-master", "3306");
            verifyPort(services, "redis", "6379");
            verifyPort(services, "rabbitmq", "5672");
            verifyPort(services, "nacos", "8848");
            verifyPort(services, "prometheus", "9090");
            verifyPort(services, "grafana", "3000");
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Service Networks")
    public void testServiceNetworks() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(DOCKER_COMPOSE_FILE)) {
            Map<String, Object> compose = yaml.load(inputStream);
            
            // 验证networks定义
            assertTrue(compose.containsKey("networks"), "应该定义networks");
            @SuppressWarnings("unchecked")
            Map<String, Object> networks = (Map<String, Object>) compose.get("networks");
            assertTrue(networks.containsKey("restaurant-network"), "应该定义restaurant-network");
            
            // 验证所有服务都在同一网络
            @SuppressWarnings("unchecked")
            Map<String, Object> services = (Map<String, Object>) compose.get("services");
            
            for (String serviceName : services.keySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> service = (Map<String, Object>) services.get(serviceName);
                assertTrue(service.containsKey("networks"), 
                    serviceName + "应该配置networks");
                
                @SuppressWarnings("unchecked")
                List<String> serviceNetworks = (List<String>) service.get("networks");
                assertTrue(serviceNetworks.contains("restaurant-network"), 
                    serviceName + "应该在restaurant-network网络中");
            }
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Service Volumes")
    public void testServiceVolumes() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(DOCKER_COMPOSE_FILE)) {
            Map<String, Object> compose = yaml.load(inputStream);
            
            // 验证volumes定义
            assertTrue(compose.containsKey("volumes"), "应该定义volumes");
            @SuppressWarnings("unchecked")
            Map<String, Object> volumes = (Map<String, Object>) compose.get("volumes");
            
            // 验证数据持久化卷
            assertTrue(volumes.containsKey("mysql-master-data"), "应该定义mysql-master-data卷");
            assertTrue(volumes.containsKey("redis-data"), "应该定义redis-data卷");
            assertTrue(volumes.containsKey("rabbitmq-data"), "应该定义rabbitmq-data卷");
            assertTrue(volumes.containsKey("nacos-data"), "应该定义nacos-data卷");
            assertTrue(volumes.containsKey("prometheus-data"), "应该定义prometheus-data卷");
            assertTrue(volumes.containsKey("grafana-data"), "应该定义grafana-data卷");
        }
    }
    
    @Test
    @Tag("Feature: restaurant-tech-stack-upgrade, Property: Service Environment Variables")
    public void testServiceEnvironmentVariables() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(DOCKER_COMPOSE_FILE)) {
            Map<String, Object> compose = yaml.load(inputStream);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> services = (Map<String, Object>) compose.get("services");
            
            // 验证Gateway环境变量
            @SuppressWarnings("unchecked")
            Map<String, Object> gateway = (Map<String, Object>) services.get("gateway");
            assertTrue(gateway.containsKey("environment"), "gateway应该配置environment");
            @SuppressWarnings("unchecked")
            Map<String, Object> gatewayEnv = (Map<String, Object>) gateway.get("environment");
            assertTrue(gatewayEnv.containsKey("NACOS_SERVER_ADDR"), "gateway应该配置NACOS_SERVER_ADDR");
            
            // 验证Order Service环境变量
            @SuppressWarnings("unchecked")
            Map<String, Object> orderService = (Map<String, Object>) services.get("order-service");
            assertTrue(orderService.containsKey("environment"), "order-service应该配置environment");
            @SuppressWarnings("unchecked")
            Map<String, Object> orderEnv = (Map<String, Object>) orderService.get("environment");
            assertTrue(orderEnv.containsKey("MYSQL_HOST"), "order-service应该配置MYSQL_HOST");
            assertTrue(orderEnv.containsKey("REDIS_HOST"), "order-service应该配置REDIS_HOST");
            assertTrue(orderEnv.containsKey("RABBITMQ_HOST"), "order-service应该配置RABBITMQ_HOST");
        }
    }
    
    /**
     * 验证服务的健康检查配置
     */
    private void verifyHealthCheck(Map<String, Object> services, String serviceName) {
        @SuppressWarnings("unchecked")
        Map<String, Object> service = (Map<String, Object>) services.get(serviceName);
        assertNotNull(service, serviceName + "服务应该存在");
        assertTrue(service.containsKey("healthcheck"), 
            serviceName + "应该配置healthcheck");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> healthcheck = (Map<String, Object>) service.get("healthcheck");
        assertTrue(healthcheck.containsKey("test"), 
            serviceName + "的healthcheck应该包含test");
        assertTrue(healthcheck.containsKey("interval"), 
            serviceName + "的healthcheck应该包含interval");
        assertTrue(healthcheck.containsKey("timeout"), 
            serviceName + "的healthcheck应该包含timeout");
        assertTrue(healthcheck.containsKey("retries"), 
            serviceName + "的healthcheck应该包含retries");
    }
    
    /**
     * 验证服务的端口映射
     */
    private void verifyPort(Map<String, Object> services, String serviceName, String expectedPort) {
        @SuppressWarnings("unchecked")
        Map<String, Object> service = (Map<String, Object>) services.get(serviceName);
        assertNotNull(service, serviceName + "服务应该存在");
        assertTrue(service.containsKey("ports"), serviceName + "应该配置ports");
        
        @SuppressWarnings("unchecked")
        List<String> ports = (List<String>) service.get("ports");
        boolean foundPort = ports.stream()
            .anyMatch(port -> port.contains(expectedPort));
        assertTrue(foundPort, serviceName + "应该暴露端口" + expectedPort);
    }
}
