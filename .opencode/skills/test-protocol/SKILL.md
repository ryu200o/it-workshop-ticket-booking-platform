---
name: test-protocol
description: Testing workflow, test planning, and quality gates for Spring Boot Modulith projects
---

# Test Protocol - Tester Agent Workflow

## 1. Test Planning Phase

**Before writing any test, create a test strategy:**

```markdown
# TEST STRATEGY: [Feature/Module Name]

## Scope
- Which modules/features to test
- Which to exclude (with justification)

## Test Types
| Type | Covered? | Tool | Priority |
|------|----------|------|----------|
| Unit Tests | ✅ | JUnit 5 + Mockito | HIGH |
| Integration Tests | ✅ | Testcontainers | HIGH |
| Contract Tests | ❌ | Not needed | LOW |
| Performance Tests | ⚠️ | JMeter | MEDIUM |

## Risk-Based Testing
| Risk | Level | Test Approach |
|------|-------|---------------|
| Payment processing | CRITICAL | Full integration + error scenarios |
| Data validation | HIGH | All edge cases |
| UI rendering | LOW | Snapshot tests |

## Required Coverage
- Line Coverage: ≥ 80%
- Critical Paths: 100%
- Branch Coverage: ≥ 70%
```

## 2. Test Execution Workflow

```
┌─────────────────────────────────────────────────────────────────┐
│                        TEST EXECUTION FLOW                      │
├─────────────────────────────────────────────────────────────────┤
│  1. Unit Tests              → Fast (< 1s) → MUST PASS          │
│  2. Slice Tests             → Medium (< 10s) → MUST PASS       │
│  3. Integration Tests       → Slow (< 2min) → MUST PASS        │
│  4. Modulith Verification   → CI Stage → MUST PASS             │
│  5. Coverage Check          → CI Stage → ≥ 80% MINIMUM         │
└─────────────────────────────────────────────────────────────────┘
```

## 3. Failure Analysis

**When tests fail, follow this triage:**

| Failure Type | Immediate Action | Escalate To |
|--------------|------------------|-------------|
| **Unit test fail** | Check test logic first → code logic second | Dev Agent |
| **Integration fail** | Check DB/container → code → configuration | Dev Agent |
| **Modulith violation** | Review dependencies → fix module boundaries | Review Agent |
| **Coverage below 80%** | Add missing tests → review coverage report | Dev Agent |
| **Flaky test** | Document flakiness → investigate root cause | Debug Agent |

### Test Failure Report Format

```markdown
# TEST FAILURE REPORT

## Summary
- **Total Tests**: 150
- **Failed**: 3
- **Skipped**: 2

## Failed Tests

### 1. `OrderServiceTest.shouldProcessPayment()`
- **Module**: orders
- **Type**: Unit Test
- **Error**: `NullPointerException` at line 42
- **Root Cause**: [analysis]
- **Recommendation**: [fix suggestion]

### 2. `OrderRepositoryIT.shouldFindByCustomerId()`
- **Module**: orders
- **Type**: Integration Test
- **Error**: Connection refused
- **Root Cause**: Testcontainers not started
- **Recommendation**: Check Docker availability

## Quality Gate Status
- Unit Tests: ❌ 1 failure
- Integration Tests: ❌ 1 failure  
- Coverage: 78% (below 80%)
- **Overall Status**: ❌ BLOCKED
- **Action**: Route to Debug Agent for root cause analysis
```

## 4. Testing Mindset

**The Tester's Mantra:**

> "The implementation is wrong until proven otherwise"

**Key attitudes:**
- **Assume failure**: Try to break the code, don't confirm it works
- **Be systematic**: Follow the test plan, don't test randomly
- **Be thorough**: Edge cases, security, performance, rollback
- **Be objective**: Evidence, not opinions

## 5. Quality Gates (MUST PASS)

| Gate | Condition | Blocking? |
|------|-----------|-----------|
| All Unit Tests Pass | 100% pass rate | YES |
| All Integration Tests Pass | 100% pass rate | YES |
| Modulith Verification | No violations | YES |
| Code Coverage | ≥ 80% | YES |
| Test Coverage of Critical Path | 100% | YES |
| No Flaky Tests | 0 flaky tests | YES |
| Pipeline Pass | All checks pass | YES |

**Zero flaky tests rule**: Any test failing intermittently must be fixed or quarantined. Flaky tests waste time and erode trust.

## 6. When to Involve Other Agents

| Situation | Action |
|-----------|--------|
| **Test fails due to code bug** | Report to Dev Agent with reproduction steps |
| **Test fails due to integration issue** | Report to DevOps Agent |
| **Test fails but behavior is correct** | Report to Review Agent for spec clarification |
| **Test is flaky** | Report to Debug Agent for root cause analysis |
| **Test is missing** | Report to Dev Agent to write missing test |

## 7. Final Test Report

**After all tests pass, produce:**

```markdown
# FINAL TEST REPORT

## Overall Status: ✅ PASSED

## Test Execution Summary
| Type | Executed | Passed | Failed | Duration |
|------|----------|--------|--------|----------|
| Unit | 120 | 120 | 0 | 45s |
| Integration | 30 | 30 | 0 | 2m 15s |
| Modulith | 1 | 1 | 0 | 10s |

## Coverage Summary
- **Overall**: 84% ✅ (target: ≥80%)
- **Critical Path**: 100% ✅
- **Branch Coverage**: 72% ✅ (target: ≥70%)

## Quality Gate Status
- ✅ Unit Tests: ALL PASSED
- ✅ Integration Tests: ALL PASSED
- ✅ Modulith Verification: PASSED
- ✅ Coverage: ABOVE THRESHOLD
- ✅ No Flaky Tests Detected

## Risk Assessment
| Risk | Level | Testing Completed |
|------|-------|-------------------|
| Payment processing | HIGH | ✅ All scenarios tested |
| Data integrity | HIGH | ✅ Transaction tests passed |
| Security | MEDIUM | ✅ Authentication tested |

## Recommendation
✅ **APPROVED FOR CODE REVIEW**

Proceed to Review Agent for final quality gate.
```

## 8. Important Reminders

- ✅ **Always test the behavior, not the implementation**
- ✅ **Prefer deterministic over clever tests**
- ✅ **Tests are documentation** — write them to be read
- ✅ **Run tests locally before pushing** — don't wait for CI to break
- ✅ **Don't skip tests** — they're your safety net
- ✅ **Trust the process** — if tests pass, proceed; if they fail, stop and fix
