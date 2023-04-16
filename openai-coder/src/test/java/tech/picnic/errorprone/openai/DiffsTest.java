package tech.picnic.errorprone.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class DiffsTest {
  private static Stream<Arguments> unifiedDiffTestCases() {
    return Stream.of(
        arguments("", "", "EmptyFile.java", ""),
        arguments("foo", "foo", "NoLineTerminatorFile.java", ""),
        arguments(
            "foo",
            "bar",
            "ModifiedNoLineTerminatorFile.java",
            """
            --- ModifiedNoLineTerminatorFile.java
            +++ ModifiedNoLineTerminatorFile.java
            @@ -1,1 +1,1 @@
            -foo
            +bar
            """
                .stripTrailing()),
        arguments(
            """
            Line 0
            Line 1
            Line 2
            Line 3
            """,
            """
            Line 1
            Line b
            Line 3
            Line 4
            """,
            "RemovalModificationAndAddition.java",
            """
            --- RemovalModificationAndAddition.java
            +++ RemovalModificationAndAddition.java
            @@ -1,5 +1,5 @@
            -Line 0
             Line 1
            -Line 2
            +Line b
             Line 3
            +Line 4
            \s"""));
  }

  @ParameterizedTest
  @MethodSource("unifiedDiffTestCases")
  void unifiedDiff(String before, String after, String path, String expected) {
    assertThat(Diffs.unifiedDiff(before, after, path)).isEqualTo(expected);
  }
}
