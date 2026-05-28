# CI/CD Pipeline 说明文档

## 概述

本项目使用GitHub Actions实现完整的CI/CD流程，包括代码质量检查、自动化测试、代码覆盖率检查、Docker镜像构建和自动部署。

## Pipeline 架构

```
代码提交
    ↓
代码质量检查 (Checkstyle, SpotBugs)
    ↓
单元测试 (5个服务并行)
    ↓
属性测试 (订单和菜品服务)
    ↓
集成测试 (使用Testcontainers)
    ↓
代码覆盖率检查 (JaCoCo, 80%阈值)
    ↓
Docker镜像构建和推送
    ↓
安全扫描 (Trivy)
    ↓
自动部署 (测试环境/生产环境)
    ↓
通知结果
```

## 触发条件

### 自动触发
- **Push到main分支**: 触发完整流程 + 生产环境部署
- **Push到develop分支**: 触发完整流程 + 测试环境部署
- **Pull Request**: 触发测试和代码质量检查

### 手动触发
可以在GitHub Actions页面手动触发workflow

## Jobs 说明

### 1. code-quality (代码质量检查)
- **运行时间**: ~2分钟
- **检查项**:
  - Checkstyle: 代码风格检查
  - SpotBugs: 静态代码分析，查找潜在bug
- **失败策略**: continue-on-error (不阻塞后续流程)

### 2. unit-tests (单元测试)
- **运行时间**: ~5分钟
- **并行策略**: 5个服务模块并行执行
- **测试范围**: 
  - 所有*Test.java文件
  - 排除集成测试和属性测试
- **产物**: 测试报告上传到Artifacts

### 3. property-tests (属性测试)
- **运行时间**: ~3分钟
- **测试服务**: restaurant-order, restaurant-dish
- **测试范围**: 所有*PropertiesTest.java文件
- **产物**: 属性测试报告上传到Artifacts

### 4. integration-tests (集成测试)
- **运行时间**: ~10分钟
- **依赖服务**: MySQL 8.0, Redis 7.2, RabbitMQ 3.12
- **测试范围**: 所有*IntegrationTest.java文件
- **环境变量**: 自动配置数据库和中间件连接
- **产物**: 集成测试报告上传到Artifacts

### 5. code-coverage (代码覆盖率)
- **运行时间**: ~5分钟
- **工具**: JaCoCo
- **阈值要求**:
  - 行覆盖率: ≥80%
  - 分支覆盖率: ≥75%
- **报告**: 
  - 上传到Codecov
  - 生成HTML报告到Artifacts

### 6. build-docker-images (Docker镜像构建)
- **运行时间**: ~15分钟
- **触发条件**: 仅在push到main或develop分支时执行
- **构建服务**: 5个微服务
- **镜像标签**:
  - `latest`: main分支
  - `develop`: develop分支
  - `{branch}-{sha}`: 带commit SHA的标签
- **优化**: 使用GitHub Actions缓存加速构建

### 7. security-scan (安全扫描)
- **运行时间**: ~5分钟
- **工具**: Trivy
- **扫描内容**: Docker镜像漏洞扫描
- **报告**: 上传到GitHub Security

### 8. deploy-test (测试环境部署)
- **触发条件**: push到develop分支
- **部署目标**: 测试环境
- **验证**: 冒烟测试

### 9. deploy-production (生产环境部署)
- **触发条件**: push到main分支
- **部署目标**: 生产环境
- **验证**: 冒烟测试
- **通知**: 部署完成通知

### 10. notify (通知)
- **运行时间**: ~1分钟
- **触发条件**: 所有测试job完成后
- **通知内容**: Pipeline执行结果

## 配置要求

### GitHub Secrets

需要在GitHub仓库设置以下Secrets:

```
DOCKER_USERNAME: Docker Hub用户名
DOCKER_PASSWORD: Docker Hub密码或访问令牌
```

配置路径: `Settings` → `Secrets and variables` → `Actions` → `New repository secret`

### Codecov (可选)

如果需要使用Codecov，需要:
1. 在 https://codecov.io 注册账号
2. 添加GitHub仓库
3. 获取CODECOV_TOKEN (公开仓库不需要)

## 本地测试

### 运行单元测试
```bash
# 所有服务
mvn clean test

# 单个服务
cd restaurant-order
mvn test -Dtest=*Test -DexcludeGroups=integration,property
```

