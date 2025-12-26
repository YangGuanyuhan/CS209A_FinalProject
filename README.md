# 📊 StackOverflow Java Q&A Analytics Platform (CS209A Final Project)

> A high-performance data visualization and analysis platform for Stack Overflow Java threads, built with Spring Boot and Real-time Stream Processing.

![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)
![Java](https://img.shields.io/badge/Java-22-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green)

## 📖 Project Overview

This project is a web application designed to analyze, visualize, and gain insights from over **1,000+** Stack Overflow questions tagged with `java`. Unlike traditional CRUD applications, this platform focuses on **dynamic, real-time data analysis** using Java's memory-efficient Stream APIs.

It answers critical questions for Java developers, such as topic trends, common multithreading pitfalls, and factors affecting question solvability.

### ✨ Key Features

- **📈 Topic Trend Analysis**: Visualizes the popularity of specific Java topics (e.g., *Spring*, *Concurrency*) over time.
- **🔗 Co-occurrence Graph**: Identifies technology stacks that frequently appear together (e.g., *Spring Boot* + *Hibernate*).
- **⚠️ Multithreading Pitfall Mining**: Uses NLP/Regex techniques to extract common error patterns in concurrency questions.
- **✅ Solvability Analysis**: Compares solved vs. unsolved questions based on multiple dimensions.
- **⚡ Real-time Processing**: All analyses are computed on-the-fly using **Java 8 Streams & Lambdas**, ensuring up-to-date results without database pre-computation.

---

## 🛠 Tech Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Backend** | Java 22, Spring Boot 3.5.7 | Core RESTful API & Business Logic |
| **Analysis** | Java Streams, Regex | In-memory dynamic data processing |
| **Database** | PostgreSQL | Persistent storage for raw Q&A data |
| **Persistence** | MyBatis / JPA | ORM & Data Access Layer |
| **Frontend** | [Vue.js / Thymeleaf] | Data visualization (ECharts/Chart.js) |
| **Build** | Maven / Gradle | Dependency Management |

---

## 🚀 How to Run

### 1. Prerequisites
- **JDK 22** or higher
- **PostgreSQL** (Ensure the service is running)
- **IntelliJ IDEA** (Recommended)

### 2. Database Configuration
1. Create a database named `stackoverflow_data` in PostgreSQL.
2. Import the raw data script (if provided) or allow the application to fetch/initialize data.
3. Update `src/main/resources/application.properties` with your credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/stackoverflow_data
   spring.datasource.username=your_username
   spring.datasource.password=your_password
