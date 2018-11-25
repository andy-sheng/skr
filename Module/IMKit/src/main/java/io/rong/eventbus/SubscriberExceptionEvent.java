//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import io.rong.eventbus.EventBus;

public final class SubscriberExceptionEvent {
  public final EventBus eventBus;
  public final Throwable throwable;
  public final Object causingEvent;
  public final Object causingSubscriber;

  public SubscriberExceptionEvent(EventBus eventBus, Throwable throwable, Object causingEvent, Object causingSubscriber) {
    this.eventBus = eventBus;
    this.throwable = throwable;
    this.causingEvent = causingEvent;
    this.causingSubscriber = causingSubscriber;
  }
}
