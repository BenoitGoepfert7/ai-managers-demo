# Implementation Plan: Age-Based Geoblocking

**Branch**: `001-age-based-geoblocking` | **Date**: 2026-11-03 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-age-based-geoblocking/spec.md`

## Summary

Extend the existing geoblocking check so that, after the country-availability test, the subscriber's age is compared with the minimum age defined for the item's category in the country resolved from the IP. Age is derived from a new `dateOfBirth` on `Subscriber` using the existing `Clock` bean. Content gets a category, and age rules (category × country → minimum age) live in an in-memory rule table in the geoblocking package, consistent with the project's in-memory demo style. `/geo/check` gains a `subscriberId` parameter and a `reason` field distinguishing country from age denials.

## Technical Context

**Language/Version**: Java 24

**Primary Dependencies**: Spring Boot 3.5.6 (`spring-boot-starter-web`)

**Storage**: In-memory maps (as `SubscriberRepo` and `GeoBlockingSvc` today); no database

**Testing**: JUnit 5, Spring Boot Test (`@WebMvcTest`, MockMvc), fixed `Clock` for age tests

**Target Platform**: JVM service (Linux server)

**Project Type**: web-service (single Maven module)

**Performance Goals**: In-memory lookups; no measurable overhead on `/geo/check`

**Constraints**: Fail closed on unknown age when a rule applies; keep existing country behaviour and status codes (451, 502) unchanged

**Scale/Scope**: Demo scale: 3 subscribers, a handful of content items, 2 age rules

## Constitution Check

The constitution (`.specify/memory/constitution.md`) is still the unfilled template, so there are no gates. **Result: PASS (no constraints defined).** Re-checked after Phase 1: unchanged. Self-imposed guidelines: follow existing package layout and naming; test-first with MockMvc tests.

## Project Structure

### Documentation (this feature)

```text
specs/001-age-based-geoblocking/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── geo-check.md
└── tasks.md             # created by /speckit-tasks
```

### Source Code (repository root)

```text
src/main/java/fr/neosoft/aimanagersdemo/
├── access/
│   ├── Subscriber.java                  # + LocalDate dateOfBirth
│   └── persistence/SubscriberRepo.java  # seed dates of birth
└── geoblocking/
    ├── GeoBlockingSvc.java              # country check + age check, returns a Decision
    ├── GeoBlockingController.java       # + subscriberId param, reason in body
    ├── ContentCatalog.java              # NEW: contentId -> category
    └── AgeRules.java                    # NEW: (category, country) -> minimum age

src/test/java/fr/neosoft/aimanagersdemo/
├── geoblocking/
│   ├── GeoBlockingControllerTest.java   # updated + age scenarios
│   └── AgeRulesTest.java                # NEW
└── access/AccessControllerTest.java     # only if it builds Subscriber directly
```

**Structure Decision**: Single Maven module; new classes go in the existing `geoblocking` package, the date of birth goes on the existing `Subscriber` record.

## Complexity Tracking

No constitution violations.
