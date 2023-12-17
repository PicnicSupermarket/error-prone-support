package tech.picnic.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class ExplicitEnumOrderingTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(ExplicitEnumOrdering.class, getClass())
        .addSourceLines(
            "A.java",
            """
            import static java.lang.annotation.RetentionPolicy.CLASS;
            import static java.lang.annotation.RetentionPolicy.RUNTIME;
            import static java.lang.annotation.RetentionPolicy.SOURCE;
            import static java.time.chrono.IsoEra.BCE;
            import static java.time.chrono.IsoEra.CE;

            import com.google.common.collect.ImmutableList;
            import com.google.common.collect.Ordering;
            import java.lang.annotation.RetentionPolicy;
            import java.time.chrono.IsoEra;

            class A {
              {
                // The `List`-accepting overload is currently ignored.
                Ordering.explicit(ImmutableList.of(RetentionPolicy.SOURCE, RetentionPolicy.CLASS));

                Ordering.explicit(IsoEra.BCE, IsoEra.CE);
                // BUG: Diagnostic contains: IsoEra.CE
                Ordering.explicit(IsoEra.BCE);
                // BUG: Diagnostic contains: IsoEra.BCE
                Ordering.explicit(IsoEra.CE);

                Ordering.explicit(RetentionPolicy.SOURCE, RetentionPolicy.CLASS, RetentionPolicy.RUNTIME);
                // BUG: Diagnostic contains: RetentionPolicy.CLASS, RetentionPolicy.RUNTIME
                Ordering.explicit(RetentionPolicy.SOURCE);
                // BUG: Diagnostic contains: RetentionPolicy.SOURCE, RetentionPolicy.RUNTIME
                Ordering.explicit(RetentionPolicy.CLASS);
                // BUG: Diagnostic contains: RetentionPolicy.SOURCE, RetentionPolicy.CLASS
                Ordering.explicit(RetentionPolicy.RUNTIME);
                // BUG: Diagnostic contains: RetentionPolicy.RUNTIME
                Ordering.explicit(RetentionPolicy.SOURCE, RetentionPolicy.CLASS);
                // BUG: Diagnostic contains: RetentionPolicy.CLASS
                Ordering.explicit(RetentionPolicy.SOURCE, RetentionPolicy.RUNTIME);
                // BUG: Diagnostic contains: RetentionPolicy.SOURCE
                Ordering.explicit(RetentionPolicy.CLASS, RetentionPolicy.RUNTIME);

                Ordering.explicit(BCE, CE);
                // BUG: Diagnostic contains: IsoEra.CE
                Ordering.explicit(BCE);
                // BUG: Diagnostic contains: IsoEra.BCE
                Ordering.explicit(CE);

                Ordering.explicit(SOURCE, CLASS, RUNTIME);
                // BUG: Diagnostic contains: RetentionPolicy.CLASS, RetentionPolicy.RUNTIME
                Ordering.explicit(SOURCE);
                // BUG: Diagnostic contains: RetentionPolicy.SOURCE, RetentionPolicy.RUNTIME
                Ordering.explicit(CLASS);
                // BUG: Diagnostic contains: RetentionPolicy.SOURCE, RetentionPolicy.CLASS
                Ordering.explicit(RUNTIME);
                // BUG: Diagnostic contains: RetentionPolicy.RUNTIME
                Ordering.explicit(SOURCE, CLASS);
                // BUG: Diagnostic contains: RetentionPolicy.CLASS
                Ordering.explicit(SOURCE, RUNTIME);
                // BUG: Diagnostic contains: RetentionPolicy.SOURCE
                Ordering.explicit(CLASS, RUNTIME);

                Ordering.explicit(RetentionPolicy.SOURCE, BCE, RetentionPolicy.CLASS, CE, RUNTIME);
                Ordering.explicit(SOURCE, IsoEra.BCE, CLASS, IsoEra.CE, RetentionPolicy.RUNTIME);
                // BUG: Diagnostic contains: RetentionPolicy.CLASS
                Ordering.explicit(RetentionPolicy.SOURCE, BCE, CE, RUNTIME);
                // BUG: Diagnostic contains: RetentionPolicy.CLASS
                Ordering.explicit(IsoEra.BCE, SOURCE, IsoEra.CE, RetentionPolicy.RUNTIME);
                // BUG: Diagnostic contains: IsoEra.CE, RetentionPolicy.RUNTIME
                Ordering.explicit(IsoEra.BCE, SOURCE, RetentionPolicy.CLASS);
                // BUG: Diagnostic contains: RetentionPolicy.SOURCE, IsoEra.BCE
                Ordering.explicit(CLASS, RUNTIME, CE);
              }
            }
            """)
        .doTest();
  }
}
