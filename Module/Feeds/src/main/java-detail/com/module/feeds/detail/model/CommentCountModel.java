package com.module.feeds.detail.model;

import java.io.Serializable;

public class CommentCountModel implements Serializable {
    int count;

    public CommentCountModel(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
