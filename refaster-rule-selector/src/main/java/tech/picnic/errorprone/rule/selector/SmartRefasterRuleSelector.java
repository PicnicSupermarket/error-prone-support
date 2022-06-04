package tech.picnic.errorprone.rule.selector;

import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.Set;

public class SmartRefasterRuleSelector implements RefasterRuleSelectorFactory.RefasterRuleSelector {
   private final Set<RefasterRule<?, ?>> refasterRules;

   public SmartRefasterRuleSelector(Set<RefasterRule<?, ?>> refasterRules) {
      this.refasterRules = refasterRules;
   }

   @Override
   public Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree) {
      return null;
   }
}
