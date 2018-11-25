//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.rong.eventbus.EventBus;
import io.rong.eventbus.EventBusException;

public class EventBusBuilder {
  private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();
  boolean logSubscriberExceptions = true;
  boolean logNoSubscriberMessages = true;
  boolean sendSubscriberExceptionEvent = true;
  boolean sendNoSubscriberEvent = true;
  boolean throwSubscriberException;
  boolean eventInheritance = true;
  ExecutorService executorService;
  List<Class<?>> skipMethodVerificationForClasses;

  EventBusBuilder() {
    this.executorService = DEFAULT_EXECUTOR_SERVICE;
  }

  public io.rong.eventbus.EventBusBuilder logSubscriberExceptions(boolean logSubscriberExceptions) {
    this.logSubscriberExceptions = logSubscriberExceptions;
    return this;
  }

  public io.rong.eventbus.EventBusBuilder logNoSubscriberMessages(boolean logNoSubscriberMessages) {
    this.logNoSubscriberMessages = logNoSubscriberMessages;
    return this;
  }

  public io.rong.eventbus.EventBusBuilder sendSubscriberExceptionEvent(boolean sendSubscriberExceptionEvent) {
    this.sendSubscriberExceptionEvent = sendSubscriberExceptionEvent;
    return this;
  }

  public io.rong.eventbus.EventBusBuilder sendNoSubscriberEvent(boolean sendNoSubscriberEvent) {
    this.sendNoSubscriberEvent = sendNoSubscriberEvent;
    return this;
  }

  public io.rong.eventbus.EventBusBuilder throwSubscriberException(boolean throwSubscriberException) {
    this.throwSubscriberException = throwSubscriberException;
    return this;
  }

  public io.rong.eventbus.EventBusBuilder eventInheritance(boolean eventInheritance) {
    this.eventInheritance = eventInheritance;
    return this;
  }

  public io.rong.eventbus.EventBusBuilder executorService(ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }

  public io.rong.eventbus.EventBusBuilder skipMethodVerificationFor(Class<?> clazz) {
    if (this.skipMethodVerificationForClasses == null) {
      this.skipMethodVerificationForClasses = new ArrayList();
    }

    this.skipMethodVerificationForClasses.add(clazz);
    return this;
  }

  public EventBus installDefaultEventBus() {
    Class var1 = EventBus.class;
    synchronized(EventBus.class) {
      if (EventBus.defaultInstance != null) {
        throw new EventBusException("Default instance already exists. It may be only set once before it's used the first time to ensure consistent behavior.");
      } else {
        EventBus.defaultInstance = this.build();
        return EventBus.defaultInstance;
      }
    }
  }

  public EventBus build() {
    return new EventBus(this);
  }
}
