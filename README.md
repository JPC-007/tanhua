# 探花交友（Tanhua）

> 基于 Spring Boot + Dubbo 的分布式社交交友平台后端系统

## 项目简介

探花交友是一款类似于探探、Tinder 的移动端交友应用后端服务。系统采用微服务架构设计，支持用户注册登录、智能推荐匹配、社交动态、短视频、即时通讯等核心功能，具备完整的后台管理系统。

## 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        客户端 (App/Web)                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    tanhua-server (API 网关)                      │
│                         端口: 10880                              │
│            REST API + JWT 认证 + Token 拦截器                     │
└─────────────────────────────────────────────────────────────────┘
                                │
                          Dubbo RPC
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                tanhua-dubbo-service (业务服务)                   │
│                  服务注册: Zookeeper                             │
└─────────────────────────────────────────────────────────────────┘
          │                     │                     │
          ▼                     ▼                     ▼
    ┌──────────┐          ┌──────────┐          ┌──────────┐
    │  MySQL   │          │ MongoDB  │          │  Redis   │
    │ 用户数据  │          │ 社交内容  │          │   缓存   │
    └──────────┘          └──────────┘          └──────────┘
```

### 技术栈

| 分类 | 技术 | 版本/说明 |
|------|------|----------|
| **核心框架** | Spring Boot | 2.1.0.RELEASE |
| **分布式服务** | Apache Dubbo | 2.7.3 |
| **服务注册** | Zookeeper | 服务发现与注册 |
| **ORM 框架** | MyBatis Plus | 3.1.0 |
| **关系数据库** | MySQL | 5.1.47 |
| **文档数据库** | MongoDB | 3.9.1 |
| **缓存** | Redis + Spring Cache | 会话/数据缓存 |
| **消息队列** | RocketMQ | 异步处理/日志记录 |
| **文件存储** | 阿里云 OSS + FastDFS | 图片/视频存储 |
| **即时通讯** | 环信（Easemob） | IM 消息服务 |
| **短信服务** | 阿里云 SMS | 验证码发送 |
| **人脸识别** | 百度 AI | 身份认证 |
| **内容审核** | 华为云 UGC | 文本/图片审核 |
| **认证授权** | JWT | Token 认证 |

## 项目结构

```
tanhua/
├── tanhua-commons/                 # 公共模块
│   └── src/main/java/com/tanhua/commons/
│       ├── templates/              # 第三方服务模板类
│       │   ├── OssTemplate.java        # 阿里云 OSS 文件上传
│       │   ├── SmsTemplate.java        # 短信发送
│       │   ├── FaceTemplate.java       # 人脸识别
│       │   ├── HuanXinTemplate.java    # 环信即时通讯
│       │   └── HuaWeiUGCTemplate.java  # 华为云内容审核
│       ├── properties/             # 配置属性类
│       └── exception/              # 自定义异常
│
├── tanhua-domain/                  # 领域模型模块
│   └── src/main/java/com/tanhua/domain/
│       ├── db/                     # MySQL 实体
│       │   ├── User.java               # 用户账户
│       │   ├── UserInfo.java           # 用户详情
│       │   ├── Settings.java           # 用户设置
│       │   ├── Question.java           # 陌生人问题
│       │   └── BlackList.java          # 黑名单
│       ├── mongo/                  # MongoDB 文档
│       │   ├── Publish.java            # 动态/圈子
│       │   ├── Video.java              # 短视频
│       │   ├── Comment.java            # 评论
│       │   ├── Friend.java             # 好友关系
│       │   ├── Visitor.java            # 访客记录
│       │   ├── UserLocation.java       # 用户位置
│       │   ├── RecommendUser.java      # 推荐用户
│       │   ├── UserLike.java           # 喜欢记录
│       │   └── FollowUser.java         # 关注关系
│       └── vo/                     # 视图对象 (VO)
│
├── tanhua-dubbo/                   # Dubbo 服务模块
│   ├── tanhua-dubbo-interface/     # 服务接口定义
│   │   └── src/main/java/com/tanhua/dubbo/api/
│   │       ├── db/                     # MySQL 服务接口
│   │       └── mongo/                  # MongoDB 服务接口
│   └── tanhua-dubbo-service/       # 服务接口实现
│       └── src/main/java/com/tanhua/dubbo/api/
│           ├── db/                     # MySQL 服务实现
│           └── mongo/                  # MongoDB 服务实现
│
├── tanhua-server/                  # API 服务模块 (端口: 10880)
│   └── src/main/java/com/tanhua/server/
│       ├── controller/             # REST 控制器
│       │   ├── LoginController.java        # 登录注册
│       │   ├── UserInfoController.java     # 用户信息
│       │   ├── TodayBestController.java    # 今日佳人/推荐
│       │   ├── MovementsController.java    # 动态/圈子
│       │   ├── VideoController.java        # 小视频
│       │   ├── CommentsController.java     # 评论
│       │   ├── LocationController.java     # 位置服务
│       │   ├── SettingsController.java     # 设置
│       │   ├── MessagesController.java     # 消息
│       │   └── HuanxinUserController.java  # 环信用户
│       ├── service/                # 业务服务层
│       └── interceptor/            # 拦截器
│           ├── TokenInterceptor.java       # Token 验证
│           └── UserHolder.java             # 用户上下文
│
├── tanhua-manage/                  # 管理后台模块 (端口: 18083)
│   └── src/main/java/com/tanhua/manage/
│       ├── controller/             # 管理端控制器
│       ├── service/                # 管理端服务
│       ├── job/                    # 定时任务
│       └── listener/               # MQ 消息监听
│
└── pom.xml                         # Maven 父工程配置
```

## 功能模块

### 1. 用户模块

| 功能 | 描述 | API |
|------|------|-----|
| 发送验证码 | 阿里云短信验证码 | `POST /user/login` |
| 登录/注册 | 手机号 + 验证码登录，新用户自动注册 | `POST /user/loginVerification` |
| 完善资料 | 填写昵称、性别、生日、头像等 | `POST /user/loginReginfo` |
| 人脸认证 | 百度 AI 人脸检测验证 | `POST /user/loginReginfo/head` |
| 更新头像 | OSS 上传头像 | `POST /user/header` |

### 2. 推荐匹配模块

| 功能 | 描述 | API |
|------|------|-----|
| 今日佳人 | 获取缘分值最高的推荐用户 | `GET /tanhua/todayBest` |
| 推荐列表 | 分页获取推荐用户列表 | `GET /tanhua/recommendation` |
| 用户详情 | 查看用户详细资料 | `GET /tanhua/{id}/personalInfo` |
| 搜附近 | 基于地理位置搜索附近用户 | `GET /tanhua/search` |
| 探花 | 左右滑动匹配（喜欢/不喜欢） | `GET /tanhua/cards` |

### 3. 动态圈子模块

| 功能 | 描述 | API |
|------|------|-----|
| 发布动态 | 发布图文/视频动态 | `POST /movements` |
| 好友动态 | 查看好友发布的动态 | `GET /movements` |
| 推荐动态 | 获取推荐的热门动态 | `GET /movements/recommend` |
| 我的动态 | 查看自己发布的动态 | `GET /movements/all` |
| 单条动态 | 获取动态详情 | `GET /movements/{id}` |
| 点赞动态 | 对动态点赞/取消点赞 | `GET /movements/{id}/like` |
| 喜欢动态 | 对动态标记喜欢 | `GET /movements/{id}/love` |
| 评论列表 | 获取动态评论 | `GET /comments` |
| 发表评论 | 对动态发表评论 | `POST /comments` |

### 4. 小视频模块

| 功能 | 描述 | API |
|------|------|-----|
| 发布视频 | 上传小视频（FastDFS 存储） | `POST /smallVideos` |
| 视频列表 | 分页获取视频 Feed | `GET /smallVideos` |
| 关注作者 | 关注视频发布者 | `POST /smallVideos/{id}/userFocus` |
| 取消关注 | 取消关注用户 | `POST /smallVideos/{id}/userUnFocus` |

### 5. 消息模块

| 功能 | 描述 | API |
|------|------|-----|
| 陌生人消息 | 查看陌生人发来的消息 | `GET /messages/strangerMessages` |
| 回复消息 | 回复陌生人消息 | `POST /messages/strangerMessages` |
| 联系人列表 | 获取好友联系人列表 | `GET /messages/contacts` |
| 添加联系人 | 添加好友 | `POST /messages/contacts` |
| 点赞列表 | 查看收到的点赞 | `GET /messages/likes` |
| 评论列表 | 查看收到的评论 | `GET /messages/comments` |
| 喜欢列表 | 查看收到的喜欢 | `GET /messages/loves` |

### 6. 互动模块

| 功能 | 描述 | API |
|------|------|-----|
| 谁看过我 | 查看访客记录 | `GET /users/visitors` |
| 我的喜欢 | 查看喜欢的用户列表 | `GET /users/friends/{type}` |
| 粉丝列表 | 查看粉丝列表 | `GET /users/fans` |
| 相互喜欢 | 查看互相喜欢的用户 | `GET /users/friends/mutual` |

### 7. 设置模块

| 功能 | 描述 | API |
|------|------|-----|
| 通用设置 | 获取/修改通知设置 | `GET/POST /users/settings` |
| 陌生人问题 | 设置陌生人提问问题 | `POST /users/questions` |
| 黑名单列表 | 获取黑名单用户 | `GET /users/blacklist` |
| 移除黑名单 | 将用户移出黑名单 | `DELETE /users/blacklist/{uid}` |

### 8. 管理后台

| 功能 | 描述 |
|------|------|
| 用户管理 | 用户列表查询、状态管理 |
| 数据统计 | 用户增长、活跃度统计分析 |
| 内容审核 | 动态内容人工/自动审核 |
| 日志管理 | 操作日志记录与查询 |

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- MongoDB 4.0+
- Redis 5.0+
- Zookeeper 3.5+
- RocketMQ 4.7+ (可选)

### 配置修改

1. **数据库配置** - 修改各模块 `application.yml` 中的数据库连接信息

2. **Zookeeper 配置**
```yaml
dubbo:
  registry:
    address: zookeeper://your-zookeeper-host:2181
