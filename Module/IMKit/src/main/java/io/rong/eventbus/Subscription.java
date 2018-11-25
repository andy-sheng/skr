//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

final class Subscription {
  final Object subscriber;
  final SubscriberMethod subscriberMethod;
  final int priority;
  volatile boolean active;

  Subscription(Object subscriber, SubscriberMethod subscriberMethod, int priority) {
    this.subscriber = subscriber;
    this.subscriberMethod = subscriberMethod;
    this.priority = priority;
    this.active = true;
  }

  public boolean equals(Object other) {
    if (!(other instanceof io.rong.eventbus.Subscription)) {
      return false;
    } else {
      io.rong.eventbus.Subscription otherSubscription = (io.rong.eventbus.Subscription)other;
      return this.subscriber == otherSubscription.subscriber && this.subscriberMethod.equals(otherSubscription.subscriberMethod);
    }
  }

  public int hashCode() {
    return this.subscriber.hashCode() + this.subscriberMethod.methodString.hashCode();
  }
}
