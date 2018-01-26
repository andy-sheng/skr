package com.mi.live.data.push.collection;

import com.base.log.MyLog;

import java.util.ArrayList;

/**
 * @module com.wali.live.video
 * <p>
 * Created by MK on 16/2/25.
 * <p>
 * 按照顺序插入的链表结构, 非线程安全
 * 带有头结点（头结点的next为第一个元素），按照从小到大排序
 * 可以设置最大size，超过之后最大size之后，会先删除第一个元素，然后再插入新的元素
 * 可以设置是否去重，如果设置去重，会先查找待插入的元素，如果找到，执行更新，否则，执行插入操作
 */
public class InsertSortLinkedList<T extends Comparable<T>> {
    public static final String TAG = "InsertSortLinkedList";

    public static final int MAX_MAX_SIZE = 3000;
    public static final int DEFAULT_MAX_SIZE = 500;

    private Node<T> header = new Node<T>();        //头结点
    private int size = 0;
    private int maxSize = DEFAULT_MAX_SIZE;
    private boolean needRemoveDuplicate = true; //是否需要去重

    public InsertSortLinkedList(int maxSize, boolean needRemoveDuplicate) {
        this.maxSize = maxSize;

        this.needRemoveDuplicate = needRemoveDuplicate;
    }

    public InsertSortLinkedList(int maxSize) {
        this(maxSize, true);
    }

    /**
     * 做时间比较插入
     *
     * @param data
     */
    public synchronized void insertOrderByTime(T data) {
        if (data != null) {
            if (needRemoveDuplicate) {
                if (update(data)) {
                    return;
                }
            }
            //开始插入
            if (size >= maxSize) {
                deleteFirst();
            }
            Node<T> node = new Node<T>(data);
            Node<T> current = header.next;
            Node<T> previous = null;
            while (current != null && data.compareTo(current.data) >= 0) {
                previous = current;
                current = current.next;
            }
            if (previous == null) {
                header.next = node;
            } else {
                previous.next = node;
            }
            node.next = current;
            size++;
        }
    }

    private T last;

    // 可以优化
    private ArrayList<T> fastReturnList = new ArrayList();

    /**
     * 不做时间比较直接插入
     *
     * @param data
     */
    public synchronized void insert(T data) {
        if (data != null) {
            last = data;
            if (needRemoveDuplicate) {
                if (update(data)) {
                    return;
                }
            }
            //开始插入
            while (size >= MAX_MAX_SIZE) {
                deleteFirst();
            }
            Node<T> node = new Node<T>(data);
            Node<T> current = header.next;
            Node<T> previous = null;
            int i = 0;
            while (current != null && i < maxSize) {
                i++;
                previous = current;
                current = current.next;
            }
            current = null;
            if (previous == null) {
                header.next = node;
            } else {
                previous.next = node;
            }
            node.next = current;
            size++;
        }
    }

    public synchronized void replaceTail(T data) {
        MyLog.w(TAG, "replaceTail");
        if (data != null) {
            last = data;
            Node<T> current = header.next;
            Node<T> previous = null;
            int i = 0;
            while (current != null && i <= DEFAULT_MAX_SIZE) {
                i++;
                previous = current;
                current = current.next;
            }
            current = null;
            if (previous == null) {
                Node<T> node = new Node<T>(data);
                header.next = node;
                node.next = current;
                size++;
            } else {
                previous.data = data;
            }
        }
    }

    // 不一定完全准
    public T getLastRough() {
        return last;
    }

    public int size() {
        return size;
    }

    public synchronized void clear() {
        header.next = null;
        size = 0;
    }

    public synchronized ArrayList<T> toArrayList() {
        ArrayList<T> result = new ArrayList<T>(size);
        Node<T> current = header.next;
        while (current != null) {
            result.add(current.data);
            current = current.next;
        }
        return result;
    }

    private Node<T> deleteFirst() {
        if (header.next != null) {
            Node<T> temp = header.next;
            header.next = temp.next;
            if (size > 0) {
                size--;
            }
            return temp;
        }
        size = 0;
        return null;
    }

    private boolean update(T data) {
        if (data != null) {
            Node<T> current = header.next; //指向第一个元素
            int i = 0;
            while (current != null && i <= DEFAULT_MAX_SIZE) {
                i++;
                if (current.data.equals(data)) { //找到
                    current.data = data;
                    return true;
                }
                current = current.next;
            }
        }
        return false;
    }

    static class Node<T> {
        T data;     //数据域
        Node<T> next; //后继指针， 链域

        public Node(T data) {
            this.data = data;
            this.next = null;
        }

        public Node() {
            this(null);
        }
    }

    public void updateMaxSize(int maxSize) {
        if (maxSize <= 0) {
            return;
        }
        this.maxSize = maxSize;
    }
}
