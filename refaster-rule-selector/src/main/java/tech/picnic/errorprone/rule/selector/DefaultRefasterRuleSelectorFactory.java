package tech.picnic.errorprone.rule.selector;

import com.google.auto.service.AutoService;
import com.google.errorprone.refaster.RefasterRule;
import java.util.List;

/** XXX: Write this */
@AutoService(RefasterRuleSelectorFactory.class)
public final class DefaultRefasterRuleSelectorFactory implements RefasterRuleSelectorFactory {
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
  public RefasterRuleSelector createRefasterRuleSelector(List<RefasterRule<?, ?>> refasterRules) {
    return new DefaultRefasterRuleSelector(refasterRules);
  }
}
