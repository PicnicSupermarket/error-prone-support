package tech.picnic.errorprone.rule.selector;

import com.google.auto.service.AutoService;
import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** XXX: Write this */
@AutoService(RefasterRuleSelector.class)
public final class DefaultRefasterRuleSelector implements RefasterRuleSelector {
  private final List<RefasterRule<?, ?>> refasterRules;

  /**
   * XXX: Write this.
   *
   * @param refasterRules XXX: Write this
   */
  public DefaultRefasterRuleSelector(List<RefasterRule<?, ?>> refasterRules) {
    this.refasterRules = refasterRules;
  }

  @Override
  public Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree) {
    return new HashSet<>(refasterRules);
  }
}
