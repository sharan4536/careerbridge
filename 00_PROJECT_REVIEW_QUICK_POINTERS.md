# 🟢 00: Project Review Quick Pointer Index

> **Leave this file open during your review!**  
> Whenever your teacher ("mam") asks a question or points to a feature on screen, find the keyword below and click the file link to jump directly to the exact file and line of code.

---

## 🎯 Direct File Pointers by Question

### 1. "Where did you use Multithreading?"
👉 Open: **[`MultithreadingConfig.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/config/MultithreadingConfig.java#L23)**
- **What to show:** The `@Bean public Executor taskExecutor()` method with `corePoolSize(5)`, `maxPoolSize(10)`, and `threadNamePrefix("CareerBridge-Async-")`.
- **Also show worker method:** [`AutoApplyService.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/service/AutoApplyService.java#L60) (`@Async("taskExecutor") runAutoApplyAsync()`) returning `CompletableFuture`.
- **What to say:** *"Mam, we configured a custom `ThreadPoolTaskExecutor` to run heavy batch auto-apply tasks on concurrent worker threads asynchronously so the web UI stays non-blocking."*

---

### 2. "What level of Inheritance have you used?"
👉 Open: **[`JwtAuthenticationFilter.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/security/JwtAuthenticationFilter.java#L17)** (Multilevel)  
👉 Open: **[`AdminDashboard.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/AdminDashboard.java#L39)** (Single)
- **What to show in `JwtAuthenticationFilter`:** Multilevel chain: `GenericFilterBean` $\rightarrow$ `OncePerRequestFilter` $\rightarrow$ `JwtAuthenticationFilter`.
- **What to show in `AdminDashboard`:** Single class inheritance: `AdminDashboard extends Application` (JavaFX).
- **What to say:** *"Mam, we have Multilevel Inheritance in our security filter, Single Inheritance in our JavaFX dashboard, and Hierarchical Interface Inheritance across all our Spring Data JPA repositories."*

---

### 3. "Where is the Eligibility Verification code?"
👉 Open: **[`EligibilityEngine.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/service/engine/EligibilityEngine.java#L32)**
- **What to show:** `checkEligibility(student, req)` comparing CGPA, active backlogs, passing year, and eligible branches.
- **Java Concept:** OOP Encapsulation, `EligibilityStatus` Enum, and DTO Pattern.

---

### 4. "Where is the Job Fit Match Score (88%) code?"
👉 Open: **[`JobFitEngine.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/service/engine/JobFitEngine.java#L35)**
- **What to show:** `calculateJobFit()` with weighted scoring: 40% skills match, 20% CGPA, 15% projects, 15% experience, 10% keywords.
- **Java Concept:** Java Streams API, `Set` lookup theory, and Math API rounding.

---

### 5. "Where is the Skill Gap Analysis code?"
👉 Open: **[`SkillGapAnalyzer.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/service/engine/SkillGapAnalyzer.java#L28)**
- **What to show:** `analyze()` comparing student skills with required skills to extract matched vs missing skills and recommendations.
- **Java Concept:** Java Collections (`List`, `ArrayList`, `Set`), Stream Lambdas, and String parsing.

---

### 6. "Show me direct JDBC code or PreparedStatement!"
👉 Open: **[`AdminDashboard.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/AdminDashboard.java#L150)** & **[`JdbcDatabasePersistenceService.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/service/JdbcDatabasePersistenceService.java#L71)**
- **What to show in `AdminDashboard`:** `insertUserViaJdbc()` using `PreparedStatement pstmt = conn.prepareStatement(sql)` with `?` parameters to prevent SQL injection.
- **What to show in `JdbcDatabasePersistenceService`:** `CHECKPOINT SYNC` and `SCRIPT TO './backups/*.sql'`.
- **Java Concept:** JDBC `Connection`, `PreparedStatement`, `Statement`, `ResultSet`, and `DatabaseMetaData`.

---

### 7. "What happens if a student updates their resume after applying?"
👉 Open: **[`ApplicationResumeSnapshot.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/entity/ApplicationResumeSnapshot.java#L15)**
- **What to show:** `resumeName`, `resumeVersion`, `fileReference`, and `contentHash`.
- **What to say:** *"Mam, we implemented the Immutable Snapshot Pattern. At the instant of application, we freeze an audit snapshot of the resume version and file path. Even if the student subsequently alters their resume profile, the recruiter's copy remains completely unchanged."*

---

### 8. "Where is the file upload and path security?"
👉 Open: **[`LocalFileStorageServiceImpl.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/service/impl/LocalFileStorageServiceImpl.java#L31)**
- **What to show:** `storeFile()` checking `if (originalFileName.contains("..")) throw ...`, generating a `UUID`, and using `Files.copy()`.
- **Java Concept:** Java NIO API (`Path`, `Files`), UUID collision prevention, and directory traversal defense.

---

### 9. "Where is the Security / JWT token generation?"
👉 Open: **[`SecurityConfig.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/config/SecurityConfig.java#L50)** & **[`JwtTokenProvider.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/security/JwtTokenProvider.java#L25)**
- **What to show:** `filterChain()` configuring stateless JWT sessions, and `generateToken()` signing tokens with HMAC-SHA256 (`HS256`).
- **Java Concept:** Cryptographic hashing, Spring Security Filter Chain, and Bearer token verification.

---

### 10. "Where are global errors handled?"
👉 Open: **[`GlobalExceptionHandler.java`](file:///Users/sharangoshreddy/.gemini/antigravity-ide/scratch/careerbridge/src/main/java/com/careerbridge/exception/GlobalExceptionHandler.java#L13)**
- **What to show:** `@ControllerAdvice` intercepting `RuntimeException` and returning HTTP 400 with a clean JSON message.
- **Java Concept:** Aspect-Oriented centralized exception handling.
