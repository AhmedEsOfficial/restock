# Task: Set Up CI/CD Pipeline with GitHub Actions

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 6 hours (T-3.5 through T-3.9 combined)
**Priority:** P0
**Prerequisites:** 001_project_structure_setup.md

---

## Sprint Context

CI/CD ensures code quality is maintained consistently across the team. Every PR should trigger automated builds, lint checks, and tests. This prevents integration problems and catches issues early.

### Why This Matters
- Automated quality gates catch bugs before merge
- Consistent build environment across team
- Build artifacts for testing before merge
- Foundation for release automation later

---

## Task Description

Configure GitHub Actions workflows for:
1. PR validation (lint, test, build)
2. Artifact generation for review builds
3. Code coverage reporting

## GitHub Actions Workflows

### 1. Main CI Workflow (.github/workflows/ci.yml)

```yaml
name: CI

on:
  pull_request:
    branches: [main, develop]
  push:
    branches: [main, develop]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  lint:
    name: Lint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Run Detekt
        run: ./gradlew detekt

      - name: Run Ktlint
        run: ./gradlew ktlintCheck

      - name: Android Lint
        run: ./gradlew lint

      - name: Upload lint results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: lint-results
          path: |
            **/build/reports/lint-results*.html
            **/build/reports/detekt/*.html

  unit-tests:
    name: Unit Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: '**/build/test-results/**/*.xml'

      - name: Upload test reports
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-reports
          path: '**/build/reports/tests/**'

  build:
    name: Build
    runs-on: ubuntu-latest
    needs: [lint, unit-tests]
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Build debug APK
        run: ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7

  instrumented-tests:
    name: Instrumented Tests
    runs-on: ubuntu-latest
    needs: build
    # Only run on main branch or release PRs
    if: github.ref == 'refs/heads/main' || contains(github.head_ref, 'release/')
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Enable KVM
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm

      - name: Run instrumented tests
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          arch: x86_64
          profile: Nexus 6
          script: ./gradlew connectedDebugAndroidTest

      - name: Upload instrumented test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: instrumented-test-results
          path: '**/build/reports/androidTests/**'
```

### 2. PR Comment Workflow (.github/workflows/pr-comment.yml)

```yaml
name: PR Comment

on:
  workflow_run:
    workflows: [CI]
    types: [completed]

jobs:
  comment:
    name: Add PR Comment
    runs-on: ubuntu-latest
    if: github.event.workflow_run.event == 'pull_request'
    steps:
      - name: Download artifacts
        uses: actions/download-artifact@v4
        with:
          run-id: ${{ github.event.workflow_run.id }}
          github-token: ${{ secrets.GITHUB_TOKEN }}

      - name: Comment on PR
        uses: actions/github-script@v7
        with:
          script: |
            const fs = require('fs');

            // Get PR number from workflow run
            const prNumber = context.payload.workflow_run.pull_requests[0]?.number;
            if (!prNumber) return;

            const status = context.payload.workflow_run.conclusion;
            const statusEmoji = status === 'success' ? '✅' : '❌';

            const body = `## ${statusEmoji} CI Results

            **Status:** ${status}
            **Run:** [View Details](${context.payload.workflow_run.html_url})

            ### Artifacts
            - Debug APK: Available for download from the workflow run
            - Test Results: Available for download from the workflow run
            `;

            await github.rest.issues.createComment({
              owner: context.repo.owner,
              repo: context.repo.repo,
              issue_number: prNumber,
              body: body
            });
```

## Code Quality Configuration

### Detekt Configuration (config/detekt/detekt.yml)

```yaml
build:
  maxIssues: 0

style:
  MaxLineLength:
    maxLineLength: 120
  WildcardImport:
    active: true
  UnusedImports:
    active: true

complexity:
  LongMethod:
    threshold: 60
  TooManyFunctions:
    thresholdInFiles: 25
    thresholdInClasses: 20

naming:
  FunctionNaming:
    functionPattern: '[a-z][a-zA-Z0-9]*'
  VariableNaming:
    variablePattern: '[a-z][a-zA-Z0-9]*'

formatting:
  android: true
```

### Root build.gradle.kts Additions

```kotlin
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    detekt {
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }

    ktlint {
        android.set(true)
        outputColorName.set("RED")
    }
}
```

## PR Template (.github/PULL_REQUEST_TEMPLATE.md)

```markdown
## Description
<!-- Describe your changes -->

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Refactoring
- [ ] Documentation
- [ ] Test

## Checklist
- [ ] My code follows the project style guidelines
- [ ] I have performed a self-review
- [ ] I have added tests that prove my fix/feature works
- [ ] New and existing unit tests pass locally
- [ ] I have updated documentation (if applicable)

## Screenshots (if applicable)
<!-- Add screenshots for UI changes -->

## Testing Instructions
<!-- How to test this PR -->
```

## Branch Protection Rules

Configure in GitHub repository settings:

### Main Branch
- Require pull request reviews (1 approval)
- Require status checks to pass:
  - `lint`
  - `unit-tests`
  - `build`
- Require branches to be up to date
- Do not allow bypassing the above settings

### Develop Branch
- Require pull request reviews (1 approval)
- Require status checks to pass:
  - `lint`
  - `unit-tests`
- Require branches to be up to date

## Acceptance Criteria

- [ ] GitHub Actions workflow runs on every PR
- [ ] Lint checks pass before merge allowed
- [ ] Unit tests run and report results
- [ ] Build artifacts generated for review builds
- [ ] PR template in use
- [ ] Branch protection rules configured
- [ ] Detekt configured with custom rules
- [ ] Ktlint configured for formatting

## Definition of Done

- [ ] ci.yml workflow file created
- [ ] All workflow jobs pass on clean codebase
- [ ] Branch protection rules active
- [ ] PR template created
- [ ] Code quality configs committed
- [ ] Team notified of new workflow requirements

## Dependent Tasks

This task enables:
- Confident merging of all future PRs
- Quality assurance for Sprint 3-4+

## Notes

- Keep CI fast (< 10 minutes for basic checks)
- Instrumented tests only on main/release branches (expensive)
- Archive artifacts for 7 days to save storage
- Use concurrency groups to cancel duplicate runs
- Consider adding code coverage reporting later
