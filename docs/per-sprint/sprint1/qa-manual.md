# Quality Assurance Manual

**Authors:**  
- João Roldão - 113920  
- Hugo Castro - 113889  
- Hugo Sousa - 112733  
- Henrique Teixeira - 114588  
- João Varela - 113780  

---

## Table of Contents
1. [Project Management](#1-project-management)  
   1.1 [Assigned Roles](#11-assigned-roles)  
   1.2 [Backlog Grooming and Monitoring](#12-backlog-grooming-and-monitoring)  
2. [Code Quality Management](#2-code-quality-management)  
   2.1 [Coding Standards & Practices](#21-coding-standards--practices)  
   2.2 [Code Review Policy](#22-code-review-policy)  
3. [Continuous Delivery Pipeline (CI/CD)](#3-continuous-delivery-pipeline-cicd)  
   3.1 [Development Workflow](#31-development-workflow)  
   3.2 [Continuous Integration (CI)](#32-continuous-integration-ci)  
   3.3 [Observability and Monitoring](#33-observability-and-monitoring)  
4. [Software Testing](#4-software-testing)  
   4.1 [Overall Testing Strategy](#41-overall-testing-strategy)  
   4.2 [Functional Testing](#42-functional-testing)  
   4.3 [Developer-Facing Tests](#43-developer-facing-tests)  
   4.4 [Exploratory Testing](#44-exploratory-testing)  
   4.5 [Non-Functional Testing](#45-non-functional-testing)  

---

## 1. Project Management

### 1.1 Assigned Roles

| Role            | Name             |
|-----------------|------------------|
| Scrum Master    | João Roldão      |
| Product Owner   | Hugo Castro      |
| QA Engineer     | Hugo Sousa       |
| Service Analyst | Henrique Teixeira|
| DevOps Master   | João Varela      |

### 1.2 Backlog Grooming and Monitoring
- **Backlog management:** GitHub Projects and Issues are used.  
- **Work organization:**
  - Issues represent tasks, bugfixes, or features.
  - Larger features can be grouped into epics.  
- **Backlog refinement:** at least weekly, at the start of each sprint, led by the Product Owner with team input.  
- **Progress tracking:**  
  - Kanban board (To Do, In Progress, Review, Done).  
  - Labels and milestones for categorization and deadlines.  
- **Traceability:** every PR must be linked to its GitHub issue.  
- **Review cadence:** progress reviewed in weekly meetings and retrospectives.  

---

## 2. Code Quality Management

### 2.1 Coding Standards & Practices
- **Style guides:**  
  - Java → Google Java Style  
  - TypeScript/JS → ESLint + Prettier  
- **Static analysis:** linting and formatting mandatory before pushing.  
- **Naming & structure:** clear names, small functions with single responsibility.  
- **Documentation:** Javadoc/TSdoc for public APIs; module-level README files.  

### 2.2 Code Review Policy
- **Branch protection:** no direct commits to `develop` or `master`.  
- **Pull Requests:** required for all changes.  
- **Minimum approval:** at least 1 reviewer (not the author).  
- **Mandatory checks before merge:**  
  - CI build  
  - Linting/formatting  
  - Unit/integration tests  
- **PR requirements:** descriptive title, linked issue, test evidence.  
- **Review checklist:** correctness, readability, performance, security, tests, docs.  
- **PR size:** prefer small, focused PRs.  
- **Author’s responsibility:** respond to feedback, update PR, squash/rebase as needed.  

---

## 3. Continuous Delivery Pipeline (CI/CD)

### 3.1 Development Workflow
- **Branching model:**  
  - `develop` → integration  
  - `master` → production  
- **Feature branches:** `feature/<short-description>`.  
- **Definition of Done:**  
  - Code implemented and pushed.  
  - Linked issue moved to “In Review” or “Done”.  
  - Reviewed and approved.  
  - CI pipeline passed.  
  - Documentation updated.  

### 3.2 Continuous Integration (CI)
- **Tooling:** GitHub Actions.  
- **Triggers:**  
  - PRs to `develop` or `master`.  
  - Pushes to feature branches.  
- **Checks:** build, dependencies, lint, unit/integration tests.  
- **Outcome:** PRs cannot be merged if any check fails.  

### 3.3 Observability and Monitoring
- **Tooling:** GitHub Actions + Docker.  
- **Deployment process:**  
  - On merge to `master`, workflow builds Docker image → pushes to registry → deploys automatically.  
- **Benefits:**  
  - Minimal manual intervention.  
  - Fast, consistent deployments.  
  - Immediate availability of fixes and features.  

---

## 4. Software Testing

### 4.1 Overall Testing Strategy
- **Principles:**  
  - Test early, test often.  
  - Automation first.  
  - Shift-left (developers write tests).  
- **CI Integration:** successful tests required for merge.  
- **Tools:**  
  - JUnit 5  
  - Mockito  
  - Cucumber  
  - Selenium  
  - Grafana  

### 4.2 Functional Testing
- **Black-box:** validates system behavior against requirements.  
- **Scope:** all major features and workflows.  
- **Implementation:**  
  - Written in Cucumber for readability.  
  - Derived from acceptance criteria.  
- **Regression suite:** runs automatically in CI.  

### 4.3 Developer-Facing Tests
- **Unit tests:**  
  - Required for business-critical logic.  
  - JUnit 5 + Mockito.  
- **Integration tests:**  
  - For multi-layer or external API interactions.  
  - JUnit 5 + TestContainers.  
- **Execution:** always run in CI.  

### 4.4 Exploratory Testing
- **Goal:** find issues outside scripted tests.  
- **When:** during sprint reviews or before releases.  
- **Focus:** new flows, complex inputs, regression areas.  
- **Outcome:** findings logged as GitHub issues.  

### 4.5 Non-Functional Testing
- **Purpose:** ensure performance, availability, reliability.  
- **SLOs:**  
  - API requests < 3s under normal load.  
  - Order registration < 2s.  
- **Performance testing:**  
  - Load/stress tests with **K6**.  
  - Results in Grafana dashboards.  
- **Execution:** before releases or scheduled cycles, not every CI run.  

---

**Course:** 41492 - Engenharia de Software (ES)
