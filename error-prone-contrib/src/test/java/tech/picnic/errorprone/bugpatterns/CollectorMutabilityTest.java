package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers.SECOND;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class CollectorMutabilityTest {
  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(CollectorMutability.class, getClass());
  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(CollectorMutability.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static java.util.stream.Collectors.toCollection;",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toMap;",
            "import static java.util.stream.Collectors.toSet;",
            "",
            "import java.util.ArrayList;",
            "import java.util.HashMap;",
            "import java.util.HashSet;",
            "import java.util.stream.Collectors;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(1).collect(Collectors.toList());",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(2).collect(toList());",
            "    Flux.just(3).collect(toImmutableList());",
            "    Flux.just(4).collect(toCollection(ArrayList::new));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(\"foo\").collect(Collectors.toMap(String::getBytes, String::length));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(\"bar\").collect(toMap(String::getBytes, String::length));",
            "    Flux.just(\"baz\").collect(toImmutableMap(String::getBytes, String::length));",
            "    // BUG: Diagnostic contains:",
            "    Flux.just(\"qux\").collect(toMap(String::getBytes, String::length, (a, b) -> a));",
            "    Flux.just(\"quux\").collect(toImmutableMap(String::getBytes, String::length, (a, b) -> a));",
            "    Flux.just(\"quuz\").collect(toMap(String::getBytes, String::length, (a, b) -> a, HashMap::new));",
            "",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(1).collect(Collectors.toSet());",
            "    // BUG: Diagnostic contains:",
            "    Stream.of(2).collect(toSet());",
            "    Stream.of(3).collect(toImmutableSet());",
            "    Stream.of(4).collect(toCollection(HashSet::new));",
            "",
            "    Flux.just(\"foo\").collect(Collectors.joining());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementFirstSuggestedFix() {
    refactoringTestHelper
        .addInputLines(
            "A.java",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toMap;",
            "import static java.util.stream.Collectors.toSet;",
            "",
            "import java.util.stream.Collectors;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(Collectors.toList());",
            "    Flux.just(2).collect(toList());",
            "",
            "    Stream.of(\"foo\").collect(Collectors.toMap(String::getBytes, String::length));",
            "    Stream.of(\"bar\").collect(toMap(String::getBytes, String::length));",
            "    Flux.just(\"baz\").collect(Collectors.toMap(String::getBytes, String::length, (a, b) -> b));",
            "    Flux.just(\"qux\").collect(toMap(String::getBytes, String::length, (a, b) -> b));",
            "",
            "    Stream.of(1).collect(Collectors.toSet());",
            "    Stream.of(2).collect(toSet());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toMap;",
            "import static java.util.stream.Collectors.toSet;",
            "",
            "import java.util.stream.Collectors;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(toImmutableList());",
            "    Flux.just(2).collect(toImmutableList());",
            "",
            "    Stream.of(\"foo\").collect(toImmutableMap(String::getBytes, String::length));",
            "    Stream.of(\"bar\").collect(toImmutableMap(String::getBytes, String::length));",
            "    Flux.just(\"baz\").collect(toImmutableMap(String::getBytes, String::length, (a, b) -> b));",
            "    Flux.just(\"qux\").collect(toImmutableMap(String::getBytes, String::length, (a, b) -> b));",
            "",
            "    Stream.of(1).collect(toImmutableSet());",
            "    Stream.of(2).collect(toImmutableSet());",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementSecondSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(SECOND)
        .addInputLines(
            "A.java",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toMap;",
            "import static java.util.stream.Collectors.toSet;",
            "",
            "import java.util.stream.Collectors;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(Collectors.toList());",
            "    Flux.just(2).collect(toList());",
            "",
            "    Stream.of(\"foo\").collect(Collectors.toMap(String::getBytes, String::length));",
            "    Stream.of(\"bar\").collect(toMap(String::getBytes, String::length));",
            "    Flux.just(\"baz\").collect(Collectors.toMap(String::getBytes, String::length, (a, b) -> b));",
            "    Flux.just(\"qux\").collect(toMap(String::getBytes, String::length, (a, b) -> b));",
            "",
            "    Stream.of(1).collect(Collectors.toSet());",
            "    Stream.of(2).collect(toSet());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static java.util.stream.Collectors.toCollection;",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toMap;",
            "import static java.util.stream.Collectors.toSet;",
            "",
            "import java.util.ArrayList;",
            "import java.util.HashMap;",
            "import java.util.HashSet;",
            "import java.util.stream.Collectors;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(toCollection(ArrayList::new));",
            "    Flux.just(2).collect(toCollection(ArrayList::new));",
            "",
            "    Stream.of(\"foo\")",
            "        .collect(",
            "            Collectors.toMap(",
            "                String::getBytes,",
            "                String::length,",
            "                (a, b) -> {",
            "                  throw new IllegalStateException();",
            "                },",
            "                HashMap::new));",
            "    Stream.of(\"bar\")",
            "        .collect(",
            "            toMap(",
            "                String::getBytes,",
            "                String::length,",
            "                (a, b) -> {",
            "                  throw new IllegalStateException();",
            "                },",
            "                HashMap::new));",
            "    Flux.just(\"baz\")",
            "        .collect(Collectors.toMap(String::getBytes, String::length, (a, b) -> b, HashMap::new));",
            "    Flux.just(\"qux\").collect(toMap(String::getBytes, String::length, (a, b) -> b, HashMap::new));",
            "",
            "    Stream.of(1).collect(toCollection(HashSet::new));",
            "    Stream.of(2).collect(toCollection(HashSet::new));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
