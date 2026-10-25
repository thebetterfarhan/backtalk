# Fork-only engine-hang test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a fork-only CI emulator test that proves PR #218 recovers a split utterance after a deliberately silent Android TTS engine.

**Architecture:** Keep PR #218 source-only. Add a small test-only Android TTS service and a fork-only workflow job that installs the existing test APK plus the fake engine on a disposable emulator. Use logcat markers and a watchdog to prove injection, hang detection, original-ID recovery, and a later speech request.

**Tech Stack:** Android `TextToSpeechService`, Gradle/Android SDK, GitHub Actions Ubuntu runner, adb, emulator.

**Spec:** `docs/superpowers/specs/2026-10-25-fork-only-engine-hang-test-design.md`

## Global Constraints

- Keep fake-engine code and workflow changes off upstream PR #218.
- Do not modify the connected phone, its accessibility services, its TTS engine, or its user data.
- Do not change project dependencies, signing, provider URLs, credentials, or existing upstream workflows.
- Do not publish the fake engine or replace the existing APK artifact.
- Fail unless the test proves the fake engine withheld the target request and BackTalk emitted recovery evidence.

## Review Focus

- The fake engine must be selected and bound before the test begins; otherwise a green test is invalid.
- The target utterance must be long enough to create `#chunkN` IDs; otherwise the hang path is not exercised.
- The fake engine must withhold only the designated request and allow the later request, proving recovery rather than a timeout.
- The workflow must collect logcat and emulator diagnostics on failure without leaking credentials or user data.
- The test must not enable or disable accessibility services on the physical phone.

### Task 1: Fake TTS engine

**Files:**
- Create: `fork-tests/fake-tts/settings.gradle`
- Create: `fork-tests/fake-tts/build.gradle`
- Create: `fork-tests/fake-tts/src/main/AndroidManifest.xml`
- Create: `fork-tests/fake-tts/src/main/java/.../SilentTtsService.java`

**Interfaces:**
- Consumes: Android `SynthesisRequest` and the utterance text/parameters.
- Produces: an installable test APK and log markers for `HANG_INJECTED`, `RECOVERY_REQUEST`, and service selection.

- [ ] Write the service and manifest with a deterministic one-request hang mode and normal synthesis for subsequent requests.
- [ ] Verify the APK can be assembled in the isolated fork test project; do not change the main BackTalk build.

### Task 2: Fork-only emulator workflow

**Files:**
- Create: `.github/workflows/engine-hang-test.yml` on the fork-only branch.
- Modify: `fork-tests/fake-tts` build files only as needed by Task 1.

**Interfaces:**
- Consumes: Task 1 APK, the existing fork test APK, and a disposable Android emulator.
- Produces: CI artifacts containing sanitized logcat and a pass/fail result.

- [ ] Provision an API-level emulator using runner-installed Android SDK tools and hardware acceleration when available.
- [ ] Install both APKs, select the fake TTS engine inside the emulator, and launch the fork test app.
- [ ] Run a bounded scripted interaction that triggers a split utterance, waits for `HANG_INJECTED`, then asserts BackTalk’s hang detection, original-ID completion, and `RECOVERY_REQUEST`.
- [ ] Upload diagnostics only on failure; ensure the job has no secrets or physical-device commands.

### Task 3: Evidence and fork isolation

**Files:**
- Modify: `README.md` or a fork-only test note only if needed to explain invocation.

- [ ] Inspect the final diff and prove no upstream PR source files, upstream workflows, test-package production settings, or credentials are included.
- [ ] Run YAML/static checks and the fake-engine unit/build check available without a local BackTalk build.
- [ ] Trigger fork CI only after the diff is reviewed; report emulator evidence separately from physical-phone evidence.

## Completion Gate

The work is complete only when the fork CI job shows the fake engine was selected, the hang was injected, recovery used the original utterance mapping, and a later request was processed. If emulator setup or the recovery marker cannot be made reliable, stop and report the exact missing evidence instead of weakening the assertion.
