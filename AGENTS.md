# Backtalk contributor guide

TalkBack-derived Android accessibility app with `phone` and `wear` flavors.
Accessibility behavior (focus, labels, speech) is core.

## Inner loop

Fast check after each edit:

```powershell
.\gradlew.bat :talkback:compilePhoneDebugKotlin
```

## Gate

Before a commit or PR, this must exit 0:

```powershell
.\gradlew.bat assemblePhoneDebug
```

Unit tests and lint use flavor-scoped task names
(`test<Flavor>DebugUnitTest`, `lint<Flavor>Debug`). Confirm the exact name with
`.\gradlew.bat :talkback:tasks` before adding one to the gate; the names have not
been verified here.

## Do not edit

Never modify outside an explicit user decision: `local.properties`, `*.jks`,
`*.keystore`, generated `braille/**/build` trees, and the two generated zips
(`braille/brltty/src/phone/res/raw/keytables.zip`,
`braille/translate/src/phone/res/raw/translationtables.zip`).

## Notes

- `ANDROID_SDK` and `JAVA_HOME` must be set. `build.sh` adds `sdk.dir` to
  `local.properties` on first run; keep other keys (for example
  `gemini.api.key`) out of commits.
- Treat `lint-baseline.xml` changes as deliberate: regenerate a baseline only to
  accept new findings on purpose.
- `gradlew` unit tests are not a substitute for live TalkBack validation on a
  device.
