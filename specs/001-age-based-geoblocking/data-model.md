# Data Model

## Subscriber (existing record, extended)
| Field | Type | Notes |
|-------|------|-------|
| id | String | existing |
| name | String | existing |
| active | boolean | existing |
| channels | Set<String> | existing |
| dateOfBirth | LocalDate | NEW; may be null (unknown) |

Derived: `age = full years between dateOfBirth and today (Clock)`.

Seed data: s1 Alice 1990-05-12 (adult), s2 Bob 2007-03-01 (19 in Nov 2026, between 18 and 20), s3 Carol 2012-01-01 (minor).

## ContentCatalog
`contentId → category` (e.g. `late-night-movie-1 → LATE_NIGHT_MOVIE`). Existing items (`world-cup-final`, `news-live`) have no category.

## AgeRule
Key: (category, country ISO-2). Value: minimum age (int ≥ 0).
Initial rules: (LATE_NIGHT_MOVIE, US) = 21; (LATE_NIGHT_MOVIE, FR) = 18.

## Country availability (existing)
`allowedCountries` gains `late-night-movie-1 → {FR, US}` so the item is reachable in both countries.

## Decision flow
1. Resolve country from IP (failure → existing 502).
2. Country not allowed for content → `COUNTRY_NOT_ALLOWED`.
3. Category of content; rule for (category, country)? none → `ALLOWED`.
4. Subscriber missing or no date of birth → `AGE_UNKNOWN`.
5. age < minimum → `AGE_RESTRICTED`, else `ALLOWED`.
