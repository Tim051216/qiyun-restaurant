#!/bin/bash

# 部署脚本模板
# 用于CI/CD Pipeline中的自动部署

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 参数检查
if [ $# -lt 1 ]; then
    log_error "Usage: $0 <environment> [service]"
    log_info "environment: test|production"
    log_info "service: all|gateway|order|dish|member|admin (default: all)"
    exit 1
fi

ENVIRONMENT=$1
SERVICE=${2:-all}

log_info "开始部署到 ${ENVIRONMENT} 环境"
log_info "部署服务: ${SERVICE}"

# 环境配置
case $ENVIRONMENT in
    test)
        DOCKER_COMPOSE_FILE="docker-compose.test.yml"
        NAMESPACE="test"
        ;;
    production)
        DOCKER_COMPOSE_FILE="docker-compose.prod.yml"
        NAMESPACE="prod"
        ;;
    *)
        log_error "未知环境: $ENVIRONMENT"
        exit 1
        ;;
esac

# 部署函数
deploy_service() {
    local service=$1
    log_info "部署服务: $service"
    
    # 拉取最新镜像
    log_info "拉取Docker镜像..."
    docker pull ${DOCKER_USERNAME}/restaurant-${service}:${IMAGE_TAG}
    
    # 停止旧容器
    log_info "停止旧容器..."
    docker-compose -f ${DOCKER_COMPOSE_FILE} stop ${service} || true
    
    # 启动新容器
    log_info "启动新容器..."
    docker-compose -f ${DOCKER_COMPOSE_FILE} up -d ${service}
    
    # 等待服务就绪
    log_info "等待服务就绪..."
    sleep 10
    
    # 健康检查
    if check_health ${service}; then
        log_info "服务 ${service} 部署成功"
    else
        log_error "服务 ${service} 健康检查失败"
        # 回滚
        log_warn "执行回滚..."
        docker-compose -f ${DOCKER_COMPOSE_FILE} restart ${service}
        exit 1
    fi
}

# 健康检查函数
check_health() {
    local service=$1
    local max_attempts=30
    local attempt=1
    
    case $service in
        gateway)
            PORT=8080
            ;;
        order)
            PORT=8081
            ;;
        dish)
            PORT=8082
            ;;
        member)
            PORT=8083
            ;;
        admin)
            PORT=8084
            ;;
        *)
            log_error "未知服务: $service"
            return 1
            ;;
    esac
    
    while [ $attempt -le $max_attempts ]; do
        log_info "健康检查尝试 $attempt/$max_attempts..."
        
        if curl -f http://localhost:${PORT}/actuator/health > /dev/null 2>&1; then
            log_info "服务健康检查通过"
            return 0
        fi
        
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "服务健康检查超时"
    return 1
}

# 冒烟测试
smoke_test() {
    log_info "执行冒烟测试..."
    
    # 测试网关
    if ! curl -f http://localhost:8080/actuator/health; then
        log_error "网关冒烟测试失败"
        return 1
    fi
    
    # 测试订单服务
    if ! curl -f http://localhost:8081/actuator/health; then
        log_error "订单服务冒烟测试失败"
        return 1
    fi
    
    # 测试菜品服务
    if ! curl -f http://localhost:8082/actuator/health; then
        log_error "菜品服务冒烟测试失败"
        return 1
    fi
    
    log_info "冒烟测试通过"
    return 0
}

# 主部署流程
main() {
    # 设置镜像标签
    if [ "$ENVIRONMENT" == "production" ]; then
        IMAGE_TAG="latest"
    else
        IMAGE_TAG="develop"
    fi
    
    # 部署服务
    if [ "$SERVICE" == "all" ]; then
        log_info "部署所有服务..."
        
        # 按依赖顺序部署
        deploy_service "gateway"
        deploy_service "order"
        deploy_service "dish"
        deploy_service "member"
        deploy_service "admin"
    else
        deploy_service "$SERVICE"
    fi
    
    # 冒烟测试
    if smoke_test; then
        log_info "部署完成！"
    else
        log_error "冒烟测试失败，请检查服务状态"
        exit 1
    fi
    
    # 清理旧镜像
    log_info "清理旧镜像..."
    docker image prune -f
    
    log_info "部署流程完成"
}

# 执行主流程
main

exit 0
