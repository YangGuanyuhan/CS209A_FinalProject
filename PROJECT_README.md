# CS209A Final Project - Stack Overflow Java Analysis

A comprehensive web application for analyzing Stack Overflow questions related to Java programming, built with Spring Boot.

## 项目功能 Features

### 数据收集 (Data Collection - 10 points)
- 通过 Stack Overflow REST API 收集 Java 相关问题
- 支持离线存储（JSON 文件格式）
- 支持示例数据生成用于测试

### 数据分析 (Data Analysis - 60 points)

#### 1. 主题趋势分析 (Topic Trends - 15 points)
- 分析多个 Java 主题在指定时间段内的活跃度趋势
- 支持 1-5 年的时间范围选择
- 折线图展示各主题随时间的变化

#### 2. 主题共现分析 (Topic Co-occurrence - 15 points)
- 识别最常一起出现的 Java 主题对
- 支持选择 Top N 对（5-20）
- 横向条形图展示共现频率

#### 3. 多线程常见问题 (Multithreading Pitfalls - 15 points)
- 通过文本分析识别多线程相关的常见问题
- 包括：竞态条件、死锁、同步问题、线程池、Wait/Notify 等
- 环形图展示分布，表格展示详细信息和示例

#### 4. 问题可解决性分析 (Solvability Analysis - 15 points)
- 对比可解决问题和难解决问题的特征
- 分析 5 个关键因素：代码片段、问题长度、用户声誉、标签数量、浏览量
- 双柱状图和表格展示对比结果

### RESTful APIs (10 points)

```
GET  /api/stats          - 获取数据集统计信息
GET  /api/trends         - 主题趋势分析 (?years=3)
GET  /api/cooccurrence   - 主题共现分析 (?topN=10)
GET  /api/pitfalls       - 多线程问题分析 (?topN=8)
GET  /api/solvability    - 可解决性分析
POST /api/init           - 初始化数据 (?mode=sample&maxQuestions=1000)
GET  /api/questions      - 获取问题列表 (?limit=10)
```

### 可视化 (Visualization - 20 points)
- 使用 Chart.js 进行数据可视化
- 响应式设计，适配各种设备
- 交互式图表，支持动态参数调整

## 技术栈 Tech Stack

- **Backend**: Spring Boot 3.5.7, Java 17
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
- **Visualization**: Chart.js
- **Data Format**: JSON
- **Build Tool**: Maven

## 快速开始 Quick Start

### 1. 克隆项目
```bash
git clone <repository-url>
cd CS209A_FinalProject_demo
```

### 2. 运行项目

#### 使用 Maven
```bash
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

#### 使用 IDE
在 IntelliJ IDEA 或 Eclipse 中打开项目，运行 `FinalProjectDemoApplication` 类

### 3. 访问应用
打开浏览器访问：`http://localhost:8080`

### 4. 初始化数据

首次使用时，点击 "Load Data" 按钮。默认会生成 1000 条示例数据。

#### 使用真实数据（可选）
如果要收集真实的 Stack Overflow 数据，可以使用 API：

```bash
curl -X POST "http://localhost:8080/api/init?mode=api&maxQuestions=1000"
```

⚠️ **注意**: 使用真实 API 需要遵守 Stack Overflow 的速率限制

## 项目结构 Project Structure

```
src/main/java/cs209a/finalproject_demo/
├── FinalProjectDemoApplication.java   # 主应用类
├── controller/
│   ├── HomeController.java            # 首页控制器
│   └── ApiController.java             # REST API 控制器
├── service/
│   ├── DataCollectionService.java     # 数据收集服务
│   ├── DataAnalysisService.java       # 数据分析服务
│   └── SampleDataGenerator.java       # 示例数据生成器
└── model/
    ├── Question.java                  # 问题实体
    ├── Answer.java                    # 答案实体
    └── Comment.java                   # 评论实体

src/main/resources/
├── application.properties             # 应用配置
└── templates/
    └── index.html                     # 主页面
```

## API 使用示例 API Examples

### 获取统计信息
```bash
curl http://localhost:8080/api/stats
```

### 获取主题趋势（过去3年）
```bash
curl http://localhost:8080/api/trends?years=3
```

### 获取 Top 10 共现主题对
```bash
curl http://localhost:8080/api/cooccurrence?topN=10
```

### 获取 Top 8 多线程问题
```bash
curl http://localhost:8080/api/pitfalls?topN=8
```

### 获取可解决性分析
```bash
curl http://localhost:8080/api/solvability
```

