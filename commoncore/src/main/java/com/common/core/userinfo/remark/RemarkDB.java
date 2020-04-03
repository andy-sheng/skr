package com.common.core.userinfo.remark;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;

import java.io.Serializable;

@Entity
public class RemarkDB implements Serializable {
    private static final long serialVersionUID = -5809782078272943999L;
    @Id
    Long userID;
    String remarkContent;
    @Keep
    public RemarkDB(Long userID, String remarkContent) {
        this.userID = userID;
        this.remarkContent = remarkContent;
    }
    @Keep
    public RemarkDB() {
    }
    public Long getUserID() {
        return this.userID;
    }
    public void setUserID(Long userID) {
        this.userID = userID;
    }
    public String getRemarkContent() {
        return this.remarkContent;
    }
    public void setRemarkContent(String remarkContent) {
        this.remarkContent = remarkContent;
    }

}
