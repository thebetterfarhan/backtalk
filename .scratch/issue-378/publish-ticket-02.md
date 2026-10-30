## Parent

Backtalk issue #378: Can Backtalk preserve the selected web reading control instead of switching to Actions?

## What to build

Manually validate the implemented preference on a real phone with a supported browser or embedded webview. Confirm that selected web reading controls remain active through ordinary content movement, explicit Actions selection still works, and the default-off path remains compatible.

## Acceptance criteria

- [ ] Validate enabled preservation for Headings and at least one other web reading control.
- [ ] Confirm ordinary content movement does not silently switch to Actions while preservation is enabled.
- [ ] Confirm explicit Actions selection still opens and operates normally.
- [ ] Confirm the default-off path retains existing behavior.
- [ ] Record device/browser or webview details and distinguish owner-heard TalkBack behavior from automated evidence.

## Blocked by

- #382: Issue 378: Preserve selected web reading control

## Validation boundary

This ticket is a manual accessibility gate. Source inspection, unit tests, and builds do not substitute for audible TalkBack, physical swipe, or browser/webview validation.
