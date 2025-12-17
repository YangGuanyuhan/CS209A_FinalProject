# 🚀 真实数据收集指南

## 概述

本指南说明如何使用独立的数据收集器从 Stack Overflow API 抓取真实数据。

## 步骤

### 第 1 步：获取 API Key

1. 访问 [Stack Apps](https://stackapps.com/apps/oauth/register)
2. 使用 Stack Overflow 账号登录
3. 填写应用信息（随便填，用于学习目的）
4. 获取你的 **API Key**（不是 Access Token）

### 第 2 步：运行数据收集器

打开 PowerShell，进入 `data-collector` 目录：

```powershell
cd e:\Desktop\java2project\CS209A_FinalProject_demo\data-collector

# 编译
javac -encoding UTF-8 StackOverflowDataCollector.java

# 运行（将 YOUR_API_KEY 替换为你的真实 API Key）
java StackOverflowDataCollector YOUR_API_KEY 1000 ..\stackoverflow_data.json
```

或者使用脚本：

```powershell
# PowerShell
.\collect-data.ps1 -ApiKey "YOUR_API_KEY" -Count 1000 -OutputFile "..\stackoverflow_data.json"
```

### 第 3 步：等待收集完成

- 收集 1000 个问题大约需要 **20-40 分钟**
- 程序会显示实时进度
- 数据会自动保存到项目根目录的 `stackoverflow_data.json`

### 第 4 步：启动主项目

```powershell
cd e:\Desktop\java2project\CS209A_FinalProject_demo
.\mvnw.cmd spring-boot:run
```

### 第 5 步：访问应用

打开浏览器：`http://localhost:8080`

点击 "Load Data" 按钮，应用会自动加载你收集的真实数据！

---

## 完整命令（复制粘贴即可）

```powershell
# 1. 进入数据收集器目录
cd e:\Desktop\java2project\CS209A_FinalProject_demo\data-collector

# 2. 编译
javac -encoding UTF-8 StackOverflowDataCollector.java

# 3. 收集数据（替换 YOUR_API_KEY）
java StackOverflowDataCollector YOUR_API_KEY 1000 ..\stackoverflow_data.json

# 4. 返回主项目目录
cd ..

# 5. 启动应用
.\mvnw.cmd spring-boot:run
```

---

## 常见问题

### Q: 收集速度很慢？
A: 为了遵守 API 使用政策，每次请求间隔约 1 秒。这是正常的。

### Q: 出现 "Quota remaining: 0"？
A: 你的每日配额用完了。等待第二天（UTC 时间）重置。

### Q: JSON 文件在哪里？
A: 在项目根目录：`e:\Desktop\java2project\CS209A_FinalProject_demo\stackoverflow_data.json`

### Q: 如何验证数据是真实的？
A: 查看 JSON 文件中的 `questionId`，访问 `https://stackoverflow.com/questions/{questionId}` 验证。

---

## API 配额说明

| 类型 | 配额 | 说明 |
|------|------|------|
| 无 Key | 300/天 | 不够用 |
| 有 Key | 10,000/天 | 足够收集 1000-2000 个问题 |

---

## 项目结构

```
CS209A_FinalProject_demo/
├── data-collector/                    # 独立的数据收集器（与主项目分离）
│   ├── StackOverflowDataCollector.java
│   ├── collect-data.bat
│   ├── collect-data.ps1
│   └── README.md
├── stackoverflow_data.json            # 收集的数据文件（运行后生成）
├── src/                               # 主项目源码
└── ...
```

---

## 成功标志

当你看到以下输出时，表示数据收集成功：

```
========================================
Collection completed!
Total questions: 1000
Output file: ..\stackoverflow_data.json
========================================
```

然后启动主项目，点击 "Load Data"，会看到：

```
Real data loaded from stackoverflow_data.json
```

🎉 **恭喜！你现在使用的是真实的 Stack Overflow 数据！**
