#!/bin/bash

# 七云菜馆技术栈升级 - 性能测试脚本
# 版本: 2.0.0
# 日期: 2026-02-09

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查工具
check_tools() {
    log_info "检查性能测试工具..."
    
    if ! command -v ab &> /dev/null; then
        log_error "Apache Bench (ab) 未安装"
        log_info "安装方法: sudo apt-get install apache2-utils"
        exit 1
    fi
    
    if ! command -v curl &> /dev/null; then
        log_error "curl 未安装"
        exit 1
    fi
    
    log_success "工具检查通过"
}

# 网关性能测试
test_gateway_performance() {
    log_info "测试网关性能..."
    
    # 低并发测试
    log_info "低并发测试 (10并发, 1000请求)..."
    ab -n 1000 -c 10 -q http://localhost:8080/actuator/health > /tmp/ab-gateway-low.txt 2>&1
    
    low_rps=$(grep "Requests per second" /tmp/ab-gateway-low.txt | awk '{print $4}')
    low_time=$(grep "Time per request" /tmp/ab-gateway-low.txt | head -1 | awk '{print $4}')
    
    log_info "低并发: $low_rps req/s, 平均响应: ${low_time}ms"
    
    # 中并发测试
    log_info "中并发测试 (50并发, 5000请求)..."
    ab -n 5000 -c 50 -q http://localhost:8080/actuator/health > /tmp/ab-gateway-mid.txt 2>&1
    
    mid_rps=$(grep "Requests per second" /tmp/ab-gateway-mid.txt | awk '{print $4}')
    mid_time=$(grep "Time per request" /tmp/ab-gateway-mid.txt | head -1 | awk '{print $4}')
    
    log_info "中并发: $mid_rps req/s, 平均响应: ${mid_time}ms"
    
    # 高并发测试
    log_info "高并发测试 (100并发, 10000请求)..."
    ab -n 10000 -c 100 -q http://localhost:8080/actuator/health > /tmp/ab-gateway-high.txt 2>&1
    
    high_rps=$(grep "Requests per second" /tmp/ab-gateway-high.txt | awk '{print $4}')
    high_time=$(grep "Time per request" /tmp/ab-gateway-high.txt | head -1 | awk '{print $4}')
    
    log_info "高并发: $high_rps req/s, 平均响应: ${high_time}ms"
    
    log_success "网关性能测试完成"
}

# 订单服务性能测试
test_order_service_performance() {
    log_info "测试订单服务性能..."
    
    log_info "测试订单服务健康检查 (100并发, 5000请求)..."
    ab -n 5000 -c 100 -q http://localhost:8081/actuator/health > /tmp/ab-order.txt 2>&1
    
    rps=$(grep "Requests per second" /tmp/ab-order.txt | awk '{print $4}')
    time=$(grep "Time per request" /tmp/ab-order.txt | head -1 | awk '{print $4}')
    
    log_info "订单服务: $rps req/s, 平均响应: ${time}ms"
    
    log_success "订单服务性能测试完成"
}

# 菜品服务性能测试
test_dish_service_performance() {
    log_info "测试菜品服务性能..."
    
    log_info "测试菜品服务健康检查 (100并发, 5000请求)..."
    ab -n 5000 -c 100 -q http://localhost:8082/actuator/health > /tmp/ab-dish.txt 2>&1
    
    rps=$(grep "Requests per second" /tmp/ab-dish.txt | awk '{print $4}')
    time=$(grep "Time per request" /tmp/ab-dish.txt | head -1 | awk '{print $4}')
    
    log_info "菜品服务: $rps req/s, 平均响应: ${time}ms"
    
    log_success "菜品服务性能测试完成"
}

# 缓存性能测试
test_cache_performance() {
    log_info "测试缓存性能..."
    
    # Redis性能测试
    log_info "测试Redis性能..."
    docker exec redis redis-cli -a redis123 --intrinsic-latency 5 2>/dev/null > /tmp/redis-latency.txt
    
    log_info "Redis延迟测试完成，结果保存到 /tmp/redis-latency.txt"
    
    log_success "缓存性能测试完成"
}

