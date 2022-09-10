package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import tech.picnic.errorprone.rule.selector.RefasterRuleSelectorFactory.RefasterRuleSelector;

/** XXX: Write */
public final class NewFactory {
  private NewFactory() {}

  /**
   * XXX: Write this.
   *
   * @param classLoader Test
   * @param refasterRules Test
   * @return Test
   */
  public static RefasterRuleSelector getSelector(
      ClassLoader classLoader, List<RefasterRule<?, ?>> refasterRules) {
    return isCompatibleWithFork(classLoader)
        ? new SmartRefasterRuleSelector(refasterRules)
        : new DefaultRefasterRuleSelector(refasterRules);
  }

  private static boolean isCompatibleWithFork(ClassLoader classLoader) {
    boolean isForkOnClassPath;
    try {
      Class<?> clazz =
          Class.forName(
              "com.google.errorprone.ErrorProneOptions", /* initialize= */ false, classLoader);
      Optional<Method> forkMethod =
          Arrays.stream(clazz.getDeclaredMethods())
              .filter(method -> Modifier.isPublic(method.getModifiers()))
              .filter(m -> m.getName().equals("isSuggestionsAsWarnings"))
              .findFirst();
      // THE CLASS IS HERE, THIS IS THE FORK!
      isForkOnClassPath = forkMethod.isPresent();
      System.out.println("CLASS FOUND? isForkOnClassPath" + isForkOnClassPath);
      //      isForkOnClassPath = true;
    } catch (ClassNotFoundException e) {
      System.out.println("NOT FOUND!!!");
      isForkOnClassPath = false;
    }
    return isForkOnClassPath;
  }
}
