# Contract: `GET /geo/check`

## Request
| Param | Required | Description |
|-------|----------|-------------|
| contentId | yes | Content item identifier |
| ip | yes | Client IP used for country lookup |
| subscriberId | no | Needed when an age rule applies; otherwise ignored |

## Responses
| Status | Body | Meaning |
|--------|------|---------|
| 200 | `{"allowed":true,"reason":"ALLOWED"}` | Country allowed and age rule satisfied or absent |
| 451 | `{"allowed":false,"reason":"COUNTRY_NOT_ALLOWED"}` | Content not available in the country (unchanged) |
| 403 | `{"allowed":false,"reason":"AGE_RESTRICTED"}` | Subscriber below the minimum age for category in that country |
| 403 | `{"allowed":false,"reason":"AGE_UNKNOWN"}` | Age rule applies but subscriber/date of birth unknown |
| 502 | text message | GeoIP lookup failed (unchanged) |

Country check runs first: a request failing both gets 451.

## Examples (seed data, today = 2026-11-03)
- `contentId=late-night-movie-1&ip=81.2.69.1&subscriberId=s2` (19 y/o, FR) → 200
- `contentId=late-night-movie-1&ip=8.8.8.8&subscriberId=s2` (19 y/o, US) → 403 AGE_RESTRICTED
- `contentId=late-night-movie-1&ip=8.8.8.8&subscriberId=s1` (adult, US) → 200
