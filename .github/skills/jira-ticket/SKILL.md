---
name: jira-ticket
description: Work on a Jira ticket of project DEMO - read it, plan, implement with tests, and transition its status. Use when the user mentions a ticket key like DEMO-3 or asks to pick up, start or close a ticket.
---

# Jira ticket workflow (project DEMO)

Uses the `jira-fake` MCP tools: `get_ticket`, `search_tickets`, `transition_ticket`.

1. **Read**: call `get_ticket` with the key (or `search_tickets` by text/status if no key). Summarise the summary, description and acceptance criteria in a few lines. If they are ambiguous, ask the user before coding.
2. **Start**: move the ticket to `In Progress` with `transition_ticket`.
3. **Branch**: follow the `git-workflow` skill; branch name contains the key (e.g. `feature/DEMO-3-short-title`).
4. **Implement**: make the smallest change satisfying each acceptance criterion, with tests (see `AGENTS.md`).
5. **Verify**: run `mvn -q verify`; all tests must pass.
6. **Finish**: commit with the key in the message (`feat(scope): DEMO-3 summary`), then move the ticket to `Done` only after the user confirms the work is complete.

Rules:

- Valid statuses are exactly `To Do`, `In Progress`, `Done`.
- Never transition a ticket to `Done` with failing tests.
- Always mention the ticket key in the final summary.
