//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus.util;

import android.content.res.Resources;
import android.util.Log;

import io.rong.eventbus.EventBus;
import io.rong.eventbus.util.ExceptionToResourceMapping;

public class ErrorDialogConfig {
  final Resources resources;
  final int defaultTitleId;
  final int defaultErrorMsgId;
  final ExceptionToResourceMapping mapping;
  EventBus eventBus;
  boolean logExceptions = true;
  String tagForLoggingExceptions;
  int defaultDialogIconId;
  Class<?> defaultEventTypeOnDialogClosed;

  public ErrorDialogConfig(Resources resources, int defaultTitleId, int defaultMsgId) {
    this.resources = resources;
    this.defaultTitleId = defaultTitleId;
    this.defaultErrorMsgId = defaultMsgId;
    this.mapping = new ExceptionToResourceMapping();
  }

  public io.rong.eventbus.util.ErrorDialogConfig addMapping(Class<? extends Throwable> clazz, int msgId) {
    this.mapping.addMapping(clazz, msgId);
    return this;
  }

  public int getMessageIdForThrowable(Throwable throwable) {
    Integer resId = this.mapping.mapThrowable(throwable);
    if (resId != null) {
      return resId;
    } else {
      Log.d(EventBus.TAG, "No specific message ressource ID found for " + throwable);
      return this.defaultErrorMsgId;
    }
  }

  public void setDefaultDialogIconId(int defaultDialogIconId) {
    this.defaultDialogIconId = defaultDialogIconId;
  }

  public void setDefaultEventTypeOnDialogClosed(Class<?> defaultEventTypeOnDialogClosed) {
    this.defaultEventTypeOnDialogClosed = defaultEventTypeOnDialogClosed;
  }

  public void disableExceptionLogging() {
    this.logExceptions = false;
  }

  public void setTagForLoggingExceptions(String tagForLoggingExceptions) {
    this.tagForLoggingExceptions = tagForLoggingExceptions;
  }

  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  EventBus getEventBus() {
    return this.eventBus != null ? this.eventBus : EventBus.getDefault();
  }
}
