//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import android.util.Log;

import io.rong.eventbus.EventBus;

final class BackgroundPoster implements Runnable {
  private final PendingPostQueue queue;
  private final EventBus eventBus;
  private volatile boolean executorRunning;

  BackgroundPoster(EventBus eventBus) {
    this.eventBus = eventBus;
    this.queue = new PendingPostQueue();
  }

  public void enqueue(Subscription subscription, Object event) {
    PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
    synchronized(this) {
      this.queue.enqueue(pendingPost);
      if (!this.executorRunning) {
        this.executorRunning = true;
        this.eventBus.getExecutorService().execute(this);
      }

    }
  }

  public void run() {
    try {
      while(true) {
        PendingPost pendingPost = this.queue.poll(1000);
        if (pendingPost == null) {
          synchronized(this) {
            pendingPost = this.queue.poll();
            if (pendingPost == null) {
              this.executorRunning = false;
              return;
            }
          }
        }

        this.eventBus.invokeSubscriber(pendingPost);
      }
    } catch (InterruptedException var9) {
      Log.w("Event", Thread.currentThread().getName() + " was interruppted", var9);
    } finally {
      this.executorRunning = false;
    }
  }
}
