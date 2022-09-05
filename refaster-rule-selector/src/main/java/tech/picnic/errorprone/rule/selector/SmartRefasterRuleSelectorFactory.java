package tech.picnic.errorprone.rule.selector;

import com.google.auto.service.AutoService;
import com.google.errorprone.refaster.RefasterRule;
import java.util.List;

/** XXX: Write this */
@AutoService(RefasterRuleSelectorFactory.class)
public final class SmartRefasterRuleSelectorFactory implements RefasterRuleSelectorFactory {
  @Override
  public int priority() {
    return 0;
  }

  @Override
  public boolean isClassPathCompatible() {
    try {
      Class.forName(
          "com.google.errorprone.TimingReporter",
          /* initialize= */ false,
          Thread.currentThread().getContextClassLoader());
      // THE CLASS IS HERE, THIS IS THE FORK!
      //      System.out.println("CLASS FOUND!!!");
      return true;
    } catch (ClassNotFoundException e) {
      //      System.out.println("NOT FOUND!!!");
      return false;
    }
    // XXX: Implement logic to determine whether the fork is on the classpath.
    //    return false;
  }

  @Override
  public RefasterRuleSelector createRefasterRuleSelector(List<RefasterRule<?, ?>> refasterRules) {
    return new SmartRefasterRuleSelector(refasterRules);
  }
}
