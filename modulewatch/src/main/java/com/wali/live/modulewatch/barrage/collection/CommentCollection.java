package com.wali.live.modulewatch.barrage.collection;

import android.os.Build;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by anping on 17/2/27.
 * 存储弹幕源数据的数据结构，里面主要存储的是arrayList,顺序的最先插入的数据再最前头.后插入的直接插入在数据后头
 * 删除时，优先插入到第一个。
 * map中放所有的对象的hashcode ,用来equals 比较
 */

public class CommentCollection<T> {

    public static final int DEFAULT_MAX_SIZE = 300;


    private int maxSize = 300; //默认最大存储500条数据

    private boolean needJugeDuplicate = true; //默认是需要去重的

    private ArrayList<T> datasource;

    private Set<T> modelHashCode;

    public CommentCollection(int maxSize) {
        this.maxSize = maxSize;
        int initSize = maxSize > DEFAULT_MAX_SIZE ? DEFAULT_MAX_SIZE : maxSize;

        datasource = new ArrayList<>(initSize);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            modelHashCode = new ArraySet<>(initSize);
        } else {
            modelHashCode = new HashSet<>(initSize);
        }

    }

    public CommentCollection(int maxSize, boolean needJugeDuplicate) {
        this.maxSize = maxSize;
        int initSize = maxSize > DEFAULT_MAX_SIZE ? DEFAULT_MAX_SIZE : maxSize;

        datasource = new ArrayList<>(initSize);
        if (needJugeDuplicate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                modelHashCode = new ArraySet<>(initSize);
            } else {
                modelHashCode = new HashSet<>(initSize);
            }
        }

    }


    public synchronized void insert(T data) {
        if (data == null) {
            return;
        }

        //先判断已经存在重复的数据
        if (needJugeDuplicate && modelHashCode != null) {
            for (T t : modelHashCode) {
                if (t.equals(data)) {
                    return;
                }
            }
        }

        //如果大于最大值，则直接删除第一个
        if (datasource.size() >= maxSize) {
            T firstData = datasource.get(0);
            datasource.remove(0);
            if (modelHashCode != null) {
                modelHashCode.remove(firstData);
            }
        }

        datasource.add(data);
        if (modelHashCode != null) {
            modelHashCode.add(data);
        }
    }

    public synchronized void replaceTail(T data) {
        if (data == null) {
            return;
        }
        if (datasource.size() <= 0) {
            insert(data);
        } else {
            T lastData = datasource.get(datasource.size() - 1);
            if (modelHashCode != null) {
                modelHashCode.remove(lastData);
                modelHashCode.add(data);
            }
            datasource.set(datasource.size() - 1, data);
        }
    }


    public synchronized void clear() {
        datasource.clear();
        if (modelHashCode != null) {
            modelHashCode.clear();
        }
    }

    public void updateMaxSize(int maxSize) {
        if (maxSize <= 0) {
            return;
        }
        this.maxSize = maxSize;
    }

    public synchronized ArrayList<T> getDatasource() {
        return datasource;
    }

    public synchronized T getLastRough() {
        if (datasource.size() <= 0) {
            return null;
        }
        return datasource.get(datasource.size() - 1);
    }
}
