# ArchSentinel 业务系统前端

本目录包含 ArchSentinel 的交互式分析前端，提供 ArchMapper、ArchInspector、ArchCompass、ArchVitals 和 ArchDiagnose 等功能入口。实际 Vue 2 源码位于 `front` 子目录，当前目录提供了便捷的转发脚本。

## 环境要求

- Node.js 18 或更高版本
- 已启动 ArchSentinel 后端服务（默认端口 `8888`）

## 首次安装

```powershell
npm run install:app
```

## 启动开发服务器

```powershell
npm run serve
```

默认访问地址为 <http://127.0.0.1:8080/>。开发服务器会将 `/api` 请求转发到运行在 8888 端口的 Spring Boot 后端。

## 生产构建

```powershell
npm run build
```

构建结果位于 `front/dist` 目录。
