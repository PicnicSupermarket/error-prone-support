package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.Set;
import tech.picnic.errorprone.rule.selector.RefasterRuleSelectorFactory.RefasterRuleSelector;

public final class DefaultRefasterRuleSelector implements RefasterRuleSelector {
   private final Set<RefasterRule<?,?>> refasterRules;

   public DefaultRefasterRuleSelector(Set<RefasterRule<?, ?>> refasterRules) {
      this.refasterRules = refasterRules;
   }

   @Override
   public Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree) {
      return refasterRules;
   }
}
