# AGENTS.md

Spring Boot 3.5 / Java 24 / Maven demo, package `fr.neosoft.aimanagersdemo`, feature packages `access`, `geoblocking`, `protection`.

## Commands

- All tests: `mvn -q verify`
- One class: `mvn -q -Dtest=AccessControllerTest test`
- One package: `mvn -q -Ptest-<package> test` (a new feature package needs its own `test-<package>` profile in `pom.xml`)
- Run: `mvn spring-boot:run`

## Code

- Standard Java naming (camelCase, PascalCase, UPPER_SNAKE constants). No prefixes or suffixes such as `p_`, `m_`, `Impl`.
- Spring bindings name their parameter explicitly when it differs from the Java name; otherwise rely on `-parameters`.
- Methods are at most 20 lines; extract private methods or collaborators beyond that.
- Package by feature. Constructor injection, `final` fields, `record` for immutable data, `Optional` instead of `null`.
- Thin controllers; logic in services. Inject `Clock` instead of `Instant.now()`.
- Validate input at the boundary (`jakarta.validation`); invalid input returns `400`. No secrets in code, no sensitive data in logs.
- Maven: inherit `spring-boot-starter-parent`, use starters, keep `pom.xml` changes minimal.

## Tests

- JUnit 5 + AssertJ + Mockito; mirror the main package, name tests by behaviour, one behaviour per test.
- Narrowest type first: plain unit test, then `@WebMvcTest`, `@SpringBootTest` only sparingly.
- Deterministic: no network, no sleep, fixed `Clock`; fake external systems behind an interface.
- Cover happy path, each denial/error branch and boundaries. Never weaken a test to make it pass.

## Workflow

- Split work into sub-tasks; one dedicated commit per sub-task, containing its code and tests.
- Run `mvn -q verify` and commit only when all tests pass.
- Small focused changes; do not touch unrelated code. Follow the `git-workflow` and `jira-ticket` skills; delegate to the `security` and `tester` agents when relevant.
