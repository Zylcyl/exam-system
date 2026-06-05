# 计算机考试系统

<p align="center">
  <strong>在线考试与题库管理平台</strong>
</p>

<p align="center">
  支持<strong>管理员</strong>、<strong>教师</strong>、<strong>学生</strong>三种角色，覆盖<strong>题库管理 → 考试创建 → 在线答题 → 自动评分 → 成绩统计</strong>全流程。
</p>

---
如果喜欢，请给一个star~  Thanks~
---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **前端框架** | Vue 3 + TypeScript | ^3.4 |
| **构建工具** | Vite | ^5.2 |
| **UI 组件库** | Element Plus | ^2.7 |
| **状态管理** | Pinia | ^2.1 |
| **路由** | Vue Router | ^4.3 |
| **HTTP 客户端** | Axios | ^1.7 |
| **后端框架** | Spring Boot | 3.2.5 |
| **安全框架** | Spring Security + JWT | JJWT 0.12.5 |
| **ORM** | MyBatis-Plus | 3.5.6 |
| **数据库** | H2 (内置) / MySQL (生产) | — |
| **工具库** | Hutool | 5.8.27 |
| **Java** | JDK 17 | — |

---

## 快速启动

### 环境要求

| 工具 | 最低版本 |
|------|----------|
| JDK | 17+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | 9+ |

### 1. 克隆项目

```bash
git clone https://github.com/Zylcyl/exam-system.git
cd exam-system
```

### 2. 启动后端

```bash
cd exam-backend

# 编译（首次运行）
mvn clean package -DskipTests

# 启动后端（Windows 需指定 JDK 路径和 UTF-8 编码）
java -Dfile.encoding=UTF-8 -jar target/exam-backend-1.0.0.jar
```

> 首次启动会自动创建 H2 数据库并初始化表结构和演示数据。<br>
> 后端启动在 `http://localhost:8080`，数据库控制台在 `http://localhost:8080/h2-console`。

### 3. 启动前端

```bash
cd exam-frontend

# 安装依赖（首次）
npm install

# 启动开发服务器
npm run dev
```

> 前端启动在 `http://localhost:3000`，Vite 自动将 `/api` 请求代理到后端 `http://localhost:8080`。

### 4. 登录系统

打开浏览器访问 `http://localhost:3000`，使用以下演示账号登录：

| 角色 | 用户名 | 密码 | 权限范围 |
|------|--------|------|----------|
| **管理员** | `admin` | `admin123` | 用户管理、角色管理、系统配置、操作日志、题库/科目/考试/阅卷/统计 |
| **教师** | `teacher` | `teacher123` | 题库管理、科目管理、考试管理、阅卷管理、成绩统计 |
| **学生** | `student` | `student123` | 在线考试、成绩查询、错题本 |

---

## 功能模块

### 管理员端

| 模块 | 说明 |
|------|------|
| **用户管理** | 创建/编辑/禁用系统用户，分配角色（管理员/教师/学生） |
| **角色管理** | 管理角色及其菜单权限的分配 |
| **系统配置** | 配置系统参数（如考试切屏次数限制） |
| **操作日志** | 查看所有用户的操作记录（AOP 自动记录 @OperateLog 注解的方法） |

### 教师端

| 模块 | 说明 |
|------|------|
| **科目管理** | 创建/管理考试科目（如 Java程序设计、数据结构等），支持树形父级 |
| **题库管理** | 支持 7 种题型：单选题、多选题、判断题、填空题、简答题、文件上传题、编程题 |
| **考试管理** | 创建考试：选择科目、设定时间/时长/总分/及格分、随机排列题目、指定考生 |
| **阅卷管理** | 对主观题（简答/编程/文件上传）进行人工评分 |
| **成绩统计** | 按考试查看学生成绩分布、通过率、排名等统计数据 |

### 学生端

| 模块 | 说明 |
|------|------|
| **考试列表** | 查看已分配给我的所有考试，显示状态（未开始/进行中/已结束） |
| **在线答题** | 进入考试后计时答题，支持切屏检测（超过限制自动交卷） |
| **我的成绩** | 查看各科考试成绩（总分/客观题分/主观题分/是否通过）、历史记录 |
| **错题本** | 收录答错的题目，方便复习巩固 |

---

## 项目结构

