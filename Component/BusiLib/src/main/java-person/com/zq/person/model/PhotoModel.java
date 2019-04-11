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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhotoModel that = (PhotoModel) o;

        if (picID != that.picID) return false;
        return picPath != null ? picPath.equals(that.picPath) : that.picPath == null;
    }

    @Override
    public int hashCode() {
        int result = picID;
        result = 31 * result + (picPath != null ? picPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PhotoModel{" +
                "picID=" + picID +
                ", picPath='" + picPath + '\'' +
                '}';
    }
}
