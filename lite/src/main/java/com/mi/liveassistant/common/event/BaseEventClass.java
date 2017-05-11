package com.mi.liveassistant.common.event;

/**
 * Created by chengsimin on 16/7/1.
 */
public abstract class BaseEventClass {
    public int op;
    public Object obj1;
    public Object obj2;

    public BaseEventClass(int op, Object obj1, Object obj2) {
        this.op = op;
        this.obj1 = obj1;
        this.obj2 = obj2;
    }
}
