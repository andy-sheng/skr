package com.didichuxing.doraemonkit.kit.sysinfo;


import android.view.View;

/**
 * Created by wanglikun on 2018/9/14.
 */

public class SysInfoItem {
    public final String name;
    public final String value;
    public View.OnClickListener listener;
    public SysInfoItem(String name, String value) {
        this.name = name;
        this.value = value;
    }
    public SysInfoItem(String name, String value,View.OnClickListener l) {
        this.name = name;
        this.value = value;
        this.listener = l;
    }
}
