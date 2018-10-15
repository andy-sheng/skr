package com.mi.live.data.query.model;

import com.wali.live.proto.Live2Proto;

/**
 * Created by chengsimin on 16/9/8.
 */
public class LiveCover {
    String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static LiveCover loadFromPb(Live2Proto.LiveCover lc) {
        LiveCover liveCover = new LiveCover();
        liveCover.setUrl(lc.getCoverUrl());
        return liveCover;
    }
}
