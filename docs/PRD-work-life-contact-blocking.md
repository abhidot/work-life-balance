# PRD: Work-Life Contact Blocking (Android)

## Problem Statement

People on a blocklist (colleagues, clients, etc.) can still reach them by phone outside the hours or days when they want to be available. Manually silencing or ignoring calls is unreliable, and OS-wide Do Not Disturb does not enforce per-contact schedules. Users need automatic, contact-aware call blocking that respects configurable work hours globally and per person, without giving up control when they need to pause enforcement or exempt someone temporarily.

## Solution

An Android app that registers as a **Call Screening** handler and **rejects incoming voice calls** before they connect when the caller matches an **enabled** blocklist entry and the current time is **outside that entry’s effective allowed window**. Users configure a **global work schedule** (days of week + start/end time in device local timezone), maintain a **blocklist** via contacts picker and manual numbers, and optionally override **days** and/or **times** per contact using a **layered** model. A **master switch** pauses all blocking without deleting data; each list entry can be **enabled or disabled** independently. Blocked-call events are **stored on-device** for future use but **not shown** in the UI in v1.

## User Stories

1. As a user, I want to set global work days and hours, so that most blocklist contacts inherit a sensible default schedule.
2. As a user, I want to add someone from my contacts to the blocklist, so that I do not have to type their number.
3. As a user, I want to add a phone number manually, so that I can block people not saved in contacts.
4. As a user, I want every phone number on a selected contact to be blocklisted together, so that blocking “this person” is consistent.
5. As a user, I want calls from blocklisted contacts outside their allowed window to be rejected before I answer, so that the call does not connect.
6. As a user, I want calls from blocklisted contacts inside their allowed window to ring normally, so that I can take work calls during agreed hours.
7. As a user, I want calls from people not on my blocklist to always ring (when blocking is active), so that family, unknown callers, and services are not accidentally blocked.
8. As a user, I want to override only the days for a specific contact while keeping global times, so that I can allow someone only on certain weekdays.
9. As a user, I want to override only the times for a specific contact while keeping global days, so that someone can reach me during a narrower daily window.
10. As a user, I want to override both days and times for one contact, so that different people can have different boundaries.
11. As a user, I want per-contact settings to inherit global days or times when I do not customize them, so that setup stays simple.
12. As a user, I want to disable a blocklist entry without deleting it, so that I can temporarily stop blocking that person.
13. As a user, I want a master “blocking paused” switch, so that all calls get through during emergencies without reconfiguring the list.
14. As a user, I want clear onboarding to grant call screening and related permissions, so that the app can actually block calls.
15. As a user, I want the home screen to show whether blocking is active, paused, or setup incomplete, so that I trust the app state at a glance.
16. As a user, I want a calm, readable UI with a status card and clear work-hours and blocklist screens, so that configuration feels low-stress.
17. As a user, I want schedules to use my phone’s local timezone, so that rules match my current location when I travel.
18. As a user, I want all rules and lists stored only on my device, so that my boundaries are private and simple.
19. As a user, I want blocking to apply only to voice calls in v1, so that the first release focuses on reliable call rejection.
20. As a user, I want to be informed in settings that SMS blocking is not in v1, so that I do not expect SMS to be stopped.
21. As a user, I want the app to fail open when master blocking is off, so that I am never stranded without calls.
22. As a user, I want disabled blocklist entries to never be blocked even when master blocking is on, so that per-person control is predictable.
23. As a user, I want to edit global work hours after initial setup, so that my routine can change.
24. As a user, I want to remove someone from the blocklist, so that they are no longer subject to rules.
25. As a user, I want to see how many people are on my blocklist from the home screen, so that I have a quick summary.
26. As a user, I want contact names shown on the blocklist when available, so that the list is scannable.
27. As a user, I want normalized phone numbers stored for matching, so that screening works regardless of format dialed.
28. As a user, I want the app to run on Android 10 and above, so that Call Screening behavior is reasonably consistent.
29. As a user, I want blocking logic to run in the call screening path with minimal latency, so that calls are rejected promptly.
30. As a user, I want not to see a history of blocked calls in v1, so that the UI stays minimal.
31. As a developer, I want blocked-call attempts logged in the local database, so that we can add activity or notifications later without schema churn.
32. As a user, I want per-contact snooze in a future version, so that I can temporarily allow someone without deleting rules (out of scope v1).

## Implementation Decisions

### Platform and scope

- **Platform:** Android only for v1.
- **Min SDK:** API 29 (Android 10+).
- **Stack:** Kotlin, Jetpack Compose, Room, Material 3. Use Hilt or manual DI consistent with project conventions once scaffolded.
- **Channels:** Voice calls only in v1. SMS is out of scope; settings may note “SMS coming later.”

### Blocking semantics

- **Mechanism:** `CallScreeningService` (or equivalent) returns disallow/reject for matching incoming calls so they do not connect from the user’s perspective.
- **Policy:** Blocklist-only. Unknown and non-list callers are never blocked by this app.
- **When to block:** For each **enabled** blocklist entry, compute **effective allowed window** = layered combination of global and per-contact days/times. If current local date/time is **outside** that window, reject the call. If inside, allow (ring through).
- **Master switch:** When “blocking paused,” screening must not reject any call (fail-open).
- **Per-entry enable:** Disabled entries are never blocked regardless of schedule.