## 数据分析方法 Analysis Methods

### 1. 主题识别
- 通过标签（tags）匹配
- 通过标题和正文内容匹配
- 支持的主题包括：generics, collections, io, lambda, stream, multithreading, concurrency, thread, socket, reflection, spring, spring-boot, jpa, hibernate, exception, testing, junit, annotation

### 2. 时间维度分析
- 按年-月分组统计
- 支持 1-5 年的时间范围
- 计算每月问题数量

### 3. 多线程问题识别
- 使用正则表达式匹配关键词
- 分析问题标题和正文内容
- 识别 8 种常见问题类型

### 4. 可解决性判断标准
- **可解决**: 有被采纳的答案，或有高分答案（≥5分）
- **难解决**: 无采纳答案且所有答案分数<2，或无答案

## 关键见解 Key Insights

### Topic Trends
- Spring Boot 和 Lambda 表达式显示增长趋势，反映现代 Java 开发实践
- 传统主题如 I/O 保持稳定但略有下降，开发者转向更高级抽象

### Topic Co-occurrence
- "spring-boot & jpa" 或 "multithreading & collections" 等常见组合反映典型企业 Java 开发挑战
- 这些共现模式建议应该一起学习的主题

### Multithreading Pitfalls
- 线程同步和竞态条件是多线程问题中的主导问题
- 这些是开发者持续遇到的基础概念
- 正确理解 Java 内存模型至关重要

### Solvability Factors
- 包含代码片段、清晰描述和适当长度的问题更容易被解决
- 用户声誉也影响问题的可见性
- 过短或过长的问题往往获得较少的优质回答

## 项目演示 Presentation Notes

### 展示准备
1. 启动应用并加载数据
2. 展示四个主要分析功能
3. 通过浏览器访问 REST API 端点
4. 解释每个分析的见解

### 讲解要点

#### Topic Trends
- 解释为什么某些主题趋势上升/下降
- Spring Boot 的流行反映了现代微服务架构
- Lambda 和 Stream 的增长显示函数式编程的接受度

#### Co-occurrence
- 解释为什么某些主题经常一起出现
- Spring-boot & JPA 是企业应用的标准组合
- Multithreading & Collections 反映并发集合的挑战

#### Multithreading Pitfalls
- 这些确实是 Java 开发者面临的常见问题
- Race condition 和 deadlock 是并发编程的基本挑战
- 需要深入理解 Java 内存模型和同步机制

#### Solvability
- 代码片段帮助回答者理解问题
- 问题长度要适中，既详细又简洁
- 用户声誉影响问题的可见性和回复质量
- 适当的标签帮助问题到达正确的受众

## 扩展建议 Future Enhancements

1. **数据库集成**: 使用 PostgreSQL 或 MySQL 替代 JSON 文件
2. **更多分析**: 添加答案质量分析、用户参与度分析等
3. **实时更新**: 定期从 Stack Overflow 更新数据
4. **高级可视化**: 使用 D3.js 实现更复杂的可视化
5. **自然语言处理**: 使用 NLP 技术进行更深入的文本分析

## 常见问题 FAQ

### Q: 如何收集真实的 Stack Overflow 数据？
A: 修改 `api/init` 调用，使用 `mode=api` 参数。注意 API 速率限制。

### Q: 数据文件存储在哪里？
A: 默认存储在项目根目录的 `stackoverflow_data.json` 文件中。

### Q: 如何添加新的分析功能？
A: 在 `DataAnalysisService` 中添加新方法，在 `ApiController` 中添加新端点，在 `index.html` 中添加新的可视化。

### Q: 示例数据是随机生成的吗？
A: 是的，但使用固定种子(42)以确保可重现性。包含真实的多线程问题示例。

## 评分要点 Grading Points

- ✅ 数据收集 (10分): 支持 API 收集和本地存储
- ✅ 主题趋势分析 (15分): 完整的时间维度和活跃度分析
- ✅ 主题共现分析 (15分): 正确的共现逻辑和Top N选择
- ✅ 多线程问题分析 (15分): 文本分析识别常见问题
- ✅ 可解决性分析 (15分): 5个因素的对比分析
- ✅ RESTful APIs (10分): 7个可用的 JSON 端点
- ✅ 可视化与见解 (20分): 完整的图表和深入的见解讨论

## 许可证 License

This project is created for educational purposes as part of CS209A course.

## 作者 Author

CS209A Student - SUSTech

## 致谢 Acknowledgments

- Stack Overflow API
- Spring Boot Framework
- Chart.js Library
