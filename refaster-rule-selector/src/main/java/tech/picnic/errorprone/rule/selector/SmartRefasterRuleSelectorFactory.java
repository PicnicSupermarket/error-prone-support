package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import java.util.Set;

public class SmartRefasterRuleSelectorFactory implements RefasterRuleSelectorFactory {
   @Override
   public int priority() {
      return 0;
   }

   @Override
   public boolean isClassPathCompatible() {
      // XXX: Implement logic to determine whether the fork is on the classpath.
      return false;
   }

   @Override
   public RefasterRuleSelector createRefasterRuleSelector(Set<RefasterRule<?, ?>> refasterRules) {
      return new SmartRefasterRuleSelector(refasterRules);
   }
}
