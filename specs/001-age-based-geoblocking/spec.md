# Feature Specification: Age-Based Geoblocking

**Feature Branch**: `001-age-based-geoblocking`

**Created**: 2026-11-03

**Status**: Draft

**Input**: User description: "geoblocking should handle age of the subscriber for each country depending on the rules defined. For instance, items of category 'late night movie' are restricted to 21 in the US and 18 in France."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Enforce country-specific minimum age (Priority: P1)

A subscriber requests playback of an item. In addition to the existing country availability check, the platform compares the subscriber's age with the minimum age that applies to the item's category in the country the subscriber is currently in. If the subscriber is too young for that country, playback is denied.

**Why this priority**: This is the core of the feature and a legal-compliance requirement; without it the rest has no value.

**Independent Test**: Request the same "late night movie" item as a 19-year-old from France (allowed) and from the US (denied).

**Acceptance Scenarios**:

1. **Given** a "late night movie" item with a minimum age of 18 in France and 21 in the US, **When** a 19-year-old subscriber requests it from France, **Then** access is granted.
2. **Given** the same item, **When** the same 19-year-old requests it from the US, **Then** access is denied with an age-restriction reason.
3. **Given** the same item, **When** a 21-year-old requests it from the US, **Then** access is granted.
4. **Given** the same item, **When** a 17-year-old requests it from France, **Then** access is denied with an age-restriction reason.

---

### User Story 2 - Define age rules per category and country (Priority: P2)

An operator maintains the set of rules stating, for each content category and country, the minimum subscriber age. Rules can be added or changed without altering the behaviour of unrelated categories or countries.

**Why this priority**: Rules must be configurable, but the initial rule set can be seeded to deliver P1.

**Independent Test**: Add a rule for a new category/country pair and verify that only matching requests are affected.

**Acceptance Scenarios**:

1. **Given** no age rule exists for a category in a country, **When** a subscriber of any age requests an item of that category from that country, **Then** no age restriction is applied (existing country availability rules still apply).
2. **Given** a rule is changed from 18 to 16 for a category and country, **When** a 17-year-old requests an item of that category from that country, **Then** access is granted.

---

### User Story 3 - Clear denial feedback (Priority: P3)

A denied subscriber and the calling system can distinguish an age-based denial from a country-based denial, so the right message is shown.

**Why this priority**: Improves user experience and supportability but is not required for enforcement.

**Independent Test**: Trigger both denial types and compare the responses.

**Acceptance Scenarios**:

1. **Given** an item available in the subscriber's country but age-restricted, **When** a too-young subscriber requests it, **Then** the denial reason identifies age as the cause.
2. **Given** an item not available in the subscriber's country, **When** any subscriber requests it, **Then** the denial reason identifies location as the cause.

---

### Edge Cases

- Subscriber's age is exactly the minimum age (e.g. turns 18 today in France): access is granted.
- Subscriber's date of birth is unknown or missing, and an age rule applies: access is denied (fail closed).
- Subscriber's birthday falls between requests: age is evaluated at request time.
- Item has no category, or the category has no rule for the country: no age restriction.
- Country cannot be determined from the IP: existing failure behaviour is kept; no age decision is made.
- An item belongs to a category with rules in some countries only: rules apply only in those countries.
- Subscriber is travelling: the rule of the country where the request originates applies, not the subscriber's home country.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST associate each content item with a category (e.g. "late night movie").
- **FR-002**: System MUST support defining a minimum subscriber age per category and per country.
- **FR-003**: System MUST determine the subscriber's age from their date of birth, evaluated at request time.
- **FR-004**: System MUST deny access when the subscriber's age is below the minimum for the item's category in the country of the request.
- **FR-005**: System MUST grant access (as far as age is concerned) when the subscriber's age is equal to or above the minimum.
- **FR-006**: System MUST apply no age restriction when no rule exists for the category/country pair.
- **FR-007**: System MUST deny access when an age rule applies and the subscriber's age is unknown.
- **FR-008**: System MUST keep existing country-availability checks; an item is accessible only if both the country and the age checks pass.
- **FR-009**: System MUST return a denial reason that distinguishes an age restriction from a country restriction.
- **FR-010**: System MUST ship with the initial rule: "late night movie" is restricted to 21 in the US and 18 in France.

### Key Entities *(include if feature involves data)*

- **Subscriber**: A user of the platform; has a date of birth from which age is derived.
- **Content Category**: A classification of items (e.g. "late night movie").
- **Content Item**: A playable item that belongs to one category.
- **Age Rule**: Minimum subscriber age for a given Content Category in a given country.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of requests from subscribers below the applicable minimum age are denied, in every country that has a rule.
- **SC-002**: 100% of requests from subscribers at or above the applicable minimum age are not blocked by age.
- **SC-003**: The same item and subscriber yield different outcomes in the US and France when the subscriber's age lies between 18 and 20.
- **SC-004**: A rule change takes effect for all subsequent requests with no change to unrelated categories or countries.
- **SC-005**: 100% of age-based denials are distinguishable from country-based denials.

## Assumptions

- Subscriber date of birth is available (or will be added) in the subscriber record; the current record has none.
- Country of the request is determined by the existing IP-based lookup.
- Ages are whole years computed from the date of birth; time zone nuances are out of scope.
- Content items currently have no category; assigning categories to existing items is part of this feature.
- Rules are not subject to change history/auditing in this version.
- Identity/age verification (proving the declared date of birth) is out of scope.
