package tech.picnic.errorprone.bugpatterns.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/** Utility class that can be used to identify reserved keywords of the Java language. */
public final class JavaKeywords {
  /**
   * List of all reserved keywords in the Java language.
   *
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-3.html#jls-3.9">JDK 17 JLS
   *     section 3.9: Keywords</a>
   */
  private static final ImmutableSet<String> RESERVED_KEYWORDS =
      ImmutableSet.of(
          "_",
          "abstract",
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
   * List of all contextual keywords in the Java language.
   *
   * @see <a href="https://docs.oracle.com/javase/specs/jls/se17/html/jls-3.html#jls-3.9">JDK 17 JLS
   *     section 3.9: Keywords</a>
   */
  private static final ImmutableSet<String> CONTEXTUAL_KEYWORDS =
      ImmutableSet.of(
          "exports",
          "module",
          "non-sealed",
          "open",
          "opens",
          "permits",
          "provides",
          "record",
          "requires",
          "sealed",
          "to",
          "transitive",
          "uses",
          "var",
          "with",
          "yield");

  /** List of all keywords in the Java language. */
  private static final ImmutableSet<String> ALL_KEYWORDS =
      Sets.union(RESERVED_KEYWORDS, CONTEXTUAL_KEYWORDS).immutableCopy();

  private JavaKeywords() {}

  /**
   * Tells whether the given string is a reserved keyword in the Java language.
   *
   * @param str The string of interest.
   * @return {@code true} if the given string is a reserved keyword in the Java language.
   */
  public static boolean isReservedKeyword(String str) {
    return RESERVED_KEYWORDS.contains(str);
  }

  /**
   * Tells whether the given string is a contextual keyword in the Java language.
   *
   * @param str The string of interest.
   * @return {@code true} if the given string is a contextual keyword in the Java language.
   */
  public static boolean isContextualKeyword(String str) {
    return CONTEXTUAL_KEYWORDS.contains(str);
  }

  /**
   * Tells whether the given string is a reserved or contextual keyword in the Java language.
   *
   * @param str The string of interest.
   * @return {@code true} if the given string is a reserved or contextual keyword in the Java
   *     language.
   */
  public static boolean isKeyword(String str) {
    return ALL_KEYWORDS.contains(str);
  }
}
