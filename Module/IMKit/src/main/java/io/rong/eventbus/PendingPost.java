//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.eventbus;

import java.util.ArrayList;
import java.util.List;

final class PendingPost {
  private static final List<io.rong.eventbus.PendingPost> pendingPostPool = new ArrayList();
  Object event;
  io.rong.eventbus.Subscription subscription;
  io.rong.eventbus.PendingPost next;

  private PendingPost(Object event, io.rong.eventbus.Subscription subscription) {
    this.event = event;
    this.subscription = subscription;
  }

  static io.rong.eventbus.PendingPost obtainPendingPost(io.rong.eventbus.Subscription subscription, Object event) {
    List var2 = pendingPostPool;
    synchronized(pendingPostPool) {
      int size = pendingPostPool.size();
      if (size > 0) {
        io.rong.eventbus.PendingPost pendingPost = (io.rong.eventbus.PendingPost)pendingPostPool.remove(size - 1);
        pendingPost.event = event;
        pendingPost.subscription = subscription;
        pendingPost.next = null;
        return pendingPost;
      }
    }

    return new io.rong.eventbus.PendingPost(event, subscription);
  }

  static void releasePendingPost(io.rong.eventbus.PendingPost pendingPost) {
    pendingPost.event = null;
    pendingPost.subscription = null;
    pendingPost.next = null;
    List var1 = pendingPostPool;
    synchronized(pendingPostPool) {
      if (pendingPostPool.size() < 10000) {
        pendingPostPool.add(pendingPost);
      }

    }
  }
}
