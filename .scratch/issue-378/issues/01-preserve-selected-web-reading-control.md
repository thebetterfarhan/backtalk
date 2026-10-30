# 01: Preserve selected web reading control

**What to build:** An off-by-default navigation preference that keeps an explicitly selected web reading control active while ordinary content movement occurs, without preventing users from explicitly choosing Actions.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Add the localized preference using existing navigation/reading-control settings conventions.
- [ ] Prevent automatic contextual Actions takeover when the preference is enabled and a web reading control is selected.
- [ ] Preserve existing automatic takeover when the preference is disabled.
- [ ] Keep explicit Actions selection functional.
- [ ] Add focused regression coverage for enabled, disabled, explicit-Actions, and non-web/unsupported transitions.
- [ ] Run the focused checks and the documented phone build gate.
