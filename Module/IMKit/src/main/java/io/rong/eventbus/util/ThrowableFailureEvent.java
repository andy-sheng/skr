//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus.util;

import io.rong.eventbus.util.HasExecutionScope;

public class ThrowableFailureEvent implements HasExecutionScope {
  protected final Throwable throwable;
  protected final boolean suppressErrorUi;
  private Object executionContext;

  public ThrowableFailureEvent(Throwable throwable) {
    this.throwable = throwable;
    this.suppressErrorUi = false;
  }

  public ThrowableFailureEvent(Throwable throwable, boolean suppressErrorUi) {
    this.throwable = throwable;
    this.suppressErrorUi = suppressErrorUi;
  }

  public Throwable getThrowable() {
    return this.throwable;
  }

  public boolean isSuppressErrorUi() {
    return this.suppressErrorUi;
  }

  public Object getExecutionScope() {
    return this.executionContext;
  }

  public void setExecutionScope(Object executionContext) {
    this.executionContext = executionContext;
  }
}
