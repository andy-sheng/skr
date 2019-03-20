package com.module.playways.grab.songselect.model;

import java.io.Serializable;

public class SpecialModel implements Serializable {

    /**
     * tagID : 2
     * tagName : 流行
     * introduction :
     */

    private int tagID;
    private String tagName;
    private String introduction;

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

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    @Override
    public String toString() {
        return "SpecialModel{" +
                "tagID=" + tagID +
                ", tagName='" + tagName + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }
}
