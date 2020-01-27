package tech.picnic.errorprone.refastertemplates;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;

/** Refaster templates related to expressions dealing with (possibly) null values. */
final class MethodTemplates {
  private MethodTemplates() {}

  /** We prefer that variables are directly returned instead of first assigned and then returned. */
  static final class DirectlyReturnObjectVariable<T> {
    @BeforeTemplate
    T before(T element) {
      T var = element;
      return var;
    }

    @AfterTemplate
    T after(T element) {
      return element;
    }
  }

  static final class DirectlyReturnBooleanVariable {
    @BeforeTemplate
    boolean before(boolean element) {
      boolean var = element;
      return var;
    }

    @AfterTemplate
    boolean after(boolean element) {
      return element;
    }
  }

  static final class DirectlyReturnIntegerVariable {
    @BeforeTemplate
    int before(int element) {
      int var = element;
      return var;
    }

    @AfterTemplate
    int after(int element) {
      return element;
    }
  }

  static final class DirectlyReturnFloatVariable {
    @BeforeTemplate
    float before(float element) {
      float var = element;
      return var;
    }

    @AfterTemplate
    float after(float element) {
      return element;
    }
  }

  static final class DirectlyReturnCharVariable {
    @BeforeTemplate
    char before(char element) {
      char var = element;
      return var;
    }

    @AfterTemplate
    char after(char element) {
      return element;
    }
  }

  static final class DirectlyReturnByteVariable {
    @BeforeTemplate
    byte before(byte element) {
      byte var = element;
      return var;
    }

    @AfterTemplate
    byte after(byte element) {
      return element;
    }
  }

  static final class DirectlyReturnShortVariable {
    @BeforeTemplate
    short before(short element) {
      short var = element;
      return var;
    }

    @AfterTemplate
    short after(short element) {
      return element;
    }
  }

  static final class DirectlyReturnLongVariable {
    @BeforeTemplate
    long before(long element) {
      long var = element;
      return var;
    }

    @AfterTemplate
    long after(long element) {
      return element;
    }
  }

  static final class DirectlyReturnDoubleVariable {
    @BeforeTemplate
    double before(double element) {
      double var = element;
      return var;
    }

    @AfterTemplate
    double after(double element) {
      return element;
    }
  }
}
