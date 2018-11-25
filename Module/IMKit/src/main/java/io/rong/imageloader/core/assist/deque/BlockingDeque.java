//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.assist.deque;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import io.rong.imageloader.core.assist.deque.Deque;

public interface BlockingDeque<E> extends BlockingQueue<E>, Deque<E> {
  void addFirst(E var1);

  void addLast(E var1);

  boolean offerFirst(E var1);

  boolean offerLast(E var1);

  void putFirst(E var1) throws InterruptedException;

  void putLast(E var1) throws InterruptedException;

  boolean offerFirst(E var1, long var2, TimeUnit var4) throws InterruptedException;

  boolean offerLast(E var1, long var2, TimeUnit var4) throws InterruptedException;

  E takeFirst() throws InterruptedException;

  E takeLast() throws InterruptedException;

  E pollFirst(long var1, TimeUnit var3) throws InterruptedException;

  E pollLast(long var1, TimeUnit var3) throws InterruptedException;

  boolean removeFirstOccurrence(Object var1);

  boolean removeLastOccurrence(Object var1);

  boolean add(E var1);

  boolean offer(E var1);

  void put(E var1) throws InterruptedException;

  boolean offer(E var1, long var2, TimeUnit var4) throws InterruptedException;

  E remove();

  E poll();

  E take() throws InterruptedException;

  E poll(long var1, TimeUnit var3) throws InterruptedException;

  E element();

  E peek();

  boolean remove(Object var1);

  boolean contains(Object var1);

  int size();

  Iterator<E> iterator();

  void push(E var1);
}
