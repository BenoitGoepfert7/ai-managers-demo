# ai-managers-demo
AI managers demo

## Geoblocking

`GET /geo/check?contentId=…&ip=…[&subscriberId=…]`

- `200` allowed, `451` `COUNTRY_NOT_ALLOWED`, `403` `AGE_RESTRICTED` / `AGE_UNKNOWN`, `502` GeoIP failure.
- Age rules are per category and country (e.g. `LATE_NIGHT_MOVIE`: 21 in the US, 18 in France); `subscriberId` is required when a rule applies.

See `specs/001-age-based-geoblocking/contracts/geo-check.md`.