### Schedule model (layered)

- **Global default:** Active **days of week** (e.g. Mon–Fri) + **start time** + **end time** (device local).
- **Per blocklist entry:**
  - **Days:** `INHERIT_GLOBAL` | custom set of weekdays.
  - **Times:** `INHERIT_GLOBAL` | custom start/end (same-day window; no overnight span in v1 unless explicitly added later).
- **Evaluation:** Block if today ∉ effective days OR current time ∉ effective time range. Inherit global for each dimension not overridden.

### Blocklist data

- **Add flows:** Contact multi-picker + manual E.164/local number entry with validation.
- **Contact expansion:** All phone numbers on a contact are associated with one blocklist entry.
- **Matching:** Normalize numbers for comparison at screening time; store contact display name and contact id when from picker.

### Overrides and future snooze

- **v1:** Enable/disable per entry only. **Snooze** deferred to v1.1+.
- **Precedence (when snooze ships):** Disabled beats snooze; snooze only when enabled—document for future, not implemented v1.

### Persistence and privacy

- **Storage:** Room on-device only; no account, no cloud sync, no third-party analytics in v1.
- **Blocked-call events:** Insert row on each reject (timestamp, normalized number, optional contact id, which rule applied). **No UI** surfacing in v1.

### UI / UX

- **Visual direction:** Calm / wellness on Material 3—soft neutrals, clear copy, generous spacing.
- **Key screens:** Onboarding (permissions + screening role + initial work hours), Home (master switch + status + blocklist count), Work hours (global days + time), Blocklist (list + add), Contact detail (enable switch, layered day/time overrides).
- **Onboarding:** Do not request all permissions before value explanation; guide user to system call screening settings.

### Deep modules (build / test in isolation)

| Module | Responsibility | Suggested interface |
|--------|----------------|---------------------|
| **ScheduleEvaluator** | Given global schedule, per-entry overrides, and `ZonedDateTime`, return whether contact is in allowed window | `fun isAllowed(now, global, entryOverrides): Boolean` |
| **CallMatchResolver** | Given incoming number and blocklist entries, resolve matching entry (if any) | `fun resolve(incomingNumber, entries): BlocklistEntry?` |
| **BlockingPolicy** | Combine master switch, entry enabled flag, schedule result → allow or reject | `fun shouldBlock(call, appState, entry): Boolean` |
| **CallScreeningBridge** | Android service adapter: map `Call.Details` → policy → `CallResponse` | Thin; tests via policy module |
| **Repositories** | Room CRUD for global settings, blocklist, block events | Standard repository pattern |

Confirm with implementer which modules get unit tests first; **ScheduleEvaluator**, **CallMatchResolver**, and **BlockingPolicy** are the highest-value test targets.

### Screening flow (logical)

```
Incoming call
  → Master off? → allow
  → Match blocklist entry? → no match → allow
  → Entry disabled? → allow
  → ScheduleEvaluator says allowed? → allow
  → reject + persist BlockEvent
```

### Permissions and roles

- Request as needed through onboarding: phone state, call log (if required for screening on target API), read contacts for picker.
- User must set app as **call screening** / caller screening handler in system settings.

## Testing Decisions

- **Principle:** Test **external behavior** of pure modules via inputs/outputs; avoid asserting internal Room SQL or Compose implementation details in unit tests.
- **ScheduleEvaluator:** Table-driven tests for inherit/custom days/times, weekday boundaries, inside/outside window, master switch not needed here.
- **CallMatchResolver:** Normalization edge cases (country code, leading zero, multiple numbers per contact).
- **BlockingPolicy:** Matrix: master on/off × entry enabled × in/out of window × no match.
- **CallScreeningBridge:** Minimal instrumented or robolectric smoke if adopted; prefer keeping telephony glue thin.
- **UI:** Manual QA on physical device for screening role and real incoming call; optional Compose UI tests for work-hours form validation only if stable.
- **Prior art:** Greenfield repo—no existing test patterns; establish `src/test` examples alongside ScheduleEvaluator.

## Out of Scope

- iOS or web clients
- SMS blocking, silencing, or auto-reply
- Allowlist-only mode or VIP always-allow list
- Per-contact **snooze** (temporary allow)
- In-app blocked-call history or post-block notifications
- Cloud sync, accounts, multi-device
- Contact group import / “block work label”
- Share-from-call-log intent
- Fixed home timezone (device local only in v1)
- Global pause that revokes screening role in system settings
- Default SMS app / true SMS interception
- Per-number pick within one contact (block all numbers only)
- Analytics SDKs and sign-in

## Further Notes

- **App naming:** Placeholder options include Boundary, Off Hours, or Work-Life; finalize before store listing.
- **OEM variance:** Call log visibility for rejected calls may vary; in-app history was explicitly deferred.
- **Play policy:** Call screening apps must clearly disclose call handling; privacy policy required before release.
- **v1.1 candidates:** Per-contact snooze, blocked-call notification toggle, SMS notification suppression, optional activity UI reading existing BlockEvent table.
- **Issue tracker:** PRD written to repo; publish to GitHub/Linear when remote and `ready-for-agent` label workflow exist.
