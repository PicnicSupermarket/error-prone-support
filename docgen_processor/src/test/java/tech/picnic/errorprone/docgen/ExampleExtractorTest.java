package tech.picnic.errorprone.docgen;

import static java.util.stream.Collectors.joining;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ExampleExtractorTest {

  private static final String INPUT =
          String.join("\n",
                  "@Test",
                  "void replacementFirstSuggestedFix() {",
                  "    refactoringTestHelper",
                  "            .addInputLines(",
                  "                    \"A.java\",",
                  "                    \"import static java.util.stream.Collectors.toList;\",",
                  "                    \"import static java.util.stream.Collectors.toMap;\",",
                  "                    \"import static java.util.stream.Collectors.toSet;\",",
                  "                    \"\",",
                  "                    \"import java.util.stream.Collectors;\",",
                  "                    \"import java.util.stream.Stream;\",",
                  "                    \"import reactor.core.publisher.Flux;\",",
                  "                    \"\",",
                  "                    \"class A {\",",
                  "                    \"  void m() {\",",
                  "                    \"    Flux.just(1).collect(Collectors.toList());\",",
                  "                    \"    Flux.just(2).collect(toList());\",",
                  "                    \"\",",
                  "                    \"    Stream.of(\"foo\").collect(Collectors.toMap(String::getBytes, String::length));\",",
                  "                    \"    Stream.of(\"bar\").collect(toMap(String::getBytes, String::length));\",",
                  "                    \"    Flux.just(\"baz\").collect(Collectors.toMap(String::getBytes, String::length, (a, b) -> b));\",",
                  "                    \"    Flux.just(\"qux\").collect(toMap(String::getBytes, String::length, (a, b) -> b));\",",
                  "                    \"\",",
                  "                    \"    Stream.of(1).collect(Collectors.toSet());\",",
                  "                    \"    Stream.of(2).collect(toSet());\",",
                  "                    \"  }\",",
                  "                    \"}\")",
                  "            .addOutputLines(",
                  "                    \"A.java\",",
                  "                    \"import static com.google.common.collect.ImmutableList.toImmutableList;\",",
                  "                    \"import static com.google.common.collect.ImmutableMap.toImmutableMap;\",",
                  "                    \"import static com.google.common.collect.ImmutableSet.toImmutableSet;\",",
                  "                    \"import static java.util.stream.Collectors.toList;\",",
                  "                    \"import static java.util.stream.Collectors.toMap;\",",
                  "                    \"import static java.util.stream.Collectors.toSet;\",",
                  "                    \"\",",
                  "                    \"import java.util.stream.Collectors;\",",
                  "                    \"import java.util.stream.Stream;\",",
                  "                    \"import reactor.core.publisher.Flux;\",",
                  "                    \"\",",
                  "                    \"class A {\",",
                  "                    \"  void m() {\",",
                  "                    \"    Flux.just(1).collect(toImmutableList());\",",
                  "                    \"    Flux.just(2).collect(toImmutableList());\",",
                  "                    \"\",",
                  "                    \"    Stream.of(\"foo\").collect(toImmutableMap(String::getBytes, String::length));\",",
                  "                    \"    Stream.of(\"bar\").collect(toImmutableMap(String::getBytes, String::length));\",",
                  "                    \"    Flux.just(\"baz\").collect(toImmutableMap(String::getBytes, String::length, (a, b) -> b));\",",
                  "                    \"    Flux.just(\"qux\").collect(toImmutableMap(String::getBytes, String::length, (a, b) -> b));\",",
                  "                    \"\",",
                  "                    \"    Stream.of(1).collect(toImmutableSet());\",",
                  "                    \"    Stream.of(2).collect(toImmutableSet());\",",
                  "                    \"  }\",",
                  "                    \"}\")",
                  "            .doTest(TestMode.TEXT_MATCH);",
                  "}");

  @Test
  void regexTest() throws FormatterException {
    final Formatter FORMATTER = new Formatter();
    Pattern pattern =
        Pattern.compile("\\.addInputLines\\((\n.*?\".*?\",)\n(.*?)\\)\n", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(INPUT);
    int count = matcher.groupCount();
    if(!matcher.find()) {
      System.out.println("no match!");
      return;
    }

    String src = matcher.group(2);
    System.out.println("\\\"foo\\\"".replaceAll("\\\\\"(.*?)\\\\\"", "\"$1\""));
  }
}
