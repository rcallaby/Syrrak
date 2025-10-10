# Contributing — Syrrak

Thank you for contributing to **Syrrak**. This document describes the processes and standards we expect contributors to follow to make reviews fast and consistent.

## Table of contents

* [Getting started](#getting-started)
* [Branching model](#branching-model)
* [Commit messages](#commit-messages)
* [Coding standards & linters](#coding-standards--linters)
* [Build, test & run](#build-test--run)
* [Pull request process](#pull-request-process)
* [Issue workflow](#issue-workflow)
* [Code review checklist](#code-review-checklist)
* [Releases & changelog](#releases--changelog)
* [Security disclosures](#security-disclosures)
* [Licensing & acknowledgements](#licensing--acknowledgements)

---

# Getting started

1. Fork the repository and create a feature branch from `main`:

   ```bash
   git clone git@github.com:<your-username>/Syrrak.git
   cd Syrrak
   git checkout -b feat/short-description
   ```
2. Install required tools:

   * JDK 17+ (or project's specified JDK)
   * Gradle (wrapper included; prefer `./gradlew`)
   * Android SDK & emulator (if running instrumentation or integration tests)
3. Run a full build:

   ```bash
   ./gradlew clean assemble check
   ```
4. Run unit tests:

   ```bash
   ./gradlew test
   ```

---

# Branching model

* `main` — production-ready code, always green (CI passing).
* `develop` — optional (only if used in the repo); otherwise work off `main`.
* Feature branches: `feat/<short-description>`
* Fix branches: `fix/<short-description>`
* Hotfix branches for urgent fixes: `hotfix/<short-description>`

Keep branches focused and short-lived. Rebase interactively to keep history clean when appropriate.

---

# Commit messages

Follow **Conventional Commits** (concise, machine-parseable):

```
<type>(<scope>): <short summary>

optional longer description (wrap at ~72 chars)

BREAKING CHANGE: description of breaking change
```

Types we use: `feat`, `fix`, `chore`, `docs`, `style`, `refactor`, `perf`, `test`, `ci`.
Examples:

* `feat(sensor): add DNS-based anomaly detector`
* `fix(network): handle IPv6 address parsing in scanner`

Atomic commits — one logical change per commit. Tests and lint must pass locally before committing.

---

# Coding standards & linters

* Language: **Kotlin** (follow Kotlin style guide).
* Formatting: run `ktfmt` or the configured formatter:

  ```bash
  ./gradlew ktfmtFormat
  ```
* Static analysis: use `detekt` and fix warnings before opening PR:

  ```bash
  ./gradlew detekt
  ```
* Null-safety: prefer non-null types, use `?.` and `?:` defensively where appropriate.
* Avoid `!!`. If necessary, add a comment explaining why it is safe.
* Keep public APIs stable and document public classes/methods with KDoc.
* Keep methods short and single-responsibility; avoid large classes.

---

# Build, test & run

* Build: `./gradlew assemble`
* Run unit tests: `./gradlew test`
* Run instrumentation (if applicable): ensure emulator is running, then:

  ```bash
  ./gradlew connectedAndroidTest
  ```
* Sonar/Code coverage: if present, run the configured Gradle task. Ensure coverage does not drop without explicit reason.
* CI must pass for every PR before merging.

---

# Pull request process

1. Open an issue describing the problem/feature unless the change is trivial (typo or documentation).
2. Create a branch from `main` and push it to your fork.
3. Open a PR against `main` with:

   * Clear title following commit style, e.g. `feat(sensor): add TLS fingerprinting`
   * Link to issue(s) (e.g., `Fixes #123`)
   * Short description of changes and rationale
   * Notes on testing performed and steps to reproduce
4. PR checklist (must be satisfied before merge):

   * [ ] CI passes
   * [ ] Lints and static checks pass
   * [ ] Unit tests added/updated for new behavior
   * [ ] No console/debug prints left in production code
   * [ ] Documentation (KDoc and README) updated if public behavior changed
5. Address review comments via new commits or force-push (keep history tidy). Squash if requested.
6. Merge strategy: maintainers will squash-merge feature PRs into `main`. Maintain a clean git history.

---

# Issue workflow

* Use clear titles and reproduce steps for bugs.
* Label issues: `bug`, `enhancement`, `question`, `security`, `good first issue`, `help wanted`.
* For security-sensitive issues, use the [Security disclosures](#security-disclosures) process — do not open a public issue.

---

# Code review checklist

Reviewers should ensure:

* The change implements the described behavior and matches the issue.
* Tests cover positive and negative paths; new logic has unit tests.
* No sensitive data (credentials, keys) is added.
* Performance and memory usage reasonable for mobile (avoid allocations in tight loops; prefer coroutines instead of blocking threads).
* API surface is minimal and documented.
* Proper error handling and logging (no verbose logs in production).
* Backwards compatibility considered for public interfaces.

Approvals:

* At least one core maintainer approval required for non-trivial changes.
* Security fixes may require two maintainers to approve.

---

# Releases & changelog

* Versioning: semantic versioning `MAJOR.MINOR.PATCH`.
* Releases are tagged on `main` (e.g., `v1.2.0`) with a release note.
* Keep a `CHANGELOG.md` with notable changes per release. PRs that modify public behavior should include a changelog entry in the PR description; maintainers will curate.

---

# Security disclosures

If you discover a security vulnerability:

1. **Do not** open a public issue.
2. Email maintainers at: `security@<repository-domain-or-org>.com` (replace with actual contact) — include:

   * Affected versions
   * Steps to reproduce (PoC if possible)
   * Impact assessment and remediation suggestion
3. Maintainers will acknowledge within 48 hours and coordinate a fix and release. If you need to remain anonymous, indicate that in your message.

If no dedicated security email is set, open a private communication channel with maintainers (e.g., org owner) — do not publish details.

---

# Licensing & acknowledgements

* By contributing, you agree to license your contributions under the repository's license (see `LICENSE` file).
* Include attribution where required by third-party components.

---

If anything in this document is unclear or you need access (CI, repo settings, or keys), open an issue titled `infra: request access` or contact a repository maintainer directly.

