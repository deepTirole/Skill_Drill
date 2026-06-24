Extract all unique technical skills from the resume text.
Rules:
1. Include ONLY:
   * Programming languages (Java, Python, C++, JavaScript, TypeScript, Go, etc.)
   * Frameworks and libraries (Spring Boot, Angular, React, Hibernate, Node.js, Express, etc.)
   * Databases (MySQL, PostgreSQL, MongoDB, Redis, etc.)
   * Cloud and DevOps tools (AWS, Docker, Kubernetes, Jenkins, GitHub Actions, etc.)
   * Development tools and platforms (Git, GitHub, Linux, Postman, Firebase, Maven, Gradle, etc.)
   * Major technical domains (Machine Learning, Data Science, Android Development, System Design, Cybersecurity, etc.)
2. Exclude:
   * DSA topics and problem-solving techniques (Arrays, Strings, Linked Lists, Stacks, Queues, Trees, Graphs, Binary Search, Dynamic Programming, Greedy, Recursion, Sliding Window, Two Pointers, Backtracking, etc.)
   * Academic subjects (DBMS, OOP, Operating Systems, Computer Networks, Software Engineering, etc.) unless explicitly listed as a technology stack or area of expertise.
   * Soft skills (Communication, Leadership, Teamwork, Problem Solving, etc.)
   * Job titles, company names, certifications, project names, achievements, and responsibilities.
3. Normalize names:
   * ReactJS → React
   * React.js → React
   * NodeJS → Node.js
   * SpringBoot → Spring Boot
   * JS → JavaScript
   * TS → TypeScript
4. Remove duplicates.
5. Return ONLY a valid JSON array of strings. No explanation, markdown, or extra text.
Resume text:
{resumeText}
