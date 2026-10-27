# CareerBridge – Smart Campus Placement & Career Intelligence Portal

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2F%204.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Security-JWT%20Stateless-blue.svg)](https://spring.io/projects/spring-security)
[![React](https://img.shields.io/badge/Frontend-React%2019%20%2B%20Vite-61dafb.svg)](https://react.dev/)
[![Database](https://img.shields.io/badge/Database-H2%20%2F%20MySQL-yellow.svg)](https://www.h2database.com/)

CareerBridge is an enterprise-grade full-stack campus recruitment platform engineered with **Java 17**, **Spring Boot**, **Spring Data JPA**, **Spring Security (JWT)**, and **React + Vite**. It replaces fragmented spreadsheets with an automated, transparent placement pipeline featuring deterministic academic eligibility checks, weighted job fit algorithms, skill gap analysis, resume versioning with immutable application snapshots, and an administrative JDBC desktop management tool.

---

## 🌟 Key Features
- **Deterministic Eligibility Engine**: Real-time evaluation of student eligibility against CGPA, allowable backlogs, and graduation year criteria.
- **Job Fit Matching Algorithm**: Weighted compatibility scoring analyzing skill overlaps, coursework, and credentials.
- **Skill Gap Diagnostics**: Intelligent breakdown of missing competencies with actionable learning recommendations.
- **Resume Versioning & Immutable Snapshots**: Cryptographic file hashing preserving application state at the instant of submission.
- **Asynchronous Auto-Application Processing**: Non-blocking worker threads managing concurrent candidate applications.
- **Role-Based Access Control**: Secure JWT stateless authentication separating Student, Recruiter, and Admin portals.
- **Dual Database Access Layer**: High-productivity Spring Data JPA paired with direct JDBC persistence and checkpoint management.

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- Java Development Kit (JDK 17 or higher)
- Node.js (v18 or higher) & npm

### 2. Start the Backend (Spring Boot)
```bash
./mvnw spring-boot:run
```
- The backend will start on **`http://localhost:8080`**.
- An embedded H2 file database is initialized automatically with pre-seeded test data (`DataSeeder.java`).

### 3. Start the Frontend (React + Vite)
In a separate terminal:
```bash
cd frontend
npm install
npm run dev
```
- The application will be accessible at **`http://localhost:5173`**.

### 4. (Optional) Run the JavaFX Admin Desktop Tool
```bash
./mvnw javafx:run
# Or run com.careerbridge.Launcher directly from your IDE
```

---

## 🔑 Demo Login Accounts

| Role | Email Address | Password | Key Features to Test |
| :--- | :--- | :--- | :--- |
| **Student** | `student@careerbridge.com` | `password123` | Check Eligibility, Job Fit Score, Skill Gap, Resume Upload, One-Click Auto-Apply |
| **Recruiter** | `recruiter@careerbridge.com` | `password123` | View Applicants, Review Immutable Resume Snapshots, Update Status (`INTERVIEW_SCHEDULED`) |
| **Admin** | `admin@careerbridge.com` | `password123` | Placement Analytics, Direct JDBC Checkpoint, Database SQL Dumps & Schema Metadata |

---

## 📂 Project Architecture

```
careerbridge/
├── src/main/java/com/careerbridge/
│   ├── config/             # Spring Security & CORS configuration
│   ├── security/           # JWT token provider, filter & user details
│   ├── entity/             # JPA entity models (User, Student, Job, Application, etc.)
│   ├── repository/         # Spring Data JPA repositories
│   ├── service/            # Business service interfaces
│   ├── service/impl/       # Service implementations (Job, App, Resume, FileStorage)
│   ├── service/engine/     # Algorithmic engines (Eligibility, JobFit, SkillGap)
│   ├── controller/         # REST API endpoints
│   ├── dto/                # Request & response transfer objects
│   ├── exception/          # Centralized GlobalExceptionHandler
│   ├── AdminDashboard.java # Standalone JavaFX JDBC Administrative GUI
│   ├── Launcher.java       # JavaFX desktop entry point
│   ├── DataSeeder.java     # Startup demo data populator
│   └── CareerbridgeApplication.java # Spring Boot main entry point
├── frontend/               # React 19 + Vite UI
├── backups/                # JDBC SQL database backup dumps
├── uploads/                # Secure candidate resume PDF storage
├── pom.xml                 # Maven build dependencies & plugins
└── README.md               # Project documentation
```
