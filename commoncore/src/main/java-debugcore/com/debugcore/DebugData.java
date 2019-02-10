package com.debugcore;

public class DebugData {
    public CharSequence title;
    public Runnable op;

    public DebugData(CharSequence title, Runnable op) {
        this.title = title;
        this.op = op;
    }
}
