package com.module.feeds.make.make;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FeedsDraftDB implements Serializable {
    private static final long serialVersionUID = -5209781574576943999L;
    @Id
    Long draftID;
    String feedsMakeModelJson;
    @Generated(hash = 1605575814)
    public FeedsDraftDB(Long draftID, String feedsMakeModelJson) {
        this.draftID = draftID;
        this.feedsMakeModelJson = feedsMakeModelJson;
    }
    @Generated(hash = 1536597371)
    public FeedsDraftDB() {
    }
    public Long getDraftID() {
        return this.draftID;
    }
    public void setDraftID(Long draftID) {
        this.draftID = draftID;
    }
    public String getFeedsMakeModelJson() {
        return this.feedsMakeModelJson;
    }
    public void setFeedsMakeModelJson(String feedsMakeModelJson) {
        this.feedsMakeModelJson = feedsMakeModelJson;
    }
}
