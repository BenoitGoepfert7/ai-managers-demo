# Research: Age-Based Geoblocking

## Where is the age check placed?
- **Decision**: Inside `GeoBlockingSvc`, after the country check, behind `/geo/check`.
- **Rationale**: The feature is described as an evolution of geoblocking; the country is already resolved there.
- **Alternatives**: Put it in `AccessService` (rejected: it has no country knowledge); a new endpoint (rejected: callers would need two calls).

## Where does the date of birth come from?
- **Decision**: Add `LocalDate dateOfBirth` to `Subscriber`, seeded in `SubscriberRepo`.
- **Rationale**: Age must be computed at request time (spec FR-003); storing age would go stale. Subscribers are the only source of identity data.
- **Alternatives**: Pass age as a request parameter (rejected: spoofable); store age (rejected: stale).

## How is "now" obtained?
- **Decision**: Inject the existing `Clock` bean (`ClockConfig`) and use `Period.between(dob, LocalDate.now(clock))`.
- **Rationale**: Deterministic tests, whole-year ages, a birthday counts on the day itself (min age is inclusive).

## Where are content categories and rules stored?
- **Decision**: `ContentCatalog` (contentId → category) and `AgeRules` (category + country → min age) as in-memory maps in the `geoblocking` package.
- **Rationale**: Matches the existing in-memory style (`allowedCountries`, `SubscriberRepo`); no persistence in the project.
- **Alternatives**: Externalized `application.yml` properties (viable later; rejected now to avoid new configuration plumbing); database (no DB present).

## Missing data handling
- Unknown subscriber when an age rule applies → deny (fail closed, 404-style not needed; treated as age denial `AGE_UNKNOWN`). If no rule applies, `subscriberId` is not needed.
- No category or no rule for the country → no age restriction.
- `subscriberId` omitted: backward compatible; age is only evaluated when a rule applies, and then it is denied as `AGE_UNKNOWN`.

## Response shape and status codes
- **Decision**: Keep 451 for country denial; use 403 for age denial; body `{ "allowed": bool, "reason": "ALLOWED|COUNTRY_NOT_ALLOWED|AGE_RESTRICTED|AGE_UNKNOWN" }`.
- **Rationale**: Existing `allowed` key is kept so current consumers continue to work; 451 stays reserved for geography.
- **Note**: the body map type changes from `Map<String, Boolean>` to `Map<String, Object>`.
