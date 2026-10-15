# DevOps Capstone Project: Viva Defense & Examination Guide
## Secure CI/CD Pipeline for a Loan Application Tracking Portal

---

## 1. 5-Minute Live Demonstration Script (For Examiners)

Follow this structured sequence to demonstrate your project during your viva / practical presentation:

```text
Step 1: The Codebase & Version Control
- Open your GitHub repository in browser: https://github.com/vickypukale/loan-application-tracking-portal
- Highlight:
  - Branching strategy (main, develop, feature branches).
  - Release tag v1.0.0.
  - Declarative Jenkinsfile, Dockerfile, and Ansible playbooks in version control.

Step 2: The CI/CD Pipeline (Jenkins)
- Open Jenkins at http://localhost:8080 -> Loan-Portal-Pipeline-Job.
- Show the 7-Stage Visual Pipeline:
  [Checkout] -> [Unit Tests] -> [Selenium Quality Gate] -> [Build Docker Image] -> [Deploy Container] -> [Health Check]
- Explain: "Our pipeline automatically executes Selenium browser tests in headless Chrome before packaging. If any assertion fails, deployment is aborted."

Step 3: Live Application Demonstration
- Open http://localhost:8081/dashboard
- Demonstrate:
  1. The 4 KPI Cards (Total, In Intake, Approved, Rejected).
  2. Real-time search filter by Application ID or Name.
  3. Click "+ New Application" (http://localhost:8081/apply) -> Submit a new borrower record.
  4. Show generated ID (e.g., LOAN-1002) and click "Drill-Down / Timeline" to show the timestamped audit log.
  5. Show http://localhost:8081/alerts displaying flagged and rejected applications with reasons.

Step 4: Docker & Container Runtime
- In terminal, run: docker ps -f name=loan-portal-app
- Show: The container is running with image loan-portal:build-<N>, port mapped to 8081, with status "(healthy)".

Step 5: Operational Reliability & Health Check
- Open http://localhost:8081/actuator/health
- Show: {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
- Mention: Ansible playbook enforces 100% idempotency and automates disaster recovery rollback within 15 seconds.
```

---

## 2. DORA Metrics Achieved

| Metric | Industry Standard | Achieved in This Project |
| :--- | :--- | :--- |
| **Deployment Frequency** | High / Daily | **On-Demand / Automated on Git push** |
| **Lead Time for Changes** | $< 1$ Day | **Sub-5 minutes** (Git push to running container) |
| **Change Failure Rate** | $< 15\%$ | **0% in Production** (Prevented by Selenium Quality Gate) |
| **Mean Time to Recovery (MTTR)**| $< 1$ Hour | **$< 15$ seconds** (Automated rollback to `v1.0.0`) |

---

## 3. Top 15 Viva Questions & Model Answers

### Q1: Why did you choose a Declarative Pipeline (`Jenkinsfile`) over Freestyle Jobs?
> **Answer:** "A Declarative Pipeline represents **Pipeline as Code**. Storing the pipeline configuration in Git provides version control, audit trails, pull request reviews for infrastructure changes, and disaster recoverability. If the Jenkins server crashes, we can recreate the entire CI/CD workflow in seconds from our `Jenkinsfile`."

### Q2: What is the purpose of the Selenium Quality Gate in your pipeline?
> **Answer:** "Unit tests only verify isolated methods. The Selenium WebDriver suite executes **4 critical user journeys** (intake form submission, search filtering, status drill-down, and alerts routing) against a running headless browser. If any UI regression or broken workflow occurs, the pipeline fails, test failure screenshots are archived, and faulty code is **prevented from reaching production**."

### Q3: Why did you use a Multi-Stage Dockerfile?
> **Answer:** "A multi-stage build uses two phases:
> 1. **Builder Stage:** Uses full Maven and JDK to compile sources and package the JAR.
> 2. **Runtime Stage:** Discards Maven, compilers, and source code, copying only the final JAR into a minimal Alpine JRE image.
> This reduces our container image size drastically and eliminates security vulnerabilities by keeping build tools out of production."

### Q4: Why did you create a non-root user (`appuser`) inside the Docker container?
> **Answer:** "By default, Docker containers run as the `root` user. If an attacker discovers a vulnerability and escapes the container, they could gain root privileges on the host server. Running our application as an unprivileged user (`appuser`, UID 1001) follows the **Principle of Least Privilege** and defense-in-depth container security."

### Q5: What is Idempotency in Ansible and how did you demonstrate it?
> **Answer:** "Idempotency means that executing the same playbook multiple times produces the exact same desired state without unintended side effects. In **Run 1**, Ansible installed packages and created directories (`changed=5`). In **Run 2**, with zero manual changes, Ansible reported **`changed=0, failed=0`**, confirming zero configuration drift."

### Q6: Why did you choose Port 8081 for the application instead of Port 8080?
> **Answer:** "Jenkins runs by default on port `8080`. To avoid port contention and ensure clean separation of concerns between our CI/CD orchestration controller and our deployed application runtime, we dedicated port `8081` exclusively to the Loan Portal across Spring Boot, Docker, and Ansible."

### Q7: How does your system handle Disaster Recovery / Rollback?
> **Answer:** "Because every Jenkins build tags an immutable, versioned Docker image (`loan-portal:build-<N>` and `loan-portal:v1.0.0`), rollback is instantaneous. If a release fails health checks on port 8081, our rollback automation swaps the container back to `loan-portal:v1.0.0` in under 15 seconds."

### Q8: What database is used and how does it support CI/CD testing?
> **Answer:** "We utilized an embedded H2 relational database managed by Spring Data JPA. Because it runs in-memory or as a self-contained local store, it requires zero external database installation, boots instantly during headless Selenium tests, and ensures test isolation across clean CI pipeline runs."

### Q9: How are PII and customer data protected in this application?
> **Answer:** "Input validation is enforced on form submissions, credentials are never committed into version control, and our architecture plan in Phase 2 incorporates PAN regex validation, Aadhaar 8-digit masking (`XXXX-XXXX-1234`), and Argon2/BCrypt cryptographic password hashing."

### Q10: What is the role of Spring Boot Actuator in your DevOps lifecycle?
> **Answer:** "Spring Boot Actuator exposes operational health telemetry via `/actuator/health`. Our Jenkins pipeline and Ansible automation query this endpoint in Stage 7 as an automated smoke test before certifying a deployment as successful."
