package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.Set;

public interface RefasterRuleSelectorFactory {
  int priority();

  boolean isClassPathCompatible();

  RefasterRuleSelector createRefasterRuleSelector(Set<RefasterRule<?,?>> refasterRules);

  interface RefasterRuleSelector {
    Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree);
  }
}
