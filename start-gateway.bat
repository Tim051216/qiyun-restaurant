@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
cd restaurant-gateway
echo Starting Gateway on port 9080...
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dmaven.test.skip=true