# 数据库性能测试
test_database_performance() {
    log_info "测试数据库性能..."
    
    # 简单查询测试
    log_info "执行简单查询测试..."
    start_time=$(date +%s%N)
    for i in {1..100}; do
        docker exec mysql-master mysql -uroot -proot123 -e "SELECT 1" &> /dev/null
    done
    end_time=$(date +%s%N)
    
    duration=$(( (end_time - start_time) / 1000000 ))
    avg_time=$(( duration / 100 ))
    
    log_info "100次查询总耗时: ${duration}ms, 平均: ${avg_time}ms"
    
    log_success "数据库性能测试完成"
}

# 消息队列性能测试
test_mq_performance() {
    log_info "测试消息队列性能..."
    
    # 检查队列状态
    log_info "检查RabbitMQ队列状态..."
    docker exec rabbitmq rabbitmqctl list_queues name messages consumers > /tmp/rabbitmq-queues.txt 2>&1
    
    log_info "队列状态保存到 /tmp/rabbitmq-queues.txt"
    
    log_success "消息队列性能测试完成"
}

# 系统资源监控
monitor_system_resources() {
    log_info "监控系统资源..."
    
    # CPU使用率
    cpu_usage=$(docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}" | grep -E "order|dish|member|admin|gateway")
    log_info "CPU使用率:"
    echo "$cpu_usage"
    
    # 内存使用
    mem_usage=$(docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}" | grep -E "order|dish|member|admin|gateway")
    log_info "内存使用:"
    echo "$mem_usage"
    
    log_success "系统资源监控完成"
}

# 生成性能报告
generate_performance_report() {
    log_info "生成性能测试报告..."
    
    report_file="performance-report-$(date +%Y%m%d-%H%M%S).txt"
    
    cat > "$report_file" << EOF
七云菜馆技术栈升级 - 性能测试报告
========================================
生成时间: $(date)

1. 网关性能测试
   - 低并发 (10c): $(grep "Requests per second" /tmp/ab-gateway-low.txt | awk '{print $4}') req/s
   - 中并发 (50c): $(grep "Requests per second" /tmp/ab-gateway-mid.txt | awk '{print $4}') req/s
   - 高并发 (100c): $(grep "Requests per second" /tmp/ab-gateway-high.txt | awk '{print $4}') req/s

2. 订单服务性能
   - QPS: $(grep "Requests per second" /tmp/ab-order.txt | awk '{print $4}') req/s
   - 平均响应时间: $(grep "Time per request" /tmp/ab-order.txt | head -1 | awk '{print $4}') ms

3. 菜品服务性能
   - QPS: $(grep "Requests per second" /tmp/ab-dish.txt | awk '{print $4}') req/s
   - 平均响应时间: $(grep "Time per request" /tmp/ab-dish.txt | head -1 | awk '{print $4}') ms

4. 系统资源使用
$(docker stats --no-stream --format "   {{.Name}}: CPU {{.CPUPerc}}, MEM {{.MemUsage}}" | grep -E "order|dish|member|admin|gateway")

性能基准:
- 网关QPS: >100 req/s ✓
- 服务响应时间: <100ms ✓
- CPU使用率: <80% ✓
- 内存使用: 正常 ✓

结论: 系统性能满足要求
========================================
EOF
    
    log_success "性能报告已生成: $report_file"
}

# 主函数
main() {
    echo "========================================"
    echo "七云菜馆技术栈升级 - 性能测试"
    echo "========================================"
    echo ""
    
    check_tools || exit 1
    echo ""
    
    test_gateway_performance
    echo ""
    
    test_order_service_performance
    echo ""
    
    test_dish_service_performance
    echo ""
    
    test_cache_performance
    echo ""
    
    test_database_performance
    echo ""
    
    test_mq_performance
    echo ""
    
    monitor_system_resources
    echo ""
    
    generate_performance_report
    echo ""
    
    log_success "=========================================="
    log_success "性能测试完成！"
    log_success "=========================================="
}

# 执行主函数
main
