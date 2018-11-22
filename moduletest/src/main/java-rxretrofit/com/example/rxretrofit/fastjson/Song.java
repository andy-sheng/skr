package com.example.rxretrofit.fastjson;

public class Song {
    String xqusic_mid;
    String xqusic_id;

    public String getXqusic_mid() {
        return xqusic_mid;
    }

    public void setXqusic_mid(String xqusic_mid) {
        this.xqusic_mid = xqusic_mid;
    }

    public String getXqusic_id() {
        return xqusic_id;
    }

    public void setXqusic_id(String xqusic_id) {
        this.xqusic_id = xqusic_id;
    }

    @Override
    public String toString() {
        return "Song{" +
                "xqusic_mid='" + xqusic_mid + '\'' +
                ", xqusic_id='" + xqusic_id + '\'' +
                '}';
    }
}
