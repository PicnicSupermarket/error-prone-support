package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.List;
import java.util.Set;

/** XXX: Write this */
public interface RefasterRuleSelectorFactory {
  /**
   * XXX: Write this
   *
   * @return XXX: Write this
   */
  int priority();

  /**
   * XXX: Write this
   *
   * @return XXX: Write this
   */
  boolean isClassPathCompatible();

  /**
   * XXX: Write this
   *
   * @param refasterRules XXX: Write this
   * @return XXX: Write this
   */
  RefasterRuleSelector createRefasterRuleSelector(List<RefasterRule<?, ?>> refasterRules);

  /** XXX: Write this */
  interface RefasterRuleSelector {
    /**
     * XXX: Write this
     *
     * @param tree XXX: Write this
     * @return XXX: Write this
     */
    Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree);
  }
}
