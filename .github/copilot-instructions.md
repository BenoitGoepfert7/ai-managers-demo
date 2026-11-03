# Copilot instructions

Project: Spring Boot 3.5, Java 24, Maven. Feature packages under `fr.neosoft.aimanagersdemo` (`access`, `geoblocking`, `protection`). See `AGENTS.md` for the full rules.

- Use constructor injection, `record`s for immutable data, thin controllers, logic in services.
- Validate request input with Bean Validation; invalid input returns 400.
- Every code change needs a test in the same change: plain unit tests for services, `@WebMvcTest` for controllers, `@SpringBootTest` sparingly.
- Tests use JUnit 5, AssertJ, Mockito; they must be deterministic (inject `Clock`, no real network).
- Run `mvn -q -Dtest=<Class> test` for quick feedback, `mvn -q -Ptest-<package> test` (access, geoblocking, protection) for one package, and `mvn -q verify` before finishing.
- Method/constructor parameters are prefixed `p_` (e.g. `p_subscriberId`); Spring bindings need explicit names: `@RequestParam("ip") String p_ip`.
- Never commit secrets or log tokens/personal data.
- Keep changes minimal and focused; do not refactor unrelated code.
- Commit messages: Conventional Commits, prefixed by the Jira key when known (e.g. `feat(access): DEMO-12 validate channel`).
