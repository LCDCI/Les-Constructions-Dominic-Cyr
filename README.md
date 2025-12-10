# Les Constructions Dominic Cyr — Project Documentation

This repository contains the full source code for the **Les Constructions Dominic Cyr (LCDC) Web Application**, developed internally by **Team FOREJ**. It includes the React.js frontend, backend services, the dedicated **Go-based Files Service**, internal documentation, and CI/CD configuration.

This document provides all required information for onboarding, development setup, workflow standards, and integration of all project services.

# 1. Repository Overview

The project is built as a modern, containerized, multi-service application. It includes:

- React.js Frontend
- Backend APIs / Services
- Files Service (Go) — secure file upload + file hosting
- Docker Infrastructure
- CI/CD Pipelines
- Internal Developer Documentation

# 2. Repository Structure

```
/
├── frontend/
├── files-service/
├── backend/
├── docker-compose.yml
├── .env
└── README.md
```

# 3. Project Setup

Clone the repository:

```
git clone https://github.com/LCDCI/Les-Constructions-Dominic-Cyr.git
cd Les-Constructions-Dominic-Cyr
```

Configure Git identity:

```
git config --global user.email "you@example.com"
git config --global user.name "Your Name"
```

# 4. Development Workflow

Create a new branch:

```
git switch -c type/CDC-JiraID-short_description
```

Example:

```
git switch -c feat/CDC-21-login-page
```

# 5. Committing & Pushing

Check branch:

```
git branch
```

Stage:

```
git add .
```

Commit:

```
git commit -m "feat(CDC-21): implement login form"
```

Push:

```
git push -u origin feat/CDC-21-login-page
```

# 6. Docker

```
docker compose up --build
```


# 7. Contribution Standards

- Feature branches only
- PRs follow naming rules
- Reviews required

# 12. Summary

This repository contains all components required to develop, deploy, and maintain the LCDCI platform.
