# Agent Engineering & Release Quality Gate

This project must never be treated as complete merely because code was written or an APK was produced.

## Required workflow for every change

1. **Inspect before editing**
   - Read the relevant source files, callers, models, Gradle configuration, and existing tests.
   - Understand the current behavior and compatibility constraints.
   - Identify the expected behavior and possible regressions before changing code.

2. **Plan and implement narrowly**
   - Make the smallest coherent change.
   - Preserve existing features and user data.
   - Do not replace real integrations with mock or placeholder behavior while claiming the feature is complete.
   - Clearly label incomplete integrations and limitations.

3. **Test at multiple levels**
   - Add or update unit tests for business logic, parsers, state transitions, edge cases, and error handling.
   - Add UI/instrumentation tests for important user flows when feasible.
   - Run formatting, lint, static analysis, and the relevant Gradle test tasks.
   - Build the actual Android variants affected by the change (at minimum debug; release too when release configuration is touched).
   - Test migration, persistence, restart, rotation, permission-denied, offline, and boundary cases where applicable.

4. **Verify the result**
   - Inspect the complete CI run for the exact commit being delivered.
   - Read compiler, lint, test, and packaging logs; do not infer success from a workflow starting.
   - Confirm all required jobs passed and that the APK artifact exists and can be downloaded.
   - Review the diff for accidental changes, TODO placeholders, secrets, unsafe permissions, and regressions.
   - If any check fails, fix the cause and rerun the full relevant checks. Repeat until green.

5. **Release only after evidence**
   - Never publish, tag, merge, or call a build "ready" while required checks are failing, cancelled, skipped, or still running.
   - Do not hide failures or weaken tests just to get a green badge.
   - A successful compilation alone does not prove runtime correctness. Perform a device/emulator smoke test for critical flows when possible.
   - If a device test cannot be performed, explicitly say so and list what remains unverified.

## Required completion report

For every task, report:
- What changed and which files/modules were affected.
- Tests added or updated.
- Exact commands/workflows run.
- CI result and link for the exact commit.
- APK artifact link, if successfully produced.
- Device/emulator test result, or a clear statement that it was not performed.
- Known limitations, risks, and remaining work.

**Definition of done:** implementation + relevant automated tests + successful build + reviewed CI logs + artifact verification + honest disclosure of any unperformed runtime checks. Code written is not code verified.
