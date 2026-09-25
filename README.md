# ArchSentinel

ArchSentinel 是面向代码类课程作业的 AI 评估与架构审查系统。系统围绕代码结构理解、质量检查、架构约束、演化分析和智能诊断，为教师与学生提供可解释的代码分析结果。

## 项目结构

```text
ArchSentinel/
├── backend/                         Spring Boot 后端与分析工具
└── front/
    ├── ArchSentinel-main/           Vue 2 业务系统前端
    └── ArchSentinel1/               Docusaurus 展示与文档站
```

## 主要功能

- **ArchMapper**：解析代码实体、依赖关系与项目结构。
- **ArchInspector**：执行代码质量检查并定位违规代码。
- **ArchCompass**：检查预定义及自定义架构约束。
- **ArchVitals**：分析度量演化、耦合面、实体归属、重构和侵入式修改。
- **ArchDiagnose**：汇总分析结果并生成诊断报告。

## 快速启动

### 1. 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

后端默认运行在 <http://localhost:8888/>，需要 Java 17。

### 2. 启动业务前端

```powershell
cd front\ArchSentinel-main
npm run install:app
npm run serve
```

业务系统默认运行在 <http://localhost:8080/>。

### 3. 启动展示站

```powershell
cd front\ArchSentinel1
npm install
npm run start
```

展示站默认运行在 <http://localhost:3000/>。

## 构建

两个前端均可通过 `npm run build` 生成生产构建。更多说明请参阅各子项目中的 README。
