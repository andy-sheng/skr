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
    Long updateTs;
    String feedsMakeModelJson;

    @Generated(hash = 1346152501)
    public FeedsDraftDB(Long draftID, Long updateTs, String feedsMakeModelJson) {
        this.draftID = draftID;
        this.updateTs = updateTs;
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

    public Long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Long updateTs) {
        this.updateTs = updateTs;
    }
}
