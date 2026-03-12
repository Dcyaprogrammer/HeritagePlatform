# HeritagePlatform

Group Coursework for CPT202

## 技术栈
- Java 21（要求 JDK 21+）
- Spring Boot 4.0.3
- Maven Wrapper（无需预装 Maven，使用 `./mvnw`/`mvnw.cmd` 即可）
- Thymeleaf（服务端渲染）

## 目录结构
```
HeritagePlatform/
├─ platform/                # 后端（Spring Boot）
│  ├─ src/main/java/...     # 业务代码
│  ├─ src/main/resources/
│  │  ├─ templates/         # Thymeleaf 模板
│  │  └─ static/            # 静态资源（CSS/JS/图片）
│  └─ pom.xml
└─ README.md
```

## 开发环境
- JDK: 21 或以上
- Maven: 可选（推荐直接使用仓库中的 Maven Wrapper）

## 快速开始
在 `platform/` 目录下：

macOS / Linux:
```bash
./mvnw spring-boot:run
```

Windows:
```bash
mvnw.cmd spring-boot:run
```

启动后访问：
```
http://localhost:8080
```

首页由 `HomeController` 映射到 `templates/index.html`。

## 常用命令
- 运行测试
  - macOS/Linux: `./mvnw test`
  - Windows: `mvnw.cmd test`
- 打包
  - macOS/Linux: `./mvnw -DskipTests package`
  - Windows: `mvnw.cmd -DskipTests package`
- 运行可执行包
  - `java -jar target/platform-0.0.1-SNAPSHOT.jar`

## 开发提示
- 页面模板放在 `src/main/resources/templates`，静态资源放在 `src/main/resources/static`。
- 开发环境已关闭 Thymeleaf 模板缓存，修改模板后可直接刷新浏览器查看效果。

## 后续规划（可选）
- 如需引入独立前端框架（React/Vue），可在仓库根目录新增 `frontend/`，采用单仓库（monorepo）管理；当前阶段先用 Thymeleaf 提升迭代效率。
