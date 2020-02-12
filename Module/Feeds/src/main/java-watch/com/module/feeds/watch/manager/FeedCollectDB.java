package com.module.feeds.watch.manager;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FeedCollectDB implements Serializable {

    private static final long serialVersionUID = -5409781574576943999L;

    @Id
    Long feedID;
    Long feedType; // 类型
    Long timeMs;   // 收藏时间戳
    String feedSong;  // 歌曲信息
    String user;      // 所属
    @Generated(hash = 1589526565)
    public FeedCollectDB(Long feedID, Long feedType, Long timeMs, String feedSong,
            String user) {
        this.feedID = feedID;
        this.feedType = feedType;
        this.timeMs = timeMs;
        this.feedSong = feedSong;
        this.user = user;
    }
    @Generated(hash = 1737506571)
    public FeedCollectDB() {
    }
    public Long getFeedID() {
        return this.feedID;
    }
    public void setFeedID(Long feedID) {
        this.feedID = feedID;
    }
    public Long getFeedType() {
        return this.feedType;
    }
    public void setFeedType(Long feedType) {
        this.feedType = feedType;
    }
    public Long getTimeMs() {
        return this.timeMs;
    }
    public void setTimeMs(Long timeMs) {
        this.timeMs = timeMs;
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
