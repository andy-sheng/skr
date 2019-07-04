package com.dialog.list;

public class DialogListItem {
    public String title;
    public Runnable op;
    public String color;

    public DialogListItem(String title, Runnable op) {
        this.title = title;
        this.op = op;
        this.color = "#3B4E79";
    }

    public DialogListItem(String title, String color, Runnable op) {
        this.title = title;
        this.op = op;
        this.color = color;
    }
}
