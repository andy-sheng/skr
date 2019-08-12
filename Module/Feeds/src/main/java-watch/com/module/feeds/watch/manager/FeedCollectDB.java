package com.module.feeds.watch.manager;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;


@Entity
public class FeedCollectDB implements Serializable {

    private static final long serialVersionUID = -5409781574576943999L;

    @Id
    Integer feedID;
    Integer feedType; // 类型
    String feedSong;  // 歌曲信息
    String user;      // 所属
    @Generated(hash = 1790950723)
    public FeedCollectDB(Integer feedID, Integer feedType, String feedSong,
            String user) {
        this.feedID = feedID;
        this.feedType = feedType;
        this.feedSong = feedSong;
        this.user = user;
    }
    @Generated(hash = 1737506571)
    public FeedCollectDB() {
    }
    public Integer getFeedID() {
        return this.feedID;
    }
    public void setFeedID(Integer feedID) {
        this.feedID = feedID;
    }
    public Integer getFeedType() {
        return this.feedType;
    }
    public void setFeedType(Integer feedType) {
        this.feedType = feedType;
    }
    public String getFeedSong() {
        return this.feedSong;
    }
    public void setFeedSong(String feedSong) {
        this.feedSong = feedSong;
    }
    public String getUser() {
        return this.user;
    }
    public void setUser(String user) {
        this.user = user;
    }
}
