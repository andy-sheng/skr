package com.module.playways.grab.songselect;

import java.io.Serializable;

public class SpecialModel implements Serializable {
    int tagID;
    String tagName;

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
