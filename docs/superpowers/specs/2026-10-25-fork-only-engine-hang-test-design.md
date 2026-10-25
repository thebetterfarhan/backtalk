# Fork-only engine-hang test design

## Intent

Check PR #218's split-utterance hang recovery on Android without interrupting speech or changing accessibility settings on the user's phone. Keep all test infrastructure on the fork-only test branch; the upstream PR remains source-only.

## Goals and boundaries

- Run a disposable Android emulator in fork CI. Do not build locally or touch the connected phone.
- Use a test-only TTS engine that deliberately withholds a response for one split utterance, then permits subsequent speech.
- Assert a real hang-detection log, recovery of the original utterance's completion, and processing of a later speech request. Distinguish those assertions from audible real-phone evidence.
- Keep the existing fork test package, user data, upstream PR head, and upstream workflows unchanged.
- Fail clearly if the emulator, test engine, low-latency mode, or utterance splitting is not active; a timeout alone is not a pass.

## Approaches

1. **Fork CI emulator plus fake TTS engine (chosen).** Exercises Android TTS callbacks and Backtalk's failover path in isolation. Requires a small test-only Android app and a fork-only CI job; slower than a JVM test.
2. **Robolectric-only unit test.** Cheaper, but cannot establish that a silent Android TTS service triggers the full hang/failover path.
3. **Force-stop the phone's current TTS engine.** Direct device evidence, but risks leaving the user's screen reader silent; rejected.

## Proposed shape

Add a minimal fake `TextToSpeechService` in a fork-only test app. In a dedicated CI job, install the existing fork test APK and fake engine on an emulator, enable only the test accessibility service there, select the fake engine there, and present long text known to split into chunks. The fake engine withholds its callback for the designated request. The job records sanitized logcat and checks the hang trigger, original-ID completion, and a later request reaching the engine. Use an explicit watchdog and collect logs on failure. Do not publish the test app or alter the existing APK artifact.

## Acceptance and limits

The job passes only if it proves the failure injection occurred and Backtalk recovered enough to advance speech; ordinary startup or a green build is insufficient. A passing emulator job supplements CI evidence but does not become a claim that the controlled hang was tested on a physical phone. No test code, workflow, or design document from this branch is sent to upstream PR #218.
