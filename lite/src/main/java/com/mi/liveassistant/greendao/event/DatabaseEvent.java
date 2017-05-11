package com.mi.liveassistant.greendao.event;

import com.mi.liveassistant.common.event.BaseEventClass;

/**
 * Created by chengsimin on 16/7/1.
 */
public class DatabaseEvent {
    private DatabaseEvent() {
    }

    public static int ADD = 1;
    public static int UPDATE = 2;
    public static int DELETE = 3;

    public static class Relation extends BaseEventClass {
        public Relation(int op, Object obj1, Object obj2) {
            super(op, obj1, obj2);
        }
    }

    public static class Song extends BaseEventClass {
        public Song(int op, Object obj1, Object obj2) {
            super(op, obj1, obj2);
        }
    }
}
