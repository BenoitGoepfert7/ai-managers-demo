# AGENTS.md

Spring Boot 3.5 / Java 24 / Maven demo (package `fr.neosoft.aimanagersdemo`) with feature packages: `access`, `geoblocking`, `protection`.

## Commands

- Build + all tests: `mvn -q verify`
- Single test class: `mvn -q -Dtest=AccessControllerTest test`
- Run: `mvn spring-boot:run`
- Test one package (custom Maven profiles in `pom.xml`, each sets surefire's `test` property to that package):
  - `mvn -q -Ptest-access test`
  - `mvn -q -Ptest-geoblocking test`
  - `mvn -q -Ptest-protection test`
- A new feature package must get its own `test-<package>` profile.

## Project-specific conventions (unusual, mandatory)

- Method and constructor parameters are prefixed with `p_` (e.g. `p_subscriberId`, `p_clock`). Fields, locals and lambda parameters keep plain camelCase.
- Because of the prefix, Spring-bound parameters must name the binding explicitly: `@RequestParam("contentId") String p_contentId`, `@PathVariable("channel") String p_channel`.
- Apply this to production code and to any parameter you add; do not rename unrelated existing parameters.

## Java

- Package by feature, not by layer. Keep controller, service and repository of a feature together.
- Constructor injection only; fields `final`; no `@Autowired` on fields in production code.
- Prefer `record` for immutable data (DTOs, value objects).
- Return `Optional` rather than `null`; never return `null` collections.
- Keep controllers thin: HTTP mapping and validation only, logic lives in services.
- Inject `Clock` (see `ClockConfig`) instead of calling `Instant.now()` so time is testable.

## Spring Boot

- Validate inputs at the boundary with Bean Validation (`jakarta.validation`, `@NotBlank`, `@Valid`); invalid input returns `400`.
- Use `@ConfigurationProperties` + `@Validated` for configuration instead of scattered `@Value`.
- Never hardcode secrets; read them from environment/config.
- Do not log sensitive data (tokens, subscriber details).

## Maven

- Inherit from `spring-boot-starter-parent`; do not set versions for dependencies it manages.
- Use starters (`spring-boot-starter-*`); keep test dependencies in `test` scope.
- Add a dependency only when needed, and keep `pom.xml` changes minimal.
- Unit tests run with Surefire (`*Test`); if integration tests are added, run them with Failsafe (`*IT`) via `mvn verify`.

## Testing (priority)

Every behaviour change or bug fix ships with a test in the same change.

- Use JUnit 5 + AssertJ (+ Mockito from `spring-boot-starter-test`).
- Pick the narrowest test type:
  1. Plain unit tests for services/logic: no Spring context, build the object with `new` and mock collaborators.
  2. `@WebMvcTest(XController.class)` for HTTP mapping, status codes, validation and JSON; mock collaborators with `@MockitoBean` (or `@Import` the real light-weight beans, as existing tests do).
  3. `@SpringBootTest` only for a few end-to-end wiring checks; it is slow.
- Prefer `MockMvcTester` (AssertJ) for new web tests; keep the style of the file when editing existing tests.
- Test mirrors main: `src/test/java/<same package>/<Class>Test.java`.
- Name tests by behaviour (`deniesInactiveSubscription`), one behaviour per test, Arrange-Act-Assert.
- Cover: happy path, each error/denial branch, boundary and invalid inputs (blank, null, unknown id).
- Tests must be deterministic: no real network, no sleeping, no dependence on wall-clock (fix the `Clock`), no test-order dependence.
- Fake external systems (e.g. `GeoIpClient`) behind an interface and mock/stub them in tests.
- Use Testcontainers with `@ServiceConnection` for real infrastructure (DB, broker); never rely on a locally installed service.
- Do not weaken or delete a test to make it pass; fix the code or explain why the expectation changed.
- Run the smallest relevant test class first, then `mvn -q verify` before finishing.

## Workflow

- Small, focused changes; do not touch unrelated code.
- Follow the `git-workflow` skill for branches/commits and the `jira-ticket` skill for ticket handling.
- Delegate to the `security` and `tester` custom agents for reviews and test writing.
