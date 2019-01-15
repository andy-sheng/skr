package com.imagepicker.model;

public abstract class ResItem {
    public static final int RES_IMAGE = 1;
    public static final int RES_VIDEO = 2;
    public abstract int getType();
}
