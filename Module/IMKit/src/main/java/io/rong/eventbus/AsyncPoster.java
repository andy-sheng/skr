//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import io.rong.eventbus.EventBus;

class AsyncPoster implements Runnable {
  private final PendingPostQueue queue;
  private final EventBus eventBus;

  AsyncPoster(EventBus eventBus) {
    this.eventBus = eventBus;
    this.queue = new PendingPostQueue();
  }

  public void enqueue(io.rong.eventbus.Subscription subscription, Object event) {
    PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
    this.queue.enqueue(pendingPost);
    this.eventBus.getExecutorService().execute(this);
  }

  public void run() {
    PendingPost pendingPost = this.queue.poll();
    if (pendingPost == null) {
      throw new IllegalStateException("No pending post available");
    } else {
      this.eventBus.invokeSubscriber(pendingPost);
    }
  }
}
