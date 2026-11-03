---

description: "Task list for age-based geoblocking"
---

# Tasks: Age-Based Geoblocking

**Input**: Design documents from `/specs/001-age-based-geoblocking/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/geo-check.md

**Tests**: Included (plan specifies test-first with MockMvc and a fixed `Clock`).

## Format: `[ID] [P?] [Story] Description`

Source root: `src/main/java/fr/neosoft/aimanagersdemo/` (abbreviated `main/`); tests: `src/test/java/fr/neosoft/aimanagersdemo/` (abbreviated `test/`).

## Phase 1: Setup

- [x] T001 Run `mvn -q test` on the branch to record the green baseline before any change

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Data the age check depends on.

- [x] T002 Add nullable `LocalDate dateOfBirth` as last component of the `Subscriber` record in main/access/Subscriber.java
- [x] T003 Seed dates of birth in main/access/persistence/SubscriberRepo.java: s1 Alice 1990-05-12, s2 Bob 2007-03-01, s3 Carol 2012-01-01; fix any other `new Subscriber(...)` usage (including tests) so it compiles
- [x] T004 [P] Create `ContentCatalog` (`@Component`, in-memory `contentId -> category`; `late-night-movie-1 -> LATE_NIGHT_MOVIE`; `Optional<String> categoryOf(String)`) in main/geoblocking/ContentCatalog.java

**Checkpoint**: project compiles, existing tests still pass.

---

## Phase 3: User Story 1 - Enforce country-specific minimum age (Priority: P1) 🎯 MVP

**Goal**: Deny playback when the subscriber is under the minimum age for the category in the request's country.

**Independent Test**: `late-night-movie-1` as 19-year-old (s2): FR → 200, US → 403 `AGE_RESTRICTED`; adult s1 US → 200.

### Tests for User Story 1 ⚠️ (write first, must fail)

- [x] T005 [P] [US1] Unit test `AgeRulesTest` in test/geoblocking/AgeRulesTest.java: min age for (LATE_NIGHT_MOVIE, US)=21 and (LATE_NIGHT_MOVIE, FR)=18, empty for other pairs
- [x] T006 [P] [US1] Add MockMvc tests in test/geoblocking/GeoBlockingControllerTest.java: s2 FR → 200; s2 US → 403 AGE_RESTRICTED; s1 US → 200; boundary ages 17/18 (FR) and 20/21 (US) using a fixed `Clock` test bean; subscriber turning minimum age today → 200; existing tests still pass

### Implementation for User Story 1

- [x] T007 [P] [US1] Create `AgeRules` (`@Component`, in-memory `(category, country) -> minimum age` int ≥ 0; `OptionalInt minimumAge(String category, String country)`) in main/geoblocking/AgeRules.java
- [x] T008 [US1] In main/geoblocking/GeoBlockingSvc.java inject `ContentCatalog`, `AgeRules`, `SubscriberRepo`, `Clock`; add `late-night-movie-1 -> {FR, US}` to `allowedCountries`; replace `isAllowed` with a method returning a `Decision` enum (`ALLOWED, COUNTRY_NOT_ALLOWED, AGE_RESTRICTED, AGE_UNKNOWN`) following the flow in data-model.md; compute age with `Period.between(dob, LocalDate.now(clock)).getYears()`; minimum age inclusive
- [x] T009 [US1] In main/geoblocking/GeoBlockingController.java add optional `subscriberId` request param, map decisions to 200 / 451 / 403 per contracts/geo-check.md, body `Map<String, Object>` with `allowed` and `reason`; keep the 502 `IllegalArgumentException` handler
- [x] T010 [US1] Update `@Import` in test/geoblocking/GeoBlockingControllerTest.java to include the new beans (`ContentCatalog`, `AgeRules`, `SubscriberRepo`, fixed `Clock`)

**Checkpoint**: US1 works and is testable via `/geo/check`.

---

## Phase 4: User Story 2 - Rules per category and country (Priority: P2)

**Goal**: Absent rules mean no restriction; rules can be changed independently.

**Independent Test**: Items without category or without a rule for the country are not age-blocked; changing a rule value changes only that pair.

### Tests for User Story 2 ⚠️

- [x] T011 [P] [US2] Add tests: uncategorised item (`news-live`, US, s3 minor) → 200; categorised item in a country without rule → 200 (use a test-configured `AgeRules`); changed rule (18→16) lets a 17-year-old in, in test/geoblocking/GeoBlockingControllerTest.java and AgeRulesTest.java

### Implementation for User Story 2

- [x] T012 [US2] Make `AgeRules` constructible from a supplied rule map (default constructor seeds the initial rules) in main/geoblocking/AgeRules.java so rule sets are replaceable without touching other pairs
- [x] T013 [US2] Verify in main/geoblocking/GeoBlockingSvc.java that missing category / missing rule short-circuits to `ALLOWED` without requiring `subscriberId`

**Checkpoint**: US1 and US2 pass.

---

## Phase 5: User Story 3 - Clear denial feedback (Priority: P3)

**Goal**: Callers can tell age denials from country denials.

**Independent Test**: Country denial → 451 `COUNTRY_NOT_ALLOWED`; age denial → 403 `AGE_RESTRICTED`; unknown age → 403 `AGE_UNKNOWN`.

### Tests for User Story 3 ⚠️

- [x] T014 [P] [US3] Add tests in test/geoblocking/GeoBlockingControllerTest.java: `world-cup-final` from US → 451 + `reason=COUNTRY_NOT_ALLOWED`; late-night movie US without `subscriberId` → 403 `AGE_UNKNOWN`; unknown subscriber id → 403 `AGE_UNKNOWN`; subscriber with null dateOfBirth → 403 `AGE_UNKNOWN`; item failing both country and age → 451

### Implementation for User Story 3

- [x] T015 [US3] Ensure `reason` is always present in the response body and order of checks is country first, in main/geoblocking/GeoBlockingController.java and GeoBlockingSvc.java (adjust only if T014 fails)

**Checkpoint**: all stories pass.

---

## Phase 6: Polish

- [x] T016 [P] Update `README.md` with the `/geo/check` parameters and reasons (link to specs/001-age-based-geoblocking/contracts/geo-check.md)
- [x] T017 Run `mvn -q test` (full suite, including AccessControllerTest and protection tests) and confirm green
- [x] T018 Run the manual curl scenarios of specs/001-age-based-geoblocking/quickstart.md and confirm expected statuses

---

## Dependencies & Execution Order

- Phase 1 → Phase 2 → US1 → US2 → US3 → Polish. US2/US3 only extend tests and small adjustments on US1 code, so they are sequential in practice.
- Within US1: T005, T006, T007 in parallel; T008 depends on T004, T007, T003; T009 depends on T008; T010 depends on T009.
- T002 → T003 (record shape before seed data).

### Parallel Example

```text
T004 ContentCatalog   |  T005 AgeRulesTest  |  T006 controller tests  |  T007 AgeRules
```

## Implementation Strategy

- **MVP**: Phases 1–3 (US1) — the legal enforcement itself.
- Then add US2 (rule flexibility), US3 (reason clarity), and polish. Commit after each phase.
