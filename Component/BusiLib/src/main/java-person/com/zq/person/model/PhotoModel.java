package com.zq.person.model;

import java.io.Serializable;

public class PhotoModel implements Serializable {
    /**
     * picID : 0
     * picPath : string
     */

    private int picID;
    private String picPath;

    public int getPicID() {
        return picID;
    }

    public void setPicID(int picID) {
        this.picID = picID;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    @Override
    public String toString() {
        return "PhotoModel{" +
                "picID=" + picID +
                ", picPath='" + picPath + '\'' +
                '}';
    }
}
