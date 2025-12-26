# Tanhua - Social Dating Platform

> A distributed social dating platform backend system based on Spring Boot + Dubbo

## Project Introduction

Tanhua is a mobile dating application backend service similar to Tantan and Tinder. The system adopts a microservices architecture design, supporting core features such as user registration/login, intelligent recommendation matching, social feeds, short videos, instant messaging, and includes a complete admin management system.

## Technical Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Client (App/Web)                           │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   tanhua-server (API Gateway)                    │
│                         Port: 10880                              │
│            REST API + JWT Auth + Token Interceptor               │
└─────────────────────────────────────────────────────────────────┘
                                │
                          Dubbo RPC
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│               tanhua-dubbo-service (Business Service)            │
│                  Service Registry: Zookeeper                     │
└─────────────────────────────────────────────────────────────────┘
          │                     │                     │
          ▼                     ▼                     ▼
    ┌──────────┐          ┌──────────┐          ┌──────────┐
    │  MySQL   │          │ MongoDB  │          │  Redis   │
    │User Data │          │Social    │          │  Cache   │
    │          │          │Content   │          │          │
    └──────────┘          └──────────┘          └──────────┘
```

### Technology Stack

| Category | Technology | Version/Description |
|----------|------------|---------------------|
| **Core Framework** | Spring Boot | 2.1.0.RELEASE |
| **Distributed Services** | Apache Dubbo | 2.7.3 |
| **Service Registry** | Zookeeper | Service Discovery & Registration |
| **ORM Framework** | MyBatis Plus | 3.1.0 |
| **Relational Database** | MySQL | 5.1.47 |
| **Document Database** | MongoDB | 3.9.1 |
| **Cache** | Redis + Spring Cache | Session/Data Caching |
| **Message Queue** | RocketMQ | Async Processing/Logging |
| **File Storage** | Alibaba Cloud OSS + FastDFS | Image/Video Storage |
| **Instant Messaging** | Easemob (Huanxin) | IM Service |
| **SMS Service** | Alibaba Cloud SMS | Verification Codes |
| **Face Recognition** | Baidu AI | Identity Verification |
| **Content Moderation** | Huawei Cloud UGC | Text/Image Review |
| **Authentication** | JWT | Token-based Auth |

## Project Structure

```
tanhua/
├── tanhua-commons/                 # Common Module
│   └── src/main/java/com/tanhua/commons/
│       ├── templates/              # Third-party Service Templates
│       │   ├── OssTemplate.java        # Alibaba Cloud OSS Upload
│       │   ├── SmsTemplate.java        # SMS Sending
│       │   ├── FaceTemplate.java       # Face Recognition
│       │   ├── HuanXinTemplate.java    # Easemob IM
│       │   └── HuaWeiUGCTemplate.java  # Huawei Cloud Content Moderation
│       ├── properties/             # Configuration Properties
│       └── exception/              # Custom Exceptions
│
├── tanhua-domain/                  # Domain Model Module
│   └── src/main/java/com/tanhua/domain/
│       ├── db/                     # MySQL Entities
│       │   ├── User.java               # User Account
│       │   ├── UserInfo.java           # User Details
│       │   ├── Settings.java           # User Settings
│       │   ├── Question.java           # Stranger Questions
│       │   └── BlackList.java          # Blacklist
│       ├── mongo/                  # MongoDB Documents
│       │   ├── Publish.java            # Posts/Moments
│       │   ├── Video.java              # Short Videos
│       │   ├── Comment.java            # Comments
│       │   ├── Friend.java             # Friend Relationships
│       │   ├── Visitor.java            # Visitor Records
│       │   ├── UserLocation.java       # User Location
│       │   ├── RecommendUser.java      # Recommended Users
│       │   ├── UserLike.java           # Like Records
│       │   └── FollowUser.java         # Follow Relationships
│       └── vo/                     # View Objects (VO)
│
├── tanhua-dubbo/                   # Dubbo Service Module
│   ├── tanhua-dubbo-interface/     # Service Interface Definitions
│   │   └── src/main/java/com/tanhua/dubbo/api/
│   │       ├── db/                     # MySQL Service Interfaces
│   │       └── mongo/                  # MongoDB Service Interfaces
│   └── tanhua-dubbo-service/       # Service Implementations
│       └── src/main/java/com/tanhua/dubbo/api/
│           ├── db/                     # MySQL Service Implementations
│           └── mongo/                  # MongoDB Service Implementations
│
├── tanhua-server/                  # API Service Module (Port: 10880)
│   └── src/main/java/com/tanhua/server/
│       ├── controller/             # REST Controllers
│       │   ├── LoginController.java        # Login/Register
│       │   ├── UserInfoController.java     # User Info
│       │   ├── TodayBestController.java    # Today's Best/Recommendations
│       │   ├── MovementsController.java    # Moments/Posts
│       │   ├── VideoController.java        # Short Videos
│       │   ├── CommentsController.java     # Comments
│       │   ├── LocationController.java     # Location Services
│       │   ├── SettingsController.java     # Settings
│       │   ├── MessagesController.java     # Messages
│       │   └── HuanxinUserController.java  # Easemob Users
│       ├── service/                # Business Service Layer
│       └── interceptor/            # Interceptors
│           ├── TokenInterceptor.java       # Token Verification
│           └── UserHolder.java             # User Context
│
├── tanhua-manage/                  # Admin Module (Port: 18083)
│   └── src/main/java/com/tanhua/manage/
│       ├── controller/             # Admin Controllers
│       ├── service/                # Admin Services
│       ├── job/                    # Scheduled Tasks
│       └── listener/               # MQ Message Listeners
│
└── pom.xml                         # Maven Parent POM
```

## Feature Modules

### 1. User Module

| Feature | Description | API |
|---------|-------------|-----|
| Send Verification Code | Alibaba Cloud SMS verification | `POST /user/login` |
| Login/Register | Phone + verification code, auto-register for new users | `POST /user/loginVerification` |
| Complete Profile | Fill in nickname, gender, birthday, avatar, etc. | `POST /user/loginReginfo` |
| Face Verification | Baidu AI face detection | `POST /user/loginReginfo/head` |
| Update Avatar | OSS avatar upload | `POST /user/header` |

### 2. Recommendation & Matching Module

| Feature | Description | API |
|---------|-------------|-----|
| Today's Best | Get user with highest compatibility score | `GET /tanhua/todayBest` |
| Recommendation List | Paginated recommended users | `GET /tanhua/recommendation` |
| User Details | View detailed user profile | `GET /tanhua/{id}/personalInfo` |
| Nearby Search | Search nearby users by location | `GET /tanhua/search` |
| Discover | Swipe left/right matching (like/dislike) | `GET /tanhua/cards` |

### 3. Moments/Posts Module

| Feature | Description | API |
|---------|-------------|-----|
| Publish Moment | Post image/video content | `POST /movements` |
| Friend Moments | View friends' posts | `GET /movements` |
| Recommended Moments | Get popular recommended posts | `GET /movements/recommend` |
| My Moments | View own posts | `GET /movements/all` |
| Single Moment | Get moment details | `GET /movements/{id}` |
| Like Moment | Like/unlike a moment | `GET /movements/{id}/like` |
| Love Moment | Mark moment as loved | `GET /movements/{id}/love` |
| Comment List | Get moment comments | `GET /comments` |
| Post Comment | Comment on a moment | `POST /comments` |

### 4. Short Video Module

| Feature | Description | API |
|---------|-------------|-----|
| Publish Video | Upload short video (FastDFS storage) | `POST /smallVideos` |
| Video List | Paginated video feed | `GET /smallVideos` |
| Follow Author | Follow video creator | `POST /smallVideos/{id}/userFocus` |
| Unfollow | Unfollow user | `POST /smallVideos/{id}/userUnFocus` |

### 5. Messages Module

| Feature | Description | API |
|---------|-------------|-----|
| Stranger Messages | View messages from strangers | `GET /messages/strangerMessages` |
| Reply Message | Reply to stranger messages | `POST /messages/strangerMessages` |
| Contact List | Get friend contact list | `GET /messages/contacts` |
| Add Contact | Add friend | `POST /messages/contacts` |
| Likes List | View received likes | `GET /messages/likes` |
| Comments List | View received comments | `GET /messages/comments` |
| Loves List | View received loves | `GET /messages/loves` |

### 6. Interaction Module

| Feature | Description | API |
|---------|-------------|-----|
| Who Viewed Me | View visitor records | `GET /users/visitors` |
| My Likes | View liked users list | `GET /users/friends/{type}` |
| Fans List | View followers list | `GET /users/fans` |
| Mutual Likes | View mutually liked users | `GET /users/friends/mutual` |

### 7. Settings Module

| Feature | Description | API |
|---------|-------------|-----|
| General Settings | Get/modify notification settings | `GET/POST /users/settings` |
| Stranger Question | Set question for strangers | `POST /users/questions` |
| Blacklist | Get blacklisted users | `GET /users/blacklist` |
| Remove from Blacklist | Remove user from blacklist | `DELETE /users/blacklist/{uid}` |

### 8. Admin Backend

| Feature | Description |
|---------|-------------|
| User Management | User list query, status management |
| Data Statistics | User growth, activity analysis |
| Content Moderation | Manual/automatic content review |
| Log Management | Operation log recording and query |

## Quick Start

### Environment Requirements

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- MongoDB 4.0+
- Redis 5.0+
- Zookeeper 3.5+
- RocketMQ 4.7+ (optional)

### Configuration

1. **Database Configuration** - Modify database connection info in each module's `application.yml`

2. **Zookeeper Configuration**
```yaml
dubbo:
  registry:
    address: zookeeper://your-zookeeper-host:2181