```

3. **Redis 配置**
```yaml
spring:
  redis:
    host: your-redis-host
    port: 6379
```

4. **第三方服务配置** - 在 `tanhua-server/application.yml` 中配置：
   - 阿里云 OSS（AccessKey、Bucket）
   - 阿里云短信（AccessKey、签名、模板）
   - 百度人脸识别（AppId、ApiKey、SecretKey）
   - 环信 IM（AppKey、ClientId、ClientSecret）

### 启动顺序

```bash
# 1. 启动基础服务（确保已启动）
# - Zookeeper
# - MySQL
# - MongoDB
# - Redis

# 2. 启动 Dubbo 服务提供者
cd tanhua-dubbo/tanhua-dubbo-service
mvn spring-boot:run

# 3. 启动 API 服务
cd tanhua-server
mvn spring-boot:run

# 4. 启动管理后台（可选）
cd tanhua-manage
mvn spring-boot:run
```

### 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| tanhua-server | 10880 | API 服务 |
| tanhua-manage | 18083 | 管理后台 |
| tanhua-dubbo-service | 20881 | Dubbo 服务 |

## 开发日志

<details>
<summary>点击展开开发记录</summary>

| 阶段 | 技术点 | 实现功能 |
|------|--------|----------|
| Day 1 | Dubbo, SOA, SpringBoot 自动装配 | 注册登录（发送验证码） |
| Day 2 | Token, 阿里云 OSS, 百度人脸识别, JWT | 完善个人信息，用户信息增删改查 |
| Day 3 | 拦截器, Token 统一处理 | 黑名单管理，陌生人问题，通用设置 |
| Day 4 | MongoDB 基础, 索引, SpringBoot 整合 | 今日佳人推荐功能 |
| Day 5 | MongoDB 进阶, Redis 缓存, RocketMQ | 圈子功能（发布/查询动态） |
| Day 6 | - | 动态点赞/喜欢，评论功能 |
| Day 7 | FastDFS, SpringCache + Redis | 小视频发布与列表，关注用户 |
| Day 8 | 环信 IM 集成 | 用户详情，陌生人消息，联系人管理 |
| Day 9 | MongoDB 地理位置索引 | 访客记录，我的喜欢，搜附近的人 |
| Day 10 | 验证码, 管理后台搭建 | 后台系统环境，数据统计 |
| Day 11 | RocketMQ 安装配置 | SpringBoot 整合 RocketMQ |
| Day 12 | RocketMQ 应用实践 | 日志记录，定时统计，华为云内容审核 |

</details>

## 项目亮点

- **微服务架构**：基于 Dubbo 实现服务治理，支持服务注册发现、负载均衡
- **多数据源**：MySQL + MongoDB 混合存储，充分发挥各自优势
- **分布式缓存**：Redis 实现会话管理、数据缓存，提升系统性能
- **异步解耦**：RocketMQ 实现日志记录、内容审核等异步处理
- **第三方集成**：集成阿里云、百度、华为、环信等多个云服务
- **安全认证**：JWT Token + 拦截器实现无状态认证
- **地理位置**：MongoDB 地理索引支持 LBS 功能

## License

MIT License
