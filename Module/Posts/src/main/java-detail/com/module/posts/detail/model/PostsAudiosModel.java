package com.module.posts.detail.model;

import java.io.Serializable;

public class PostsAudiosModel implements Serializable {
    /**
     * URL : string
     * durTimeMs : 0
     */

    private String URL;
    private int durTimeMs;

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public int getDurTimeMs() {
        return durTimeMs;
    }

    public void setDurTimeMs(int durTimeMs) {
        this.durTimeMs = durTimeMs;
    }
}
