package com.didichuxing.doraemonkit.ui.widget.dialog;

public class DialogListItem {
    public String title;
    public Runnable op;

    public DialogListItem(String title, Runnable op) {
        this.title = title;
        this.op = op;
    }
}
