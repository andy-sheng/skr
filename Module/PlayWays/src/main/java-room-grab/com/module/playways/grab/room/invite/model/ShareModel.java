package com.module.playways.grab.room.invite.model;

public class ShareModel {

    public static final int SHARE_TYPE_CIPHER = 1;       //暗号
    public static final int SHARE_TYPE_QQ = 2;           //QQ
    public static final int SHARE_TYPE_QQ_QZON = 3;      //QQ空间
    public static final int SHARE_TYPE_WECHAT = 4;       //微信
    public static final int SHARE_TYPE_WECHAT_FRIEND = 5;//朋友圈

    int shareType;
    int resId;
    String desc;

    public ShareModel(int shareType, int resId, String desc) {
        this.shareType = shareType;
        this.resId = resId;
        this.desc = desc;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

}
