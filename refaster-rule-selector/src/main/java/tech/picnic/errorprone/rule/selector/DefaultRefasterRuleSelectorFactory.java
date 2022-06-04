package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.Set;
import tech.picnic.errorprone.rule.selector.RefasterRuleSelectorFactory.RefasterRuleSelector;

public class DefaultRefasterRuleSelectorFactory implements RefasterRuleSelectorFactory {
  @Override
  public int priority() {
    return 1;
  }

  @Override
  public boolean isClassPathCompatible() {
    // XXX: Implement logic here to check this.
    return true;
  }

  @Override
  public RefasterRuleSelector createRefasterRuleSelector(Set<RefasterRule<?, ?>> refasterRules) {
    return new DefaultRefasterRuleSelector(refasterRules);
  }
}
