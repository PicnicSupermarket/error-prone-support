package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/** XXX: Write */
public final class DefaultRuleSelectorFactory implements RefasterRuleSelectorFactory {
  /**
   * XXX: Write this.
   *
   * @param classLoader Test
   * @param refasterRules Test
   * @return Test
   */
  @Override
  public RefasterRuleSelector createRefasterRuleSelector(
      ClassLoader classLoader, List<RefasterRule<?, ?>> refasterRules) {
    return isClassPathCompatible(classLoader)
        ? new SmartRefasterRuleSelector(refasterRules)
        : new DefaultRefasterRuleSelector(refasterRules);
  }

  /**
   * XXX: Write this
   *
   * @param classLoader Test
   * @return Test
   */
  @Override
  public boolean isClassPathCompatible(ClassLoader classLoader) {
    Class<?> clazz;
    try {
      clazz =
          Class.forName(
              "com.google.errorprone.ErrorProneOptions", /* initialize= */ false, classLoader);
    } catch (ClassNotFoundException e) {
      return false;
      //      throw new IllegalStateException("Cannot load
      // `com.google.errorprone.ErrorProneOptions`", e);
    }

    return Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> Modifier.isPublic(m.getModifiers()))
        .anyMatch(m -> m.getName().equals("isSuggestionsAsWarnings"));
  }
}
