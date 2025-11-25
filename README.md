# Overview

Internal development repository for the LCDCI Web Application, built by team FOREJ.
This repo contains the frontend, backend, documentation, and CI/CD configuration for the project.

## 1. Project Setup
Clone the repository

`git clone <https://github.com/LCDCI Les-Constructions-Dominic-Cyr.git>`

`cd Les-Constructions-Dominic-Cyr`

Add the upstream remote

`git remote add upstream <https://github.com/LCDCI/Les-Constructions-Dominic-Cyr.git>`

Configure your Git identity

You must be logged into Git in your terminal:

`git config --global user.email "<you@example.com>"`

`git config --global user.name "Your Name"`

## 2. Development Workflow
Before starting any ticket

Reset your local main to match the remote:

`git reset --hard origin/main`

Create a new branch

Follow the team naming conventions:

`git switch -c type/CDC-JiraID-short_description`

Example:

`git switch -c feat/CDC-21-login-page`

## 3. Committing & Pushing
Ensure you are NOT on main before committing

Check your branch:

`git branch`

Stage your changes

`git add .`

Commit using required naming format

`git commit -m "feat(CDC-21): implement login form"`

Push your branch

`git push -u origin feat/CDC-21-login-page`
