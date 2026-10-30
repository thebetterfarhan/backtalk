## Problem Statement

When a user explicitly selects a web reading control such as Headings or Links, ordinary movement through page content can cause Backtalk to automatically switch to Actions. Generic text-selection actions can make the Actions setting eligible even when the user did not request it, so the next directional gesture no longer uses the selected web control.

## Solution

Add an optional, off-by-default navigation preference that preserves the user's explicitly selected web reading control while navigating web content. When enabled, automatic contextual activation of Actions must not replace the selected web control. Users can still select Actions explicitly, and existing automatic behavior remains unchanged when the preference is disabled.

## User Stories

1. As a web-browsing Backtalk user, I want to preserve my selected Headings control while reading nearby content, so that the next directional gesture continues to move between headings.
2. As a web-browsing Backtalk user, I want the same preservation behavior for Links, Controls, and other web reading controls, so that I can navigate consistently with my chosen granularity.
3. As a Backtalk user who relies on automatic contextual Actions, I want the new behavior disabled by default, so that an upgrade does not change my navigation model unexpectedly.
4. As a user who explicitly chooses Actions, I want Actions to remain selectable and usable, so that preservation does not prevent intentional action navigation.
5. As a user moving across content with generic selection commands, I want those commands not to silently take over my selected web control when preservation is enabled, so that selectable text does not interrupt web navigation.
6. As a user navigating ordinary non-web content, I want existing contextual setting behavior preserved, so that the preference affects only the requested web-reading scenario.
7. As a localized Backtalk user, I want the preference title and description translated through the existing settings-resource conventions, so that the option is understandable in my language.

## Implementation Decisions

- Implement the behavior at the contextual Actions-setting activation seam, where Backtalk decides whether a newly focused node should automatically replace the current selector setting.
- Gate only automatic takeover; do not alter explicit selector choices or the availability of Actions in the Actions menu.
- Treat the preference as disabled by default and place it with navigation/reading-control settings using existing preference and boolean-resource conventions.
- Preserve the current contextual restoration behavior when the selected setting is no longer supported by the focused node.
- Keep the change scoped to phone navigation behavior unless existing shared settings architecture requires a corresponding Wear resource entry.

## Testing Decisions

- Test externally observable contextual-setting decisions rather than private implementation details.
- Cover preservation enabled with a selected web control and an eligible Actions node: the current setting remains selected.
- Cover preservation disabled: existing automatic Actions activation remains unchanged.
- Cover explicit Actions selection: Actions remains usable regardless of the preference.
- Cover a non-web or unsupported contextual-setting transition to ensure existing behavior is unchanged.
- Use the nearest existing selector/contextual-setting test seam; add a focused unit test only if no existing test harness covers this decision.
- Automated tests and builds do not prove audible TalkBack output or physical swipe behavior; live device/browser validation remains a separate gate.

## Out of Scope

- Redesigning reading-control ordering or gesture mappings.
- Changing the semantics of text selection or removing generic selection actions.
- Changing web traversal, browser behavior, or accessibility-node contents.
- Enabling the preference by default.
- Claiming device, browser, TalkBack speech, or Braille validation from source tests alone.

## Further Notes

- Issue #378 reports the behavior in Samsung Internet, Microsoft Edge, and embedded webviews; the supplied diagnostic evidence specifically confirms the heading-to-Actions transition in Edge.
- Source inspection shows Actions eligibility is based on the non-empty custom-action menu, including ancestor actions, so the exact generic-action mix should be confirmed by focused tests and device validation.