```
exam-system/
├── exam-backend/                          # Spring Boot 后端
│   ├── pom.xml                            # Maven 依赖配置
│   └── src/main/
│       ├── java/com/exam/
│       │   ├── ExamApplication.java       # 主启动类
│       │   ├── annotation/                # 自定义注解（@OperateLog）
│       │   ├── aspect/                    # AOP 切面（操作日志自动记录）
│       │   ├── common/                    # 公共类（BaseEntity / Result / 全局异常处理）
│       │   ├── config/                    # Security 配置 / MyBatis-Plus 配置
│       │   ├── controller/                # REST 控制器
│       │   │   ├── AuthController         # 认证（登录/当前用户/菜单）
│       │   │   ├── ExamController         # 考试（CRUD + 答题流程）
│       │   │   ├── FileController         # 文件上传下载
│       │   │   ├── LogController          # 操作日志查询
│       │   │   ├── QuestionController     # 题库管理
│       │   │   ├── ScoreController        # 成绩查询统计
│       │   │   ├── SubjectController      # 科目管理
│       │   │   └── UserManageController   # 用户管理
│       │   ├── dto/                       # 数据传输对象
│       │   ├── entity/                    # 数据库实体
│       │   ├── mapper/                    # MyBatis Mapper 接口
│       │   ├── security/                  # JWT 过滤器 / Token 工具 / 用户加载
│       │   ├── service/                   # 业务接口 + 实现
│       │   └── util/                      # 工具类（密码生成器）
│       └── resources/
│           ├── application.yml            # 主配置（H2 / JWT / MyBatis-Plus）
│           ├── application-mysql.yml      # MySQL 环境配置（可选）
│           └── db/
│               ├── schema.sql             # 15 张表 DDL
│               └── data.sql               # 初始数据（3角色 + 3用户 + 菜单权限）
│
├── exam-frontend/                         # Vue 3 前端
│   ├── package.json
│   ├── vite.config.ts                     # Vite 配置（端口 / 代理 / 路径别名）
│   ├── index.html
│   └── src/
│       ├── main.ts                        # 入口（挂载 App + Pinia + Router + Element Plus）
│       ├── App.vue                        # 根组件（<router-view/>）
│       ├── api/                           # API 请求封装（auth / exam / question / subject / user / log）
│       ├── router/index.ts                # 路由配置 + beforeEach 守卫（token 校验 + 角色权限）
│       ├── stores/user.ts                 # Pinia 用户状态（token / userInfo / login / logout）
│       ├── utils/request.ts               # Axios 实例（拦截器：注入 token、401 处理）
│       ├── layout/MainLayout.vue          # 主布局（侧边栏 + 顶栏 + 面包屑 + 内容区）
│       ├── styles/global.css              # 全局样式
│       └── views/
│           ├── login/index.vue            # 登录页
│           ├── dashboard/index.vue        # 仪表盘（统计卡片 + 快捷操作）
│           ├── admin/                     # 管理员页面（用户管理/角色管理/系统配置/操作日志）
│           ├── teacher/                   # 教师页面（题库/科目/考试/阅卷/统计）
│           └── student/                   # 学生页面（考试列表/答题/成绩/错题本）
│
├── .gitignore
└── README.md
```

---

## 数据库设计

系统共 **15 张表**，通过 H2 嵌入式数据库自动初始化：

| 表 | 说明 |
|----|------|
| `sys_user` | 用户表（用户名/密码/姓名/角色/状态，逻辑删除） |
| `sys_role` | 角色表（管理员/教师/学生） |
| `sys_menu` | 菜单权限表（树形结构，directory/menu/button 三种类型） |
| `sys_role_menu` | 角色-菜单关联表 |
| `sys_config` | 系统配置表 |
| `sys_log` | 操作日志表 |
| `subject` | 考试科目表 |
| `question` | 题目表（7 种题型：单选/多选/判断/填空/简答/文件上传/编程） |
| `question_option` | 题目选项表（选择题的 ABCD 选项） |
| `exam` | 考试表（名称/科目/时间/时长/总分/及格分/随机排序/重试/切屏限制） |
| `exam_question` | 考试-题目关联表（含题目顺序和分值） |
| `exam_student` | 考生关联表（状态/开始时间/提交时间/得分/切屏次数） |
| `student_answer` | 答题记录表（答题内容/是否正确/得分/评语） |
| `exam_score` | 成绩表（总分/客观题分/主观题分/是否通过/排名） |
| `file_attachment` | 文件附件表（上传题用） |

---

## API 接口概览

### 认证

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录 | 无 |
| GET | `/api/auth/me` | 获取当前用户信息和菜单 | 登录 |

### 考试（教师/管理员）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/exam/page` | 分页查询考试列表 |
| GET | `/api/exam/{id}` | 获取考试详情 |
| POST | `/api/exam` | 创建考试 |
| PUT | `/api/exam/{id}` | 更新考试 |
| DELETE | `/api/exam/{id}` | 删除考试 |

### 考试（学生）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/exam/my-exams` | 我的考试列表 |
| GET | `/api/exam/{id}/start` | 开始考试（随机返回题目） |
| POST | `/api/exam/{id}/answer` | 保存答案 |
| POST | `/api/exam/{id}/submit` | 提交考试 |
| POST | `/api/exam/{id}/cheat` | 切屏检测 |

### 题目

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/question/page` | 分页查询题目 |
| POST | `/api/question` | 创建题目 |
| PUT | `/api/question/{id}` | 更新题目 |
| DELETE | `/api/question/{id}` | 删除题目 |
| POST | `/api/question/import` | Excel 批量导入 |

---

## 安全设计

- **JWT 无状态认证**：登录签发 token，24 小时有效期，HMAC-SHA384 签名
- **请求拦截器**：前端 axios 自动在请求头注入 `Authorization: Bearer <token>`
- **路由守卫**：前端 `router.beforeEach` 校验 token 存在性和角色权限
- **Spring Security**：`/api/auth/**` 开放登录，其他接口按角色保护
- **CORS 配置**：后端允许所有来源的跨域请求（开发环境）
- **密码加密**：BCrypt 哈希存储
- **切屏检测**：学生考试期间检测浏览器失焦次数，超过上限自动交卷

---

## 切换 MySQL

生产环境建议使用 MySQL。修改 `application.yml` 中 Spring profile 为 `mysql`，并编辑 `application-mysql.yml` 中的数据库连接信息即可。

---

## 后端启动注意事项

1. **Windows 下必须指定 JDK 17 完整路径**，否则默认使用 JDK 8 会导致 `UnsupportedClassVersionError`
2. **必须加 `-Dfile.encoding=UTF-8`**，否则中文会乱码
3. **首次启动自动建库建表**，H2 数据库文件存储在项目根目录 `data/examdb.mv.db`
4. 如需重置数据库，删除 `data/` 目录后重启即可
