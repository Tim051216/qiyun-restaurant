#!/bin/bash

# 容器化部署验证脚本
# 用于验证Docker环境和配置的正确性

echo "========================================="
echo "容器化部署验证"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查计数
PASSED=0
FAILED=0

# 检查函数
check() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $1"
        ((PASSED++))
    else
        echo -e "${RED}✗${NC} $1"
        ((FAILED++))
    fi
}

# 1. 检查Docker是否安装
echo "1. 检查Docker环境..."
docker --version > /dev/null 2>&1
check "Docker已安装"

docker-compose --version > /dev/null 2>&1
check "Docker Compose已安装"

# 2. 检查Docker是否运行
docker ps > /dev/null 2>&1
check "Docker守护进程正在运行"

# 3. 检查Dockerfile文件
echo ""
echo "2. 检查Dockerfile文件..."
[ -f "../restaurant-gateway/Dockerfile" ]
check "Gateway Dockerfile存在"

[ -f "../restaurant-order/Dockerfile" ]
check "Order Service Dockerfile存在"

[ -f "../restaurant-dish/Dockerfile" ]
check "Dish Service Dockerfile存在"

[ -f "../restaurant-member/Dockerfile" ]
check "Member Service Dockerfile存在"

[ -f "../restaurant-admin-service/Dockerfile" ]
check "Admin Service Dockerfile存在"

# 4. 检查docker-compose文件
echo ""
echo "3. 检查docker-compose配置..."
[ -f "docker-compose-full.yml" ]
check "docker-compose-full.yml存在"

# 验证docker-compose配置语法
docker-compose -f docker-compose-full.yml config --quiet > /dev/null 2>&1
check "docker-compose配置语法正确"

# 5. 检查必要的配置文件
echo ""
echo "4. 检查配置文件..."
[ -f "monitoring/prometheus.yml" ]
check "Prometheus配置文件存在"

[ -f "DOCKER_DEPLOYMENT.md" ]
check "部署文档存在"

# 6. 检查网络
echo ""
echo "5. 检查Docker网络..."
docker network ls | grep restaurant-network > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo -e "${YELLOW}!${NC} restaurant-network网络已存在（将在docker-compose up时使用）"
else
    echo -e "${GREEN}✓${NC} restaurant-network网络将在启动时创建"
fi

# 7. 检查数据卷
echo ""
echo "6. 检查Docker数据卷..."
VOLUMES=$(docker volume ls -q | grep -E "mysql-master-data|redis-data|rabbitmq-data|nacos-data" | wc -l)
if [ $VOLUMES -gt 0 ]; then
    echo -e "${YELLOW}!${NC} 发现 $VOLUMES 个已存在的数据卷"
    echo "   如需全新部署，请先运行: docker-compose -f docker-compose-full.yml down -v"
else
    echo -e "${GREEN}✓${NC} 数据卷将在启动时创建"
fi

# 8. 检查端口占用
echo ""
echo "7. 检查端口占用..."
PORTS=(3306 6379 5672 8080 8081 8082 8083 8084 8848 9090 3000)
PORT_CONFLICTS=0

for PORT in "${PORTS[@]}"; do
    if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1 || netstat -an | grep ":$PORT " | grep LISTEN >/dev/null 2>&1; then
        echo -e "${YELLOW}!${NC} 端口 $PORT 已被占用"
        ((PORT_CONFLICTS++))
    fi
done

if [ $PORT_CONFLICTS -eq 0 ]; then
    echo -e "${GREEN}✓${NC} 所有必需端口都可用"
    ((PASSED++))
else
    echo -e "${RED}✗${NC} 发现 $PORT_CONFLICTS 个端口冲突"
    ((FAILED++))
fi

# 9. 检查磁盘空间
echo ""
echo "8. 检查系统资源..."
AVAILABLE_SPACE=$(df -h . | awk 'NR==2 {print $4}' | sed 's/G//')
if [ $(echo "$AVAILABLE_SPACE > 20" | bc) -eq 1 ]; then
    echo -e "${GREEN}✓${NC} 磁盘空间充足 (${AVAILABLE_SPACE}GB可用)"
    ((PASSED++))
else
    echo -e "${YELLOW}!${NC} 磁盘空间较少 (${AVAILABLE_SPACE}GB可用)，建议至少20GB"
    ((PASSED++))
fi

# 总结
echo ""
echo "========================================="
echo "验证结果"
echo "========================================="
echo -e "通过: ${GREEN}$PASSED${NC}"
echo -e "失败: ${RED}$FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ 所有检查通过！可以开始部署。${NC}"
    echo ""
    echo "启动命令:"
    echo "  docker-compose -f docker-compose-full.yml up -d"
    echo ""
    echo "查看日志:"
    echo "  docker-compose -f docker-compose-full.yml logs -f"
    echo ""
    exit 0
else
    echo -e "${RED}✗ 发现 $FAILED 个问题，请先解决后再部署。${NC}"
    echo ""
    exit 1
fi
