//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import java.lang.reflect.Method;

import io.rong.eventbus.ThreadMode;

final class SubscriberMethod {
  final Method method;
  final ThreadMode threadMode;
  final Class<?> eventType;
  String methodString;

  SubscriberMethod(Method method, ThreadMode threadMode, Class<?> eventType) {
    this.method = method;
    this.threadMode = threadMode;
    this.eventType = eventType;
  }

  public boolean equals(Object other) {
    if (other instanceof io.rong.eventbus.SubscriberMethod) {
      this.checkMethodString();
      io.rong.eventbus.SubscriberMethod otherSubscriberMethod = (io.rong.eventbus.SubscriberMethod)other;
      otherSubscriberMethod.checkMethodString();
      return this.methodString.equals(otherSubscriberMethod.methodString);
    } else {
      return false;
    }
  }

  private synchronized void checkMethodString() {
    if (this.methodString == null) {
      StringBuilder builder = new StringBuilder(64);
      builder.append(this.method.getDeclaringClass().getName());
      builder.append('#').append(this.method.getName());
      builder.append('(').append(this.eventType.getName());
      this.methodString = builder.toString();
    }

  }

  public int hashCode() {
    return this.method.hashCode();
  }
}
