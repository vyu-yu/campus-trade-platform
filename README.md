# 校园二手交易平台

## 项目概述
面向校园内部学生的二手交易平台，支持二手物品发布、搜索、收藏、留言和交易状态管理，集成 AI 智能功能。

## 技术栈
- **后端**: Spring Boot 3.2 + MyBatis-Plus + Spring Security + JWT + MySQL
- **前端**: Vue 3 + Vite + Element Plus + Axios + Vue Router + Pinia
- **AI**: 大模型 Prompt 模拟（规则引擎 + 关键词匹配）

## 项目结构
```
campus-trade-backend/          # 后端项目
├── pom.xml
├── src/main/java/com/campustrade/
│   ├── CampusTradeApplication.java    # 主启动类
│   ├── common/                        # 通用工具类
│   │   ├── Result.java                # 统一返回结果
│   │   ├── JwtUtil.java               # JWT 工具
│   │   ├── JwtAuthFilter.java         # JWT 认证过滤器
│   │   ├── GlobalExceptionHandler.java # 全局异常处理
│   │   └── MyMetaObjectHandler.java   # MyBatis-Plus 自动填充
│   ├── config/                        # 配置类
│   │   ├── SecurityConfig.java        # Spring Security 配置
│   │   ├── WebMvcConfig.java          # CORS 跨域配置
│   │   └── MyBatisPlusConfig.java     # MyBatis-Plus 分页
│   ├── enums/                         # 枚举类
│   ├── entity/                        # 实体类 (7张表)
│   ├── mapper/                        # MyBatis-Plus Mapper
│   ├── dto/                           # 数据传输对象
│   ├── vo/                            # 视图对象
│   ├── service/                       # 服务接口
│   ├── service/impl/                  # 服务实现
│   └── controller/                    # 控制器 (8个)
└── src/main/resources/
    ├── application.yml                # 配置文件
    └── init.sql                       # 数据库初始化脚本

campus-trade-frontend/         # 前端项目
├── package.json
├── vite.config.js
├── index.html
└── src/
    ├── main.js                        # Vue 入口
    ├── App.vue                        # 根组件
    ├── api/                           # API 接口 (9个模块)
    ├── stores/                        # Pinia 状态管理
    ├── router/                        # 路由配置
    ├── components/                    # 公共组件
    └── views/                         # 页面组件 (9个页面)
```

## 数据库设计 (7张业务表)
| 表名 | 说明 | 主要字段 |
|------|------|----------|
| user | 用户表 | id, username, password, nickname, role(USER/ADMIN), status |
| category | 分类表 | id, name, parent_id, sort |
| product | 商品表 | id, user_id, category_id, title, price, condition, status(SELLING/SOLD/TAKEN_DOWN) |
| favorite | 收藏表 | id, user_id, product_id |
| message | 留言表 | id, user_id, product_id, content, parent_id |
| transaction | 交易表 | id, product_id, seller_id, buyer_id, status(PENDING/PAID/COMPLETED/CANCELLED) |
| report | 举报表 | id, user_id, product_id, reason, status(PENDING/RESOLVED/DISMISSED) |

## API 接口清单

### 用户模块
| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /api/user/register | POST | 注册 | 公开 |
| /api/user/login | POST | 登录，返回 token | 公开 |
| /api/user/profile | GET | 获取个人信息 | 登录 |
| /api/user/profile | PUT | 修改个人信息 | 登录 |

### 商品模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/product/publish | POST | 发布商品 |
| /api/product/{id} | PUT | 修改商品 |
| /api/product/{id} | DELETE | 删除商品 |
| /api/product/{id} | GET | 商品详情 |
| /api/product/list | GET | 商品列表（分页、搜索、筛选） |
| /api/product/my | GET | 我的商品 |
| /api/product/{id}/status | PUT | 修改商品状态 |

### 分类模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/category/list | GET | 获取分类列表 |

### 收藏模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/favorite | POST | 收藏商品 |
| /api/favorite/{productId} | DELETE | 取消收藏 |
| /api/favorite/list | GET | 我的收藏 |

### 留言模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/message | POST | 发送留言 |
| /api/message/{productId} | GET | 商品留言列表 |

### 交易模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/transaction/create | POST | 创建交易 |
| /api/transaction/{id}/status | PUT | 更新交易状态 |
| /api/transaction/list | GET | 我的交易 |
| /api/transaction/{id} | GET | 交易详情 |

### 举报模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/report | POST | 提交举报 |
| /api/report/list | GET | 举报列表（管理员） |
| /api/report/{id}/status | PUT | 处理举报（管理员） |

### AI 模块
| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ai/generate-title | POST | AI 智能生成商品标题 |
| /api/ai/optimize-description | POST | AI 优化商品描述 |
| /api/ai/price-suggestion | POST | AI 价格建议 |
| /api/ai/customer-service | POST | 智能客服问答 |
| /api/ai/risk-check | POST | 风险检测 |

## AI 功能说明

### 1. 智能商品标题生成
- **输入**: 商品描述 + 分类
- **处理**: 提取关键信息，结合分类生成标题
- **输出**: 优化后的商品标题

### 2. 智能描述优化
- **输入**: 原始描述 + 成色信息
- **处理**: 补充交易信息、成色说明
- **输出**: 完善后的商品描述

### 3. AI 价格建议
- **输入**: 分类 + 成色 + 原价
- **处理**: 成色系数计算（全新1.0 ~ 其他0.3）
- **输出**: 建议价格 + 参考区间

### 4. 智能客服
- **输入**: 用户问题
- **处理**: 关键词匹配（交易流程/举报/退款/平台介绍）
- **输出**: 结构化回答

### 5. 风险检测
- **输入**: 商品标题 + 描述 + 价格
- **处理**: 敏感词检测 + 价格异常分析
- **输出**: 风险等级 + 警告列表

## 快速启动

### 1. 数据库初始化
```sql
-- 执行初始化脚本
source campus-trade-backend/src/main/resources/init.sql
```

### 2. 启动后端
```bash
cd campus-trade-backend
mvn spring-boot:run
```

### 3. 启动前端
```bash
cd campus-trade-frontend
npm install
npm run dev
```

### 4. 访问系统
- 前端页面: http://localhost:3000
- 后端接口: http://localhost:8080

### 测试账号
- 管理员: admin / admin123
- 普通用户: 注册即可

## 完整业务流程
1. 用户注册登录 → 2. 浏览商品列表 → 3. 查看商品详情 → 4. 收藏/留言 → 5. 发起购买 → 6. 管理交易状态 → 7. 举报违规商品 → 8. 管理员处理举报
