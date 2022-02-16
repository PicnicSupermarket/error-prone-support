package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableSet;

@SuppressWarnings("DeclarationOrder" /* The private constructor should come first. */)
final class JavaKeywords {
  private JavaKeywords() {}

  /**
   * List of all Java Language Keywords.
   *
   * <p>See: the <a
   * href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html">Oracle
   * Documentation</a> on Java Keywords.
   */
  private static final ImmutableSet<String> JAVA_KEYWORDS =
      ImmutableSet.of(
          "assert",
          "boolean",
          "break",
          "byte",
          "case",
          "catch",
          "char",
          "class",
          "const",
          "continue",
          "default",
          "do",
          "double",
          "else",
          "enum",
          "extends",
          "final",
          "finally",
          "float",
          "for",
          "goto",
          "if",
          "implements",
          "import",
          "instanceof",
          "int",
          "interface",
          "long",
          "native",
          "new",
          "package",
          "private",
          "protected",
          "public",
          "return",
          "short",
          "static",
          "strictfp",
          "super",
          "switch",
          "synchronized",
          "this",
          "throw",
          "throws",
          "transient",
          "try",
          "void",
          "volatile",
          "while");

  /**
   * Check whether the String is a Java Language Keyword.
   *
   * @param possibleKeyword a possible Java keyword
   * @return whether the String is a Java keyword
   */
  public static boolean isJavaKeyword(String possibleKeyword) {
    return JAVA_KEYWORDS.contains(possibleKeyword);
  }
}
