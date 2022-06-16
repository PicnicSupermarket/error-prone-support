package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.FixChoosers;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

public final class CollectorMutabilityCheckTest {

  private final CompilationTestHelper compilationTestHelper =
      CompilationTestHelper.newInstance(CollectorMutabilityCheck.class, getClass());

  private final BugCheckerRefactoringTestHelper refactoringTestHelper =
      BugCheckerRefactoringTestHelper.newInstance(CollectorMutabilityCheck.class, getClass());

  @Test
  void identification() {
    compilationTestHelper
        .addSourceLines(
            "A.java",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toSet;",
            "import static java.util.stream.Collectors.toMap;",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import reactor.core.publisher.Flux;",
            "import java.util.stream.Stream;",
            "import java.util.HashMap;",
            "",
            "class A {",
            "  void m() {",
            "    // BUG: Diagnostic contains: emphasize (im)mutability",
            "    Flux.just(1).collect(toList());",
            "    // BUG: Diagnostic contains: emphasize (im)mutability",
            "    Flux.just(\"foo\").collect(toMap(String::getBytes, String::length));",
            "    Flux.just(\"foo\").collect(toImmutableMap(String::getBytes, String::length));",
            "    Flux.just(\"foo\").collect(toMap(String::getBytes, String::length, (a, b) -> a, HashMap::new));",
            "    Flux.just(1).collect(toImmutableList());",
            "    // BUG: Diagnostic contains: emphasize (im)mutability",
            "    Flux.just(1).collect(toSet());",
            "    Flux.just(1).collect(toImmutableSet());",
            "    // BUG: Diagnostic contains: emphasize (im)mutability",
            "    Stream.of(1).collect(toList());",
            "    Stream.of(1).collect(toImmutableList());",
            "    // BUG: Diagnostic contains: emphasize (im)mutability",
            "    Stream.of(\"foo\").collect(toMap(String::getBytes, String::length));",
            "    Stream.of(\"foo\").collect(toImmutableMap(String::getBytes, String::length));",
            "    Stream.of(\"foo\").collect(toMap(String::getBytes, String::length, (a, b) -> a, HashMap::new));",
            "    // BUG: Diagnostic contains: emphasize (im)mutability",
            "    Stream.of(1).collect(toSet());",
            "    Stream.of(1).collect(toImmutableSet());",
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
            "import static java.util.stream.Collectors.toSet;",
            "import static java.util.stream.Collectors.toMap;",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import reactor.core.publisher.Flux;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(toList());",
            "    Flux.just(\"foo\").collect(toMap(String::getBytes, String::length));",
            "    Flux.just(1).collect(toSet());",
            "    Stream.of(1).collect(toList());",
            "    Stream.of(\"foo\").collect(toMap(String::getBytes, String::length));",
            "    Stream.of(1).collect(toSet());",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toSet;",
            "import static java.util.stream.Collectors.toMap;",
            "import static com.google.common.collect.ImmutableList.toImmutableList;",
            "import static com.google.common.collect.ImmutableSet.toImmutableSet;",
            "import static com.google.common.collect.ImmutableMap.toImmutableMap;",
            "import reactor.core.publisher.Flux;",
            "import java.util.stream.Stream;",
            "",
            "class A {",
            "  void m() {",
            "    Flux.just(1).collect(toImmutableList());",
            "    Flux.just(\"foo\").collect(toImmutableMap(String::getBytes, String::length));",
            "    Flux.just(1).collect(toImmutableSet());",
            "    Stream.of(1).collect(toImmutableList());",
            "    Stream.of(\"foo\").collect(toImmutableMap(String::getBytes, String::length));",
            "    Stream.of(1).collect(toImmutableSet());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacementSecondSuggestedFix() {
    refactoringTestHelper
        .setFixChooser(FixChoosers.SECOND)
        .addInputLines(
            "A.java",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toMap;",
            "import static java.util.stream.Collectors.toSet;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "class A {",
            " void m() {",
            "   Flux.just(1).collect(toList());",
            "   Flux.just(\"foo\").collect(toMap(String::getBytes, String::length));",
            "   Flux.just(1).collect(toSet());",
            "   Stream.of(1).collect(toList());",
            "   Stream.of(\"foo\").collect(toMap(String::getBytes, String::length));",
            "   Stream.of(1).collect(toSet());",
            " }",
            "}")
        .addOutputLines(
            "A.java",
            "import static java.util.stream.Collectors.toCollection;",
            "import static java.util.stream.Collectors.toList;",
            "import static java.util.stream.Collectors.toMap;",
            "import static java.util.stream.Collectors.toSet;",
            "import java.util.ArrayList;",
            "import java.util.HashMap;",
            "import java.util.HashSet;",
            "import java.util.stream.Stream;",
            "import reactor.core.publisher.Flux;",
            "class A {",
            " void m() {",
            "   Flux.just(1).collect(toCollection(ArrayList::new));",
            "   Flux.just(\"foo\").collect(toMap(String::getBytes, String::length, (a, b) -> a, HashMap::new));",
            "   Flux.just(1).collect(toCollection(HashSet::new));",
            "   Stream.of(1).collect(toCollection(ArrayList::new));",
            "   Stream.of(\"foo\").collect(toMap(String::getBytes, String::length, (a, b) -> a, HashMap::new));",
            "   Stream.of(1).collect(toCollection(HashSet::new));",
            " }",
            "}")
        .doTest();
  }
}
