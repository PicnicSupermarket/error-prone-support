package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import java.util.List;

/** XXX: Write this */
public interface RefasterRuleSelectorFactory {
  /**
   * XXX: Write this
   *
   * @param classLoader XXX: Write this
   * @param refasterRules XXX: Write this
   * @return XXX: Write this
   */
  RefasterRuleSelector createRefasterRuleSelector(
      ClassLoader classLoader, List<RefasterRule<?, ?>> refasterRules);

  /**
   * XXX: Write this
   *
   * @param classLoader XXX: Write this
   * @return XXX: Write this
   */
  boolean isClassPathCompatible(ClassLoader classLoader);
}
