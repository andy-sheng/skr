package com.module.playways.grab.room.dynamicmsg;

import java.io.Serializable;

public class DynamicModel implements Serializable {
    /**
     * bigEmojiURL : string
     * id : 0
     * smallEmojiURL : string
     */

    private String bigEmojiURL;
    private int id;
    private String smallEmojiURL;

    public String getBigEmojiURL() {
        return bigEmojiURL;
    }

    public void setBigEmojiURL(String bigEmojiURL) {
        this.bigEmojiURL = bigEmojiURL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSmallEmojiURL() {
        return smallEmojiURL;
    }

    public void setSmallEmojiURL(String smallEmojiURL) {
        this.smallEmojiURL = smallEmojiURL;
    }


    @Override
    public String toString() {
        return "DynamicModel{" +
                "bigEmojiURL='" + bigEmojiURL + '\'' +
                ", id=" + id +
                ", smallEmojiURL='" + smallEmojiURL + '\'' +
                '}';
    }
}
