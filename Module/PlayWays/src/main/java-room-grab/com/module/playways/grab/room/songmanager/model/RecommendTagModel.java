package com.module.playways.grab.room.songmanager.model;

import java.io.Serializable;

public class RecommendTagModel implements Serializable {

    /**
     * type : 123
     * name : 热门排行1
     */

    private int type;
    private String name;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
