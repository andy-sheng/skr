//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import io.rong.eventbus.EventBus;
import io.rong.eventbus.EventBusException;

final class HandlerPoster extends Handler {
  private final PendingPostQueue queue;
  private final int maxMillisInsideHandleMessage;
  private final EventBus eventBus;
  private boolean handlerActive;

  HandlerPoster(EventBus eventBus, Looper looper, int maxMillisInsideHandleMessage) {
    super(looper);
    this.eventBus = eventBus;
    this.maxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
    this.queue = new PendingPostQueue();
  }

  void enqueue(io.rong.eventbus.Subscription subscription, Object event) {
    PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
    synchronized(this) {
      this.queue.enqueue(pendingPost);
      if (!this.handlerActive) {
        this.handlerActive = true;
        if (!this.sendMessage(this.obtainMessage())) {
          throw new EventBusException("Could not send handler message");
        }
      }

    }
  }

  public void handleMessage(Message msg) {
    boolean rescheduled = false;

    try {
      long started = SystemClock.uptimeMillis();

      long timeInMethod;
      do {
        PendingPost pendingPost = this.queue.poll();
        if (pendingPost == null) {
          synchronized(this) {
            pendingPost = this.queue.poll();
            if (pendingPost == null) {
              this.handlerActive = false;
              return;
            }
          }
        }

        this.eventBus.invokeSubscriber(pendingPost);
        timeInMethod = SystemClock.uptimeMillis() - started;
      } while(timeInMethod < (long)this.maxMillisInsideHandleMessage);

      if (!this.sendMessage(this.obtainMessage())) {
        throw new EventBusException("Could not send handler message");
      } else {
        rescheduled = true;
      }
    } finally {
      this.handlerActive = rescheduled;
    }
  }
}
