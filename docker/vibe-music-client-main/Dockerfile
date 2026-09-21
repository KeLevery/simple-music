# ==================== 第一阶段：构建 ====================
# 使用 Node.js 20 的 Alpine 精简版作为基础镜像，命名为 builder
# Alpine 是一个极小的 Linux 发行版，镜像体积只有 ~50MB（对比完整版 ~1GB）
FROM node:20-alpine AS builder

# 设置容器内的工作目录为 /app
# 后续的 COPY、RUN 等命令都会在这个目录下执行
WORKDIR /app

# 启用 corepack（Node.js 自带的包管理器管理工具）
# 然后用 corepack 安装并激活最新版 pnpm
# corepack 类似于 nvm 管理 Node 版本，但它管理的是 pnpm/yarn 等包管理器
RUN corepack enable && corepack prepare pnpm@latest --activate

# 先只复制 package.json 和 pnpm-lock.yaml
# 为什么不直接 COPY . . ？因为这一步会被 Docker 缓存！
# 只要这两个文件没变，下次构建就会跳过 pnpm install，大幅提速
COPY package.json pnpm-lock.yaml ./

# 根据 lockfile 安装依赖
# 不使用 --frozen-lockfile，允许根据 package.json 自动更新依赖
RUN pnpm install

# 复制全部源码到容器中
# 放在 pnpm install 之后，这样源码改动不会导致重新安装依赖
COPY . .

# 执行构建，生成 dist 目录（Vite/Vue 项目的打包输出）
RUN pnpm build

# ==================== 第二阶段：运行 ====================
# 构建阶段产出了 dist 目录，但那个镜像里有 Node.js + 源码 + 依赖，体积很大
# 我们只需要 dist 里的静态文件，所以换一个更小的 Nginx 镜像来运行
# nginx:alpine 只有 ~25MB，而 node:20-alpine 有 ~180MB
FROM nginx:alpine

# 复制自定义的 Nginx 配置文件
# 这个文件控制了 Nginx 如何处理请求（比如 SPA 路由的 try_files）
COPY nginx.conf /etc/nginx/nginx.conf

# 从 builder 阶段复制构建产物到 Nginx 的默认静态文件目录
# --from=builder 指定从哪个阶段复制
# Nginx 默认会从 /usr/share/nginx/html 目录提供静态文件服务
COPY --from=builder /app/dist /usr/share/nginx/html

# 声明容器暴露 80 端口（Nginx 默认端口）
# 这只是文档声明，实际映射还需要在 docker-compose 里指定 ports
EXPOSE 80

# 容器启动时执行的命令
# nginx -g "daemon off;" 的意思是让 Nginx 在前台运行
# Docker 容器需要前台进程保持运行，否则容器会立刻退出
CMD ["nginx", "-g", "daemon off;"]
