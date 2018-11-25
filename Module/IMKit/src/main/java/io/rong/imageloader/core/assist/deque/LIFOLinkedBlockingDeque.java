//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.assist.deque;

import io.rong.imageloader.core.assist.deque.LinkedBlockingDeque;

public class LIFOLinkedBlockingDeque<T> extends LinkedBlockingDeque<T> {
  private static final long serialVersionUID = -4114786347960826192L;

  public LIFOLinkedBlockingDeque() {
  }

  public boolean offer(T e) {
    return super.offerFirst(e);
  }

  public T remove() {
    return super.removeFirst();
  }
}
