//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import io.rong.eventbus.EventBus;

public final class NoSubscriberEvent {
  public final EventBus eventBus;
  public final Object originalEvent;

  public NoSubscriberEvent(EventBus eventBus, Object originalEvent) {
    this.eventBus = eventBus;
    this.originalEvent = originalEvent;
  }
}
