# ==================== 第一阶段：构建 ====================
# 使用 Maven 3.9.9 + JDK 21 的镜像作为构建环境，命名为 builder
# eclipse-temurin 是 Adoptium 项目维护的 OpenJDK 发行版，是目前最主流的 JDK 镜像
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# 设置容器内的工作目录为 /app
WORKDIR /app

# 先只复制 pom.xml（Maven 的项目配置文件，类似前端的 package.json）
# 然后执行 mvn dependency:go-offline 下载所有依赖到本地缓存
# -B 表示 Batch 模式（非交互式，不在日志里输出进度条等装饰字符）
# 这一步和前端先复制 package.json 是同样的思路：利用 Docker 缓存加速构建
# 只要 pom.xml 没变，依赖就不会重新下载（Maven 依赖可能有好几百MB）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制 Java 源码到容器中
# 放在依赖下载之后，这样源码改动不会导致重新下载依赖
COPY src ./src

# 执行 Maven 打包
# clean：先清理之前的构建产物
# package：编译 + 打包，生成 target/*.jar 文件
# -DskipTests：跳过测试（在 Docker 构建中跑测试会非常慢，一般交给 CI 来做）
RUN mvn clean package -DskipTests

# ==================== 第二阶段：运行 ====================
# 构建阶段有 Maven + JDK + 源码 + 依赖，体积可能超过 800MB
# 但运行 jar 包只需要 JRE（Java 运行时），不需要 Maven 和 JDK 编译工具
# eclipse-temurin:21-jre-alpine 只有 ~170MB，比完整 JDK 镜像小很多
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 从 builder 阶段复制打包好的 jar 文件
# /app/target/*.jar 是 Maven 的标准输出路径
# 复制后重命名为 app.jar，方便后续引用
COPY --from=builder /app/target/*.jar app.jar

# 声明容器暴露 8080 端口（Spring Boot 的默认端口）
# 这只是文档声明，实际映射还需要在 docker-compose 里指定 ports
EXPOSE 8080

# 容器启动时执行的命令
# 使用 ENTRYPOINT 而不是 CMD，表示这是容器的主命令，不可被 docker run 覆盖
# java -jar app.jar 启动 Spring Boot 应用
# 配合 docker-compose 中的 environment，Spring Boot 会自动读取环境变量
ENTRYPOINT ["java", "-jar", "app.jar"]