### 运行属性测试
```bash
cd restaurant-order
mvn test -Dtest=*PropertiesTest
```

### 运行集成测试
```bash
# 需要先启动依赖服务
docker-compose up -d mysql redis rabbitmq

cd restaurant-order
mvn test -Dtest=*IntegrationTest
```

### 生成代码覆盖率报告
```bash
mvn clean test jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

### 检查代码覆盖率阈值
```bash
mvn jacoco:check
```

## 代码覆盖率配置

每个服务的pom.xml中已配置JaCoCo插件:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <!-- 准备代理 -->
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        
        <!-- 生成报告 -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        
        <!-- 检查阈值 -->
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.75</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Docker镜像构建

### 本地构建测试
```bash
# 构建单个服务
cd restaurant-order
mvn clean package -DskipTests
docker build -t restaurant-order:test .

# 运行容器
docker run -p 8081:8081 restaurant-order:test
```

### 多阶段构建优化
Dockerfile使用多阶段构建:
1. **构建阶段**: 使用Maven编译Java代码
2. **运行阶段**: 使用精简的JRE镜像运行应用

优势:
- 镜像体积小 (~200MB vs ~800MB)
- 构建速度快 (利用Maven缓存)
- 安全性高 (不包含构建工具)

## 性能优化

### 1. 并行执行
- 单元测试: 5个服务并行
- 属性测试: 2个服务并行
- 集成测试: 2个服务并行
- Docker构建: 5个服务并行

### 2. 缓存策略
- Maven依赖缓存
- Docker层缓存
- GitHub Actions缓存

### 3. 条件执行
- Docker构建: 仅在push时执行
- 部署: 仅在特定分支执行
- 安全扫描: 仅在镜像构建后执行

## 故障排查

### 测试失败
1. 查看GitHub Actions日志
2. 下载测试报告Artifacts
3. 本地复现问题

### 代码覆盖率不达标
```bash
# 查看详细覆盖率报告
mvn jacoco:report
open target/site/jacoco/index.html

# 找到未覆盖的代码
# 补充单元测试
```

### Docker构建失败
```bash
# 本地测试构建
docker build -t test .

# 查看构建日志
docker build --progress=plain -t test .
```

### 部署失败
1. 检查环境配置
2. 验证服务健康检查
3. 查看应用日志

## 最佳实践

### 1. 提交前检查
```bash
# 运行所有测试
mvn clean test

# 检查代码覆盖率
mvn jacoco:check

# 代码格式化
mvn checkstyle:check
```

### 2. Pull Request流程
1. 创建feature分支
2. 提交代码并推送
3. 创建PR到develop分支
4. 等待CI检查通过
5. Code Review
6. 合并到develop

### 3. 发布流程
1. develop分支测试通过
2. 创建PR到main分支
3. 等待CI检查通过
4. Code Review
5. 合并到main
6. 自动部署到生产环境

## 监控和告警

### GitHub Actions监控
- 查看workflow运行历史
- 分析失败原因
- 优化执行时间

### 代码覆盖率趋势
- Codecov Dashboard
- 覆盖率变化趋势
- 未覆盖代码热点

### Docker镜像安全
- Trivy扫描报告
- GitHub Security Alerts
- 漏洞修复建议

## 扩展功能

### 添加新的测试类型
在ci-cd.yml中添加新的job:
```yaml
new-test-type:
  name: New Test Type
  runs-on: ubuntu-latest
  steps:
    - name: Checkout code
      uses: actions/checkout@v4
    - name: Run tests
      run: mvn test -Dtest=*NewTest
```

### 添加新的部署环境
```yaml
deploy-staging:
  name: Deploy to Staging
  runs-on: ubuntu-latest
  if: github.ref == 'refs/heads/staging'
  environment:
    name: staging
    url: https://staging.restaurant.example.com
  steps:
    - name: Deploy
      run: ./deploy-staging.sh
```

### 集成通知服务
```yaml
- name: Send notification
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## 参考资料

- [GitHub Actions文档](https://docs.github.com/en/actions)
- [JaCoCo文档](https://www.jacoco.org/jacoco/trunk/doc/)
- [Testcontainers文档](https://www.testcontainers.org/)
- [Docker最佳实践](https://docs.docker.com/develop/dev-best-practices/)
- [Trivy文档](https://aquasecurity.github.io/trivy/)
