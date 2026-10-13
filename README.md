# Secure CI/CD Pipeline for a Loan Application Tracking Portal

A robust, enterprise-grade web application designed for financial institutions to track, manage, and audit loan applications in real-time, backed by a fully automated and secure DevOps delivery pipeline.

## 🚀 Key Features (MVP)
* **Application Intake:** Record loan applications with auto-generated reference IDs.
* **Searchable Dashboard:** Query loan records by Application ID or Name.
* **Summary Indicators:** Real-time metrics (Total, In Review, Approved, Rejected).
* **Status Drill-Down:** Detailed timeline of application lifecycle transitions.
* **Alerts & Exceptions:** Highlight rejected loans and exception conditions.
* **Automated Health Check:** Exposed `/actuator/health` endpoint for monitoring.

## 🛠️ Technology Stack
* **Language & Framework:** Java 17, Spring Boot 3.2
* **Build Tool:** Apache Maven
* **Database:** Embedded H2 Database
* **CI/CD Orchestration:** Jenkins (Pipeline as Code via `Jenkinsfile`)
* **Quality Gate:** JUnit 5 & Selenium WebDriver
* **Containerization:** Docker
* **Configuration Management:** Ansible

## 💻 Local Setup & Execution

### Prerequisites
* Java JDK 17+
* Apache Maven 3.8+
* Git

### Build & Run
```bash
# Clone the repository
git clone <YOUR-GITHUB-REPO-URL>
cd DevOps_Project

# Compile and package
mvn clean package

# Run the application
mvn spring-boot:run
```

Access the portal in your browser at: `http://localhost:8080`
### Status: Release-Ready Baseline v1.0.0 
