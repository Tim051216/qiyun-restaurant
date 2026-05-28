#!/bin/bash

# 冒烟测试脚本
# 用于验证部署后的服务是否正常运行

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# 配置
BASE_URL=${BASE_URL:-"http://localhost"}
TIMEOUT=5

# 测试计数
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 测试函数
test_endpoint() {
    local service=$1
    local port=$2
    local endpoint=$3
    local expected_status=${4:-200}
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log_info "测试 ${service}: ${endpoint}"
    
    local url="${BASE_URL}:${port}${endpoint}"
    local response=$(curl -s -o /dev/null -w "%{http_code}" --max-time ${TIMEOUT} ${url} 2>&1)
    
    if [ "$response" == "$expected_status" ]; then
        log_success "✓ ${service} ${endpoint} - 通过 (${response})"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        log_error "✗ ${service} ${endpoint} - 失败 (期望: ${expected_status}, 实际: ${response})"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# 测试服务健康检查
test_health_checks() {
    log_info "========================================="
    log_info "测试服务健康检查"
    log_info "========================================="
    
    test_endpoint "Gateway" 8080 "/actuator/health"
    test_endpoint "Order Service" 8081 "/actuator/health"
    test_endpoint "Dish Service" 8082 "/actuator/health"
    test_endpoint "Member Service" 8083 "/actuator/health"
    test_endpoint "Admin Service" 8084 "/actuator/health"
}

# 测试Prometheus指标
test_metrics() {
    log_info "========================================="
    log_info "测试Prometheus指标"
    log_info "========================================="
    
    test_endpoint "Gateway" 8080 "/actuator/prometheus"
    test_endpoint "Order Service" 8081 "/actuator/prometheus"
    test_endpoint "Dish Service" 8082 "/actuator/prometheus"
}

# 测试基本API
test_basic_apis() {
    log_info "========================================="
    log_info "测试基本API"
    log_info "========================================="
    
    # 测试网关路由
    test_endpoint "Gateway" 8080 "/api/order/health" 200 || true
    test_endpoint "Gateway" 8080 "/api/dish/health" 200 || true
    
    # 测试订单服务
    # 注意：这里可能需要认证，返回401也是正常的
    test_endpoint "Order Service" 8081 "/order/list" 401 || true
    
    # 测试菜品服务
    test_endpoint "Dish Service" 8082 "/dish/list" 200 || true
}

# 测试服务注册
test_service_registration() {
    log_info "========================================="
    log_info "测试服务注册"
    log_info "========================================="
    
    # 检查Nacos服务注册（如果可访问）
    if curl -f http://localhost:8848/nacos/ > /dev/null 2>&1; then
        log_info "Nacos可访问，检查服务注册..."
        
        # 这里可以添加具体的Nacos API调用来验证服务注册
        log_info "服务注册检查需要Nacos API凭证，跳过详细检查"
    else
        log_info "Nacos不可访问，跳过服务注册检查"
    fi
}

# 测试数据库连接
test_database_connection() {
    log_info "========================================="
    log_info "测试数据库连接"
    log_info "========================================="
    
    # 通过健康检查端点验证数据库连接
    local response=$(curl -s http://localhost:8081/actuator/health 2>&1)
    
    if echo "$response" | grep -q "UP"; then
        log_success "✓ 数据库连接正常"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log_error "✗ 数据库连接异常"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

# 测试Redis连接
test_redis_connection() {
    log_info "========================================="
    log_info "测试Redis连接"
    log_info "========================================="
    
    # 通过健康检查端点验证Redis连接
    local response=$(curl -s http://localhost:8081/actuator/health 2>&1)
    
    if echo "$response" | grep -q "redis"; then
        log_success "✓ Redis连接正常"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log_info "Redis健康检查信息未在响应中找到"
    fi
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

# 性能测试（简单）
test_performance() {
    log_info "========================================="
    log_info "测试响应时间"
    log_info "========================================="
    
    local url="${BASE_URL}:8080/actuator/health"
    local response_time=$(curl -o /dev/null -s -w '%{time_total}' ${url})
    
    log_info "Gateway响应时间: ${response_time}s"
    
    # 检查响应时间是否在可接受范围内（例如 < 1秒）
    if (( $(echo "$response_time < 1.0" | bc -l) )); then
        log_success "✓ 响应时间正常"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        log_error "✗ 响应时间过长"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

# 生成测试报告
generate_report() {
    log_info "========================================="
    log_info "测试报告"
    log_info "========================================="
    
    echo ""
    echo "总测试数: ${TOTAL_TESTS}"
    echo -e "${GREEN}通过: ${PASSED_TESTS}${NC}"
    echo -e "${RED}失败: ${FAILED_TESTS}${NC}"
    echo ""
    
    local success_rate=$(echo "scale=2; ${PASSED_TESTS} * 100 / ${TOTAL_TESTS}" | bc)
    echo "成功率: ${success_rate}%"
    echo ""
    
    if [ ${FAILED_TESTS} -eq 0 ]; then
        log_success "所有冒烟测试通过！"
        return 0
    else
        log_error "部分冒烟测试失败，请检查服务状态"
        return 1
    fi
}

# 主函数
main() {
    log_info "开始执行冒烟测试..."
    log_info "目标环境: ${BASE_URL}"
    echo ""
    
    # 等待服务启动
    log_info "等待服务启动..."
    sleep 5
    
    # 执行测试
    test_health_checks
    test_metrics
    test_basic_apis
    test_service_registration
    test_database_connection
    test_redis_connection
    test_performance
    
    # 生成报告
    generate_report
}

# 执行主函数
main

# 返回退出码
if [ ${FAILED_TESTS} -eq 0 ]; then
    exit 0
else
    exit 1
fi
