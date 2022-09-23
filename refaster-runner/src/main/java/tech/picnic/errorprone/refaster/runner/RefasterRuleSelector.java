package tech.picnic.errorprone.refaster.runner;

import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.Set;

/** XXX: Write this. */
public interface RefasterRuleSelector {
  /**
   * XXX: Write this
   *
   * @param tree XXX: Write this
   * @return XXX: Write this
   */
  Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree);
}
