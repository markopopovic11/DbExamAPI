# DbExamAPI code rules

The schema is `../docs/structure/schema-v2.dbml`, explained step by step in `../docs/structure/schema.md`.

## Layers

One call direction: controller → mapper → service → repository.

- A controller injects only its mapper.
- A mapper injects services only. It resolves request ids to entities with each service's `findByIdNotNull`, inline in every method. It has no private `toDto`.
- `toDto` and `toResponse` live only in `web/v1/extensions`, as static methods in `final` classes.
- A service injects **only its own repository**, never a second one. Other tables are reached through their services.
- Every service has `Optional<X> findById(String id)` and `X findByIdNotNull(String id)`, which throws `XDoesNotExistException`.

## Services

- Setters stay inline in `create` and `update`. No `applyFields`-style helper that copies a DTO onto an entity.
- Private methods go at the bottom of the class.
- Rules that read or write the database stay private methods in the owning service, for example a uniqueness pre-check through the service's own repository.

## Helpers

Logic that only computes from its arguments goes to `helpers/<domain>/` as a Spring `@Component` the service injects. Examples are a rule check, a conversion and a generated name. Never use a static util class for it.

- Name rule checks `XRuleHelper` with `requireX(...)` methods that throw the domain exception, for example `TaskRuleHelper.requireDataSourceFits`.
- A helper is stateless: one bean serves every request, so it keeps no per-call data in fields. Data that one call derives and several private methods share goes in a private record inside the helper.
- A helper may inject config or other helpers, never a repository or a service.
- When two services check the same rule, they share one helper method and one exception, for example `DatasetVersionRuleHelper.requireVersionOfTaskDataset`.
- Service tests pass a real `new XHelper()`, not a mock.

## Streams

- No `for` or `while` loops in services and helpers. Use streams and collectors (`map`, `filter`, `forEach`, `Collectors.groupingBy`, `partitioningBy`).
- Prefer method references (`Objects::nonNull`, `DatasetVersion::getId`, `this::deleteById`) to lambdas that only forward.
- When a nullable value falls back to a default, use `Optional.ofNullable(...).map(...).orElse(...)` instead of a chain of null checks. A single `x == null ? null : x.getId()` in a mapper or extension stays a ternary.

## Data

- Ids are 21-character NanoId strings. Times are `Instant`.
- The schema is owned by Flyway (`src/main/resources/db/migration`) and `ddl-auto` is `none`. An entity change needs a new migration. Never edit an applied one.
- Secrets live in the git-ignored `.env`: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`. Never copy their values into code, docs or PRs.
- Document files go through `FileStorageService`. The only implementation is Cloudflare R2.
