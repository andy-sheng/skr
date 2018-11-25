//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus.util;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.rong.eventbus.EventBus;
import io.rong.eventbus.util.HasExecutionScope;
import io.rong.eventbus.util.ThrowableFailureEvent;

public class AsyncExecutor {
  private final Executor threadPool;
  private final Constructor<?> failureEventConstructor;
  private final EventBus eventBus;
  private final Object scope;

  public static io.rong.eventbus.util.AsyncExecutor.Builder builder() {
    return new io.rong.eventbus.util.AsyncExecutor.Builder();
  }

  public static io.rong.eventbus.util.AsyncExecutor create() {
    return (new io.rong.eventbus.util.AsyncExecutor.Builder()).build();
  }

  private AsyncExecutor(Executor threadPool, EventBus eventBus, Class<?> failureEventType, Object scope) {
    this.threadPool = threadPool;
    this.eventBus = eventBus;
    this.scope = scope;

    try {
      this.failureEventConstructor = failureEventType.getConstructor(Throwable.class);
    } catch (NoSuchMethodException var6) {
      throw new RuntimeException("Failure event class must have a constructor with one parameter of type Throwable", var6);
    }
  }

  public void execute(final io.rong.eventbus.util.AsyncExecutor.RunnableEx runnable) {
    this.threadPool.execute(new Runnable() {
      public void run() {
        try {
          runnable.run();
        } catch (Exception var5) {
          Exception e = var5;

          Object event;
          try {
            event = io.rong.eventbus.util.AsyncExecutor.this.failureEventConstructor.newInstance(e);
          } catch (Exception var4) {
            Log.e(EventBus.TAG, "Original exception:", var5);
            throw new RuntimeException("Could not create failure event", var4);
          }

          if (event instanceof HasExecutionScope) {
            ((HasExecutionScope)event).setExecutionScope(io.rong.eventbus.util.AsyncExecutor.this.scope);
          }

          io.rong.eventbus.util.AsyncExecutor.this.eventBus.post(event);
        }

      }
    });
  }

  public interface RunnableEx {
    void run() throws Exception;
  }

  public static class Builder {
    private Executor threadPool;
    private Class<?> failureEventType;
    private EventBus eventBus;

    private Builder() {
    }

    public io.rong.eventbus.util.AsyncExecutor.Builder threadPool(Executor threadPool) {
      this.threadPool = threadPool;
      return this;
    }

    public io.rong.eventbus.util.AsyncExecutor.Builder failureEventType(Class<?> failureEventType) {
      this.failureEventType = failureEventType;
      return this;
    }

    public io.rong.eventbus.util.AsyncExecutor.Builder eventBus(EventBus eventBus) {
      this.eventBus = eventBus;
      return this;
    }

    public io.rong.eventbus.util.AsyncExecutor build() {
      return this.buildForScope((Object)null);
    }

    public io.rong.eventbus.util.AsyncExecutor buildForActivityScope(Activity activity) {
      return this.buildForScope(activity.getClass());
    }

    public io.rong.eventbus.util.AsyncExecutor buildForScope(Object executionContext) {
      if (this.eventBus == null) {
        this.eventBus = EventBus.getDefault();
      }

      if (this.threadPool == null) {
        this.threadPool = Executors.newCachedThreadPool();
      }

      if (this.failureEventType == null) {
        this.failureEventType = ThrowableFailureEvent.class;
      }

      return new io.rong.eventbus.util.AsyncExecutor(this.threadPool, this.eventBus, this.failureEventType, executionContext);
    }
  }
}
