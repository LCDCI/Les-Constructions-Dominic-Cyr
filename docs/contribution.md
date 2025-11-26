# Team Workflow & Conventions

These are guidelines to keep our team consistent and prevent merge conflicts.  

## 1. Branch Naming

Use the following pattern:

`type/CDC-JiraID-short_description`

Types:

- `feat/` – new feature  
- `bug/` – bug fix  
- `doc/` – documentation  
- `conf/` – config / setup work  

Example:

`feat/CDC-1-owner-dashboard`

## 2. Pull Request Naming

Use this exact format:

`feat(CDC-JiraID): short description`

Rules:

- Keep parentheses  
- Keep everything lowercase  
- No punctuation except the colon  
- Short and clear description  

Example:

`feat(CDC-21): implement login screen`

## 3. Commit Naming

Same structure as PR titles. GitHub will append the commit number automatically.  
Do not delete it.

Example:

`feat(CDC-21): add form validation (#123)`


## 4. Basic Workflow

Before starting any new ticket:

`git reset --hard origin/main`

Then create a new branch:

`git switch -c type/CDC-JiraID-short_description`


Commit and push:

`git add .`

`git commit -m "feat(CDC-JiraID): short description"`

`git push`


Open a PR into **main**, assign reviewers, follow naming rules.


## 5. PR Review Checklist

Every PR must satisfy:

- [ ] Branch + PR naming follow conventions  
- [ ] Only changes related to your Jira ticket  
- [ ] Code builds and runs locally  
- [ ] No console/server errors  
- [ ] No secrets or hardcoded credentials  
- [ ] No breaking changes unless approved  
- [ ] Basic testing done (manual or automated)


## 6. Merge Rules

- Minimum 1–2 approvals before merging  
- Resolve all merge conflicts before requesting review  
- Use **Squash and Merge**  
- PR title must follow conventions (see above)