# Quickstart: validating age-based geoblocking

## Prerequisites
Java 24, Maven. From the repo root.

## Automated
```bash
mvn -q test -Dtest='GeoBlockingControllerTest,AgeRulesTest'
```
Expected: all tests pass, including the US/FR scenarios for a 19-year-old and boundary ages 17/18/20/21.

## Manual
```bash
mvn spring-boot:run
curl -i "localhost:8080/geo/check?contentId=late-night-movie-1&ip=81.2.69.1&subscriberId=s2"   # 200
curl -i "localhost:8080/geo/check?contentId=late-night-movie-1&ip=8.8.8.8&subscriberId=s2"      # 403 AGE_RESTRICTED
curl -i "localhost:8080/geo/check?contentId=late-night-movie-1&ip=8.8.8.8&subscriberId=s1"      # 200
curl -i "localhost:8080/geo/check?contentId=world-cup-final&ip=8.8.8.8"                         # 451 COUNTRY_NOT_ALLOWED
curl -i "localhost:8080/geo/check?contentId=late-night-movie-1&ip=8.8.8.8"                      # 403 AGE_UNKNOWN
```
See [contract](contracts/geo-check.md) and [data model](data-model.md).
