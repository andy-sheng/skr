package com.module.playways.grab.songselect.model;

import java.io.Serializable;

public class SpecialModel implements Serializable {
    /**
     * tagID : 8
     * tagName : 疯狂00后
     * introduction : 请开始你的表演
     * bgColor : #9B6C43
     */

    private int tagID;
    private String tagName;
    private String introduction;
    private String bgColor;

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

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
}
