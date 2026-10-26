# CareerBridge: Website Feature → Java Code & Concept Cheat Sheet

> **For Your Viva / Review:** When your evaluator ("mam") points to any button, card, or screen in the website and asks:
> 1. *"Show me the Java code for this feature."*
> 2. *"Explain what this code does."*
> 3. *"What Java concept have you used here?"*
> 
> Look up the feature below to find the exact file, method, explanation, and Java concepts.

---

## Quick Navigation Index

| Feature on Screen | Java File to Open | Primary Java Concept |
| :--- | :--- | :--- |
| [1. "Check Eligibility" Button](#1-feature-check-eligibility-button-on-job-card) | `EligibilityEngine.java` | **OOP Encapsulation, Conditional Logic, Builder Pattern, DTO Pattern** |
| [2. "Analyze Match / Job Fit Score" (88% Score)](#2-feature-job-fit-score--compatibility-circle-eg-88) | `JobFitEngine.java` | **Java Streams API, Set Theory, Type Casting, Math API** |
| [3. "Skill Gap Analysis" (Matched vs Missing Skills)](#3-feature-skill-gap-analysis-badge--recommendations) | `SkillGapAnalyzer.java` | **Java Collections (List, Set), Stream Filtering, Lambda Expressions** |
| [4. "Apply Now" Button (Submitting Application)](#4-feature-apply-now-modal--application-submission) | `ApplicationServiceImpl.java` | **Spring `@Transactional`, Immutable Snapshot Pattern, Audit Trail** |
| [5. "One-Click Auto-Apply" Button](#5-feature-one-click-auto-apply-button) | `AutoApplyService.java` | **Automated Batch Processing, Service Composition, Exception Handling (`try-catch`)** |
| [6. Resume Upload (PDF File)](#6-feature-upload-new-resume-version-pdf-file) | `LocalFileStorageServiceImpl.java` | **Java NIO Filesystem API (`Path`, `Files`), UUID Generation, Path Traversal Security** |
| [7. Download Resume PDF](#7-feature-download-resume-pdf-button) | `ResumeServiceImpl.java` + `ResumeController.java` | **Binary Streaming (`byte[]`), HTTP Header MIME Streaming (`MediaType.APPLICATION_OCTET_STREAM`)** |
| [8. AI Resume Review / ATS Critique](#8-feature-ai-ats-resume-analysis--cover-letter-generator) | `AIService.java` | **Java Collections (`Map<String, Object>`, `Arrays.asList()`), String Templating** |
| [9. Recruiter Changing Status (e.g. "Interview Scheduled")](#9-feature-recruiter-updating-status--timeline-events) | `ApplicationServiceImpl.java` | **Entity State Mutation, Cascading Persistence, Audit Logging** |
| [10. User Login & Registration](#10-feature-user-login--registration) | `AuthServiceImpl.java` + `SecurityConfig.java` | **Spring Security, BCrypt Hashing, JWT Cryptography (HMAC-SHA256)** |
| [11. Database Backup (SQL Dump) Button](#11-feature-admin-database-checkpoint--sql-backup-button) | `JdbcDatabasePersistenceService.java` | **Direct JDBC, Spring `JdbcTemplate`, Low-Level SQL Execution (`CHECKPOINT SYNC`, `SCRIPT TO`)** |
| [12. Desktop Admin Tool (JavaFX Table & Forms)](#12-feature-standalone-desktop-admin-tool) | `AdminDashboard.java` | **JavaFX UI Components, JDBC `PreparedStatement` (Parameterized SQL) vs `Statement`** |
| [13. Student Dashboard Placement Statistics](#13-feature-student-dashboard-stats-interviews-offers-rejections) | `DashboardService.java` | **Java Stream Reductions (`mapToDouble`, `average`), Map Aggregations** |
| [14. Error Popups / Validation Failure](#14-feature-error-popups-invalid-password-email-already-taken) | `GlobalExceptionHandler.java` | **Spring `@ControllerAdvice`, `@ExceptionHandler`, Centralized Aspect-Oriented Handling** |

---

## 1. Feature: "Check Eligibility" Button on Job Card

- **Where Mam points:** The green/red **"Check Eligibility"** button under any job posting on the Jobs page.
- **File to Open:** `src/main/java/com/careerbridge/service/engine/EligibilityEngine.java`
- **Method:** `checkEligibility(Student student, JobRequirement req)`
- **What to Explain:**
  > *"When the student clicks 'Check Eligibility', this method compares the student's profile attributes against the company's criteria. It checks four conditions: minimum CGPA, maximum backlogs allowed, graduation passing out year, and eligible engineering branch. Instead of a simple yes/no, it populates an audit list of reasons so the student knows exactly why they qualified or got disqualified."*
- **Java Concepts Used:**
  1. **OOP Encapsulation & POJOs:** Accessing student and job attributes via getter methods (`student.getCgpa()`, `req.getMinCgpa()`).
  2. **Enum (Enumeration):** `EligibilityStatus` enum (`ELIGIBLE`, `NOT_ELIGIBLE`, `UNCLEAR`) ensures type safety instead of raw magic strings.
  3. **Data Transfer Object (DTO) Pattern:** Returning `EligibilityResult` containing both status and `List<String> reasons`.
  4. **Null Safety:** Checking `req.getMinCgpa() != null` to avoid `NullPointerException`.

---

## 2. Feature: Job Fit Score / Compatibility Circle (e.g. 88%)

- **Where Mam points:** The circular score indicator showing **"88% Job Fit"** on the job card.
- **File to Open:** `src/main/java/com/careerbridge/service/engine/JobFitEngine.java`
- **Method:** `calculateJobFit(Student student, List<StudentSkill> studentSkills, JobRequirement req)`
- **What to Explain:**
  > *"This feature calculates a multi-dimensional weighted compatibility score between 0 and 100%. We don't just match keywords; we calculate 5 sub-scores: 40% for technical skills match, 20% for CGPA, 15% for project proficiency, 15% for experience fit, and 10% for branch keywords. We then compute the composite weighted average."*
- **Java Concepts Used:**
  1. **Java Streams & Lambdas:** `Arrays.stream(req.getRequiredSkills().split(",")).map(s -> s.trim().toLowerCase()).collect(...)`
  2. **Java Collections (`Set` theory):** Creating a `Set<String>` of student skills to perform $O(1)$ lookup matching via `.filter(candidateSkills::contains).count()`.
  3. **Type Casting & Floating-Point Arithmetic:** `(double) matches / requiredList.size()` to avoid integer division truncation.
  4. **Java Math API:** `Math.round(overall * 10.0) / 10.0` to round the final result to one decimal place.

---

## 3. Feature: Skill Gap Analysis Badge & Recommendations

- **Where Mam points:** The **"Skill Gap"** modal showing green pills (Matched Skills) and yellow pills (Missing Skills with advice).
- **File to Open:** `src/main/java/com/careerbridge/service/engine/SkillGapAnalyzer.java`
- **Method:** `analyze(List<StudentSkill> studentSkills, JobRequirement req)`
- **What to Explain:**
  > *"This feature performs mathematical set difference between what the company wants and what the candidate knows. It iterates through the required skills: if the student has it, it adds it to `matchedSkills`; if missing, it adds it to `missingSkills` and appends a personalized study recommendation."*
- **Java Concepts Used:**
  1. **Java Collections Framework:** Using `List<String>` and `ArrayList` to store and partition matched vs missing items.
  2. **Stream Filtering:** Transforming entity objects (`StudentSkill`) into lowercase strings using `map(ss -> ss.getSkill().getName().toLowerCase())`.
  3. **String Manipulation:** Using `.split(",")`, `.trim()`, and `.toLowerCase()` for case-insensitive matching.

---

## 4. Feature: "Apply Now" Modal & Application Submission

- **Where Mam points:** Clicking **"Apply"**, selecting a resume version, and clicking the blue **"Submit Application"** button.
- **File to Open:** `src/main/java/com/careerbridge/service/impl/ApplicationServiceImpl.java`
- **Method:** `submitApplication(String email, ApplicationRequest request)`
- **What to Explain:**
  > *"This method handles job application submission. First, it verifies that the student actually owns the selected resume version for security. Second, it creates an `ApplicationResumeSnapshot`—which freezes the resume version and file reference forever. Third, it calculates real-time eligibility and fit scores and saves the application. Finally, it creates an `ApplicationEvent` for timeline auditing."*
- **Java Concepts Used:**
  1. **Spring `@Transactional` Management:** Ensures ACID properties; if saving the audit event fails, the application is rolled back.
  2. **Design Pattern: Immutable Snapshot Pattern:** `ApplicationResumeSnapshot` prevents future edits by the student from altering historical job submissions.
  3. **Object-Relational Mapping (ORM):** Managing `@ManyToOne` and `@OneToOne` JPA relationships.
  4. **Builder Pattern (Lombok `@Builder`):** Creating immutable objects cleanly (`Application.builder().student(student).job(job)...build()`).

---

## 5. Feature: "One-Click Auto-Apply" Button

- **Where Mam points:** The purple/gradient **"Auto-Apply"** button at the top of the Jobs page.
- **File to Open:** `src/main/java/com/careerbridge/service/AutoApplyService.java`
- **Method:** `runAutoApply(String email, Long defaultResumeVersionId)`
- **What to Explain:**
  > *"Auto-Apply iterates through every open job in the database. For each job, it invokes `EligibilityEngine`. If the student is ELIGIBLE, it automatically creates and submits an application using the student's chosen resume version. If ineligible, it skips the job and reports why."*
- **Java Concepts Used:**
  1. **Service Layer Composition:** Injecting and calling `JobRepository`, `JobService`, and `ApplicationService` (Separation of Concerns).
  2. **Batch Processing via Iteration:** Using standard Java `for (Job job : allJobs)`.
  3. **Robust Exception Handling:** Wrapping each job application in a `try-catch` block so that a failure on one job does not abort the remaining applications.

---

## 6. Feature: Upload New Resume Version (PDF File)

- **Where Mam points:** In the Resumes tab, clicking **"Upload New Version"** and picking a `.pdf` file from your computer.
- **File to Open:** `src/main/java/com/careerbridge/service/impl/LocalFileStorageServiceImpl.java`
- **Method:** `storeFile(MultipartFile file, Long studentId)`
- **What to Explain:**
  > *"This handles multi-part file uploads. To prevent hackers from overwriting system files using relative path traversal (like `../../etc/passwd`), we clean the filename. We then generate a unique file name using `UUID.randomUUID()` and copy the file stream directly to disk using Java NIO `Files.copy()`."*
- **Java Concepts Used:**
  1. **Java NIO (New I/O) API:** Using `java.nio.file.Path`, `Paths.get()`, and `Files.createDirectories()`.
  2. **Security & Defensive Programming:** Checking `if (originalFileName.contains("..")) throw new RuntimeException(...)` against directory traversal attacks.
  3. **UUID (Universally Unique Identifier):** `UUID.randomUUID().toString()` prevents filename collisions.
  4. **I/O Streaming & Atomic Replacement:** `Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING)`.

---

## 7. Feature: Download Resume PDF Button

- **Where Mam points:** The download icon / button next to any resume version.
- **Files to Open:**
  1. Service: `src/main/java/com/careerbridge/service/impl/ResumeServiceImpl.java` (`downloadResumeVersionFile`)
  2. Controller: `src/main/java/com/careerbridge/controller/ResumeController.java` (`downloadVersion`)
- **What to Explain:**
  > *"When downloaded, `ResumeServiceImpl` checks authorization to ensure students can only download their own resumes. It reads the raw bytes from disk with `Files.readAllBytes()`. The controller packages the byte array with HTTP headers (`Content-Type: application/octet-stream` and `Content-Disposition: attachment; filename=resume.pdf`) so the browser triggers an instant file download."*
- **Java Concepts Used:**
  1. **Binary Data Handling:** `byte[]` arrays for file transfer.
  2. **Access Control / Authorization:** `if (!version.getResume().getStudent().getId().equals(student.getId())) throw ...`
  3. **Spring HTTP Abstractions:** `ResponseEntity<byte[]>`, `HttpHeaders`, and `MediaType`.

---

## 8. Feature: AI / ATS Resume Analysis & Cover Letter Generator

- **Where Mam points:** The **"ATS Review"** button in Resumes, or **"Generate Cover Letter"** in AI Tools.
- **File to Open:** `src/main/java/com/careerbridge/service/AIService.java`
- **Methods:** `analyzeResume(Long versionId)` and `generateCoverLetter(...)`
- **What to Explain:**
  > *"This service simulates an ATS (Applicant Tracking System). It evaluates formatting, action verbs, and quantifiable metrics, returning structured strengths, weaknesses, and improvement suggestions. The cover letter generator dynamically synthesizes the candidate's name, target role, and employer requirements into a professional application letter."*
- **Java Concepts Used:**
  1. **Java Collections:** Using `HashMap<String, Object>` and `Arrays.asList(...)` for dynamic JSON response structures.
  2. **String Manipulation:** String concatenation and `.substring()` with ternary boundary checks `(jobDescription.length() > 120 ? ... : ...)`.

---

## 9. Feature: Recruiter Updating Status & Timeline Events

- **Where Mam points:** In the Recruiter Dashboard, clicking the status dropdown and changing an applicant to **"INTERVIEW_SCHEDULED"** or **"OFFERED"**.
- **File to Open:** `src/main/java/com/careerbridge/service/impl/ApplicationServiceImpl.java`
- **Method:** `updateStatus(String email, Long id, String newStatus)`
- **What to Explain:**
  > *"When the recruiter updates an applicant's status, this method verifies recruiter privileges, mutates the `Application.status` field, and persists the update. Concurrently, it creates a new `ApplicationEvent` containing an event type (`STATUS_UPDATED`), timestamp, and description. This builds an unalterable chronological audit timeline."*
- **Java Concepts Used:**
  1. **Role-Based Authorization (RBAC):** `if (user.getRole() == Role.RECRUITER || user.getRole() == Role.ADMIN)`.
  2. **Event Sourcing / Audit Logging:** Rather than merely overwriting data, an `ApplicationEvent` entity logs state changes over time.
  3. **Encapsulation:** Mutating entity state via setter methods (`app.setStatus(newStatus)`) and persisting through `applicationRepository.save(app)`.

---

## 10. Feature: User Login & Registration

- **Where Mam points:** The Login screen or Register screen.
- **Files to Open:**
  1. Service: `src/main/java/com/careerbridge/service/impl/AuthServiceImpl.java`
  2. Config: `src/main/java/com/careerbridge/config/SecurityConfig.java`
  3. Token: `src/main/java/com/careerbridge/security/JwtTokenProvider.java`
- **What to Explain:**
  > *"In `AuthServiceImpl.java`, when a user registers, their raw password is encrypted using `BCryptPasswordEncoder` (salted cryptographic hash) so plaintext passwords never touch the database. When logging in, Spring's `AuthenticationManager` validates the credentials. Upon success, `JwtTokenProvider` generates a signed JWT token containing the user's email and role."*
- **Java Concepts Used:**
  1. **Cryptography & Security:** BCrypt password hashing and HMAC-SHA256 (`HS256`) secret key signing (`Keys.hmacShaKeyFor`).
  2. **Spring Security Architecture:** `AuthenticationManager`, `UsernamePasswordAuthenticationToken`, `SecurityContextHolder`.
  3. **Stateless REST Pattern:** No HTTP session cookies stored on the server.

---

## 11. Feature: Admin Database Checkpoint & SQL Backup Button

- **Where Mam points:** In the Admin Dashboard, clicking **"Checkpoint Database"** or **"Create SQL Backup"**.
- **File to Open:** `src/main/java/com/careerbridge/service/JdbcDatabasePersistenceService.java`
- **Methods:** `flushCheckpointToDisk()` and `saveDatabaseBackup(String note)`
- **What to Explain:**
  > *"This feature demonstrates direct low-level JDBC control. `flushCheckpointToDisk()` executes the SQL command `CHECKPOINT SYNC` through `JdbcTemplate` to flush all in-memory database pages directly to disk. `saveDatabaseBackup()` triggers the H2 JDBC command `SCRIPT TO 'filepath'`, creating a complete SQL backup dump in the `./backups/` directory."*
- **Java Concepts Used:**
  1. **Direct JDBC (Java Database Connectivity):** Using Spring's `JdbcTemplate.execute()` for database maintenance commands.
  2. **Date & Time Formatting:** `SimpleDateFormat("yyyyMMdd_HHmmss")` to generate chronological backup filenames.
  3. **JDBC Metadata Inspection:** In `getDatabaseStatus()`, using `DatabaseMetaData meta = conn.getMetaData()` to inspect tables, row counts, and driver versions dynamically.

---

## 12. Feature: Standalone Desktop Admin Tool

- **Where Mam points:** If Mam asks: *"Have you done any Java GUI / Desktop code or direct JDBC with PreparedStatement?"*
- **Files to Open:** `src/main/java/com/careerbridge/AdminDashboard.java` & `src/main/java/com/careerbridge/Launcher.java`
- **Methods:** `fetchUsers()` and `insertUserViaJdbc()`
- **What to Explain:**
  > *"We created an independent administrative desktop application using JavaFX that communicates with the database using pure JDBC. In `insertUserViaJdbc()`, we use `java.sql.PreparedStatement` with parameterized placeholders (`?`) to prevent SQL injection. In `fetchUsers()`, we use `java.sql.Statement` and `ResultSet` to populate an interactive JavaFX `TableView`."*
- **Java Concepts Used:**
  1. **JavaFX GUI Framework:** `Application`, `Stage`, `Scene`, `TableView`, `TableColumn`, `Button`, `VBox`, `HBox`.
  2. **JDBC Interfaces:** `DriverManager.getConnection()`, `java.sql.Connection`, `java.sql.PreparedStatement`, `java.sql.ResultSet`.
  3. **SQL Injection Defense:** `pstmt.setString(1, email); pstmt.setString(2, password);`
  4. **JavaFX Observable Collections:** `ObservableList<UserRow> users = FXCollections.observableArrayList()`.

---

## 13. Feature: Student Dashboard Stats (Interviews, Offers, Rejections)

- **Where Mam points:** The 4 summary metric boxes at the top of the Student Dashboard.
- **File to Open:** `src/main/java/com/careerbridge/service/DashboardService.java`
- **Method:** `getDashboardStats(String email)`
- **What to Explain:**
  > *"This method aggregates all student applications. It loops through them and tallies interviews, rejections, and offers based on status strings. It also calculates the average job fit score across all applied jobs using Java Streams."*
- **Java Concepts Used:**
  1. **Java Streams & Numerical Reductions:** `apps.stream().mapToDouble(a -> a.getJobFitScore()).average().orElse(0.0)`
  2. **Hash Map Frequency Counting:** `resumeStats.put(resumeName, resumeStats.getOrDefault(resumeName, 0) + 1)` to track which resume version gets the most applications.

---

## 14. Feature: Error Popups (Invalid Password, Email Already Taken)

- **Where Mam points:** Any red popup alert that appears when input is wrong or duplicate.
- **File to Open:** `src/main/java/com/careerbridge/exception/GlobalExceptionHandler.java`
- **Methods:** `handleRuntimeException` and `handleValidationExceptions`
- **What to Explain:**
  > *"Instead of letting raw Java stack traces crash the server or display on the frontend, this class acts as a global interceptor. When any service throws a `RuntimeException`, this class catches it and returns a clean HTTP 400 Bad Request with a human-readable error message in JSON format."*
- **Java Concepts Used:**
  1. **Aspect-Oriented Exception Handling:** Spring `@ControllerAdvice` and `@ExceptionHandler`.
  2. **Validation Error Extraction:** Iterating across `ex.getBindingResult().getFieldErrors()` with a lambda to map field names to validation messages.
