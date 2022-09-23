package tech.picnic.errorprone.refaster.runner;

import com.google.errorprone.refaster.RefasterRule;
import com.sun.source.tree.CompilationUnitTree;
import java.util.Set;

/** Definition for a Refaster templates selection algorithm. */
public interface RefasterRuleSelector {
  /**
   * Retrieve a set of Refaster templates that can possibly match based on a {@link
   * CompilationUnitTree}.
   *
   * @param tree The {@link CompilationUnitTree} for which candidate Refaster templates are
   *     selected.
   * @return Set of Refaster templates that can possibly match in the provided {@link
   *     CompilationUnitTree}.
   */
  Set<RefasterRule<?, ?>> selectCandidateRules(CompilationUnitTree tree);
}
