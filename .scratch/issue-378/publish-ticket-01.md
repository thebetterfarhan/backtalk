## Parent

Backtalk issue #378: Can Backtalk preserve the selected web reading control instead of switching to Actions?

## What to build

Add an off-by-default navigation preference that keeps an explicitly selected web reading control active while ordinary web content movement occurs. When enabled, automatic contextual Actions activation must not replace the selected Headings, Links, Controls, or other web reading control. Users must still be able to select Actions explicitly.

## Acceptance criteria

- [ ] The preference is available through the existing navigation/reading-control settings and is off by default.
- [ ] With preservation enabled, automatic Actions activation does not replace an explicitly selected web reading control during ordinary web navigation.
- [ ] With preservation disabled, existing automatic Actions behavior remains unchanged.
- [ ] Explicitly selecting Actions still works regardless of the preference.
- [ ] Non-web and unsupported contextual-setting transitions retain existing behavior.
- [ ] Localized preference title and description follow existing settings-resource conventions.
- [ ] Focused regression coverage verifies enabled, disabled, explicit-Actions, and non-web/unsupported paths.
- [ ] The focused checks and documented phone build gate pass.

## Proposed seam

Guard the contextual Actions activation decision at the existing selector/contextual-setting boundary. Do not change web traversal, gesture mappings, or the availability of explicit Actions.

## Out of scope

Changing reading-control ordering, text-selection semantics, browser behavior, or claiming physical TalkBack/browser validation from automated tests.

## Labels

This is an accessibility enhancement related to issue #378.
