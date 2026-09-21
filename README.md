Skill_Drill
Skill_Drill is an automated, AI-driven mock interview platform designed to bridge the gap between academic learning and professional job readiness. The system eliminates generic interview preparation by allowing candidates to upload unstructured resumes, which the platform parses to extract technical skills. By combining these extracted keywords with target job roles, the platform dynamically schedules tailored mock interviews and evaluates answers in real-time.

System Architecture & Highlights
1.) Non-Linear Progression Engine: Calculates adaptive interview difficulties using a custom mathematical Elo Rating algorithm within a @Transactional service layer, utilizing dynamic K-factors to introduce diminishing returns and prevent rating inflation for expert-tier users.

2.) Stateless Security Pipeline: Enforces production-level authentication using Spring Security and JSON Web Tokens (JWT) with 256-bit Base64URL-encoded signature verification (HS256), paired with a transactional Email OTP verification flow.

3.) Asynchronous Evaluation Loop: Decouples the AI grading logic using Spring's @Async worker threads to process technical answers in the background without holding HTTP connection threads hostage, ensuring client-side stability.

Core Technologies
1.) Backend Ecosystem: Java, Spring Boot, Spring Security, Spring AI, Spring Retry.

2.) Database & ORM: PostgreSQL with a highly normalized schema (mapping Strong and Weak entities via explicit cascade styling) and Hibernate ORM/Spring Data JPA.

3.) Frontend Interface: Angular 18+, Tailwind CSS, and native Web Speech API for seamless Text-to-Speech (TTS) integration.

4.) Cloud & Containerization: Docker, AWS EC2, Amazon RDS, Amazon S3, and CloudFront.

Unstructured Data Pipeline
Incoming PDF resumes are parsed on the fly using the Apache Tika library to extract core technical competencies asynchronously. To ensure highly optimized storage, this unstructured data is mapped into a relational USER_KEYWORDS many-to-many join table, acting as a "Master Skill Library" that prevents duplicate keyword string storage while tracking individual proficiency levels.

Deployment Architecture
The backend ecosystem and persistent data tier are containerized on a single AWS EC2 instance orchestrated via Docker Compose. The multi-container stack runs the Spring Boot application alongside a dedicated PostgreSQL database connected through an isolated internal bridge network, utilizing named Docker volumes to ensure strict data persistence across container lifecycles. Inbound web traffic is routed through a host-level Nginx reverse proxy configured with SSL termination to enforce end-to-end HTTPS encryption. In parallel, the compiled Angular frontend is hosted statically in a private Amazon S3 bucket and distributed globally at ultra-low latency via the AWS CloudFront edge caching network.
