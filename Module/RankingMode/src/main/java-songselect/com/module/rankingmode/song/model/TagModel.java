package com.module.rankingmode.song.model;

import java.io.Serializable;

public class TagModel implements Serializable {
    /**
     * tagID : 1
     * tagName : 网络歌曲
     */

    private int tagID;
    private String tagName;

    public int getTagID() {
        return tagID;
    }

    public void setTagID(int tagID) {
        this.tagID = tagID;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
