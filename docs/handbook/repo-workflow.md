# Repository Workflow

## Branching Strategy

### Main Branches

- **`main`** - Production-ready code (protected)
- **`develop`** - Integration branch for ongoing development (protected)

### Feature Branches

Create feature branches from `develop`:

```bash
git checkout develop
git pull origin develop
git checkout -b feat/short-description
```

### Branch Naming Convention

Use these prefixes:

- `feat/` - New features (e.g., `feat/order-tracking`)
- `fix/` - Bug fixes (e.g., `fix/login-validation`)
- `docs/` - Documentation changes (e.g., `docs/api-guide`)
- `test/` - Test additions/changes (e.g., `test/order-service`)
- `refactor/` - Code refactoring (e.g., `refactor/carrier-module`)
- `chore/` - Build/config changes (e.g., `chore/update-dependencies`)

**Examples:**
```bash
feat/carrier-rate-comparison
fix/notification-delivery-bug
docs/architecture-diagrams
test/integration-tests
```

## Commit Guidelines

### Commit Message Format

```
<type>: <short description>

<optional detailed description>
```

### Best Practices

- **One logical change per commit** - Don't mix feature work with bug fixes
- **Present tense** - "add feature" not "added feature"
- **Be descriptive** - Explain what and why, not how
- **Reference issues** - Include issue number when applicable: `feat: add tracking (#42)`

## Pull Request Process

### 1. Create Pull Request

When your feature is ready:

```bash
git push origin feat/your-feature-name
```

Then create PR on GitHub targeting `develop` branch.

### 2. PR Requirements

Your PR must include:

- **Descriptive title** - Clear summary of changes
- **Linked issue** - Reference the GitHub issue (e.g., "Closes #42")
- **Description** - What changed and why
- **Testing evidence** - How you tested the changes

### 3. PR Review

- **Minimum 1 approval required** from team member
- **All CI checks must pass:**
  - Build successful
  - Tests passing
  - Linting checks passed
  - Code coverage 70%