```

3. **Redis Configuration**
```yaml
spring:
  redis:
    host: your-redis-host
    port: 6379
```

4. **Third-party Service Configuration** - Configure in `tanhua-server/application.yml`:
   - Alibaba Cloud OSS (AccessKey, Bucket)
   - Alibaba Cloud SMS (AccessKey, Signature, Template)
   - Baidu Face Recognition (AppId, ApiKey, SecretKey)
   - Easemob IM (AppKey, ClientId, ClientSecret)

### Startup Order

```bash
# 1. Start infrastructure services (ensure these are running)
# - Zookeeper
# - MySQL
# - MongoDB
# - Redis

# 2. Start Dubbo service provider
cd tanhua-dubbo/tanhua-dubbo-service
mvn spring-boot:run

# 3. Start API service
cd tanhua-server
mvn spring-boot:run

# 4. Start admin backend (optional)
cd tanhua-manage
mvn spring-boot:run
```

### Service Ports

| Service | Port | Description |
|---------|------|-------------|
| tanhua-server | 10880 | API Service |
| tanhua-manage | 18083 | Admin Backend |
| tanhua-dubbo-service | 20881 | Dubbo Service |

## Development Log

<details>
<summary>Click to expand development records</summary>

| Phase | Technologies | Features Implemented |
|-------|--------------|---------------------|
| Day 1 | Dubbo, SOA, SpringBoot Auto-config | Login/Register (Send Verification Code) |
| Day 2 | Token, Alibaba OSS, Baidu Face Recognition, JWT | Complete Profile, User CRUD |
| Day 3 | Interceptor, Token Handling | Blacklist Management, Stranger Questions, Settings |
| Day 4 | MongoDB Basics, Indexing, SpringBoot Integration | Today's Best Recommendation |
| Day 5 | MongoDB Advanced, Redis Cache, RocketMQ | Moments Feature (Publish/Query) |
| Day 6 | - | Moment Like/Love, Comments |
| Day 7 | FastDFS, SpringCache + Redis | Short Video Publish/List, Follow Users |
| Day 8 | Easemob IM Integration | User Details, Stranger Messages, Contact Management |
| Day 9 | MongoDB Geospatial Indexing | Visitor Records, My Likes, Nearby Search |
| Day 10 | Verification Code, Admin Setup | Admin System, Data Statistics |
| Day 11 | RocketMQ Installation | SpringBoot + RocketMQ Integration |
| Day 12 | RocketMQ Application | Logging, Scheduled Stats, Huawei Cloud Content Moderation |

</details>

## Project Highlights

- **Microservices Architecture**: Dubbo-based service governance with service discovery, registration, and load balancing
- **Multi-datasource**: MySQL + MongoDB hybrid storage, leveraging respective strengths
- **Distributed Cache**: Redis for session management and data caching, improving performance
- **Async Decoupling**: RocketMQ for async processing of logging, content moderation, etc.
- **Third-party Integration**: Integration with Alibaba Cloud, Baidu, Huawei, Easemob cloud services
- **Secure Authentication**: JWT Token + Interceptor for stateless authentication
- **Geolocation**: MongoDB geospatial indexing for LBS features

## License

MIT License
