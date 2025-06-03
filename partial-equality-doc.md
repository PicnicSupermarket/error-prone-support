## 🛡 `ImmutablesPartialEquality` BugChecker

This custom [Error Prone](https://errorprone.info/) `BugChecker` flags risky usages of:

```java
assertThat(actual).isEqualTo(expected);
```

When `actual` or `expected` is an **Immutable** type that may not follow standard equality semantics.

### 🚩 What is flagged?

Comparisons where:

* The argument type is annotated with `@Value.Immutable`, and either:

  * Overrides `equals(Object)`, or
  * Has one or more `@Value.Auxiliary` fields.

**Or**:

* The argument type is annotated with `@Immutable` and extends or implements such a risky `@Value.Immutable` type.

### ✅ What is **not** flagged?

* Calls that use `usingRecursiveComparison()`, e.g.:

  ```java
  assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  ```

### 🤔 Why does this matter?

`@Value.Immutable` types may **exclude fields from equality** (e.g., `@Value.Auxiliary`), which makes direct `isEqualTo(...)` comparisons misleading or incorrect in tests.

### ✔ Recommended alternatives

* Use `usingRecursiveComparison()`:

  ```java
  assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  ```
* Or compare individual fields
* Or use custom assertion helpers
