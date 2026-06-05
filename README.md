# 计算机考试系统

在线考试与题库管理平台，支持管理员、教师、学生三种角色。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia |
| 后端 | Spring Boot 3.2 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | H2（嵌入式，开箱即用） |

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 18+

### 1. 启动后端

```bash
cd exam-backend

# 编译
mvn clean package -DskipTests

# 启动（注意 Windows 下需指定 JDK 17 和 UTF-8 编码）
java -Dfile.encoding=UTF-8 -jar target/exam-backend-1.0.0.jar
```

后端运行在 `http://localhost:8080`

### 2. 启动前端

```bash
cd exam-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端运行在 `http://localhost:3000`，`/api` 请求自动代理到后端。

### 3. 登录

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 教师 | teacher | teacher123 |
| 学生 | student | student123 |

## 项目结构

```
├── exam-backend/              # Spring Boot 后端
│   └── src/main/java/com/exam/
│       ├── config/            # Security、MyBatis-Plus 配置
│       ├── controller/        # REST 接口
│       ├── entity/            # 实体类
│       ├── mapper/            # MyBatis Mapper
│       ├── security/          # JWT 认证与权限
│       └── service/           # 业务逻辑
├── exam-frontend/             # Vue 3 前端
│   └── src/
│       ├── api/               # API 请求封装
│       ├── router/            # 路由配置
│       ├── stores/            # Pinia 状态管理
│       └── views/             # 页面组件
│           ├── admin/         # 管理员页面
│           ├── teacher/       # 教师页面
│           ├── student/       # 学生页面
│           └── login/         # 登录页
└── data/                      # H2 数据库文件（自动生成）
```

## 功能模块

- **管理员**：用户管理、角色管理、系统配置、操作日志
- **教师**：科目管理、题库管理、考试管理、阅卷管理、成绩统计
- **学生**：在线考试、成绩查询、错题本
