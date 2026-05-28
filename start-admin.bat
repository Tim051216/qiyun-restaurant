@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%
cd restaurant-admin-service
echo Starting Admin Service on port 8084...
mvn spring-boot:run -Dspring-boot.run.profiles=local -Dmaven.test.skip=true
