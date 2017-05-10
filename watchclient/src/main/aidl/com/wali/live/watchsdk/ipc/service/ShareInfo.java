package com.wali.live.watchsdk.ipc.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zyh on 2017/4/25.
 */

public class ShareInfo implements Parcelable {
    public static final int TYPE_WECHAT = 0;
    public static final int TYPE_MOMENT = 1;
    public static final int TYPE_QQ = 2;
    public static final int TYPE_QZONE = 3;
    public static final int TYPE_WEIBO = 4;
    public static final int TYPE_MILIAO = 5;
    public static final int TYPE_MILIAO_FEEDS = 6;

//    private int mPlatForm;
    private String mTitle;
    private String mContent;
    private String mPicUrl;
    private String mUrl;

    public ShareInfo(/*int shareType,*/ String title, String content, String picUrl, String url) {
//        mPlatForm = shareType;
        mTitle = title;
        mContent = content;
        mPicUrl = picUrl;
        mUrl = url;
    }

    protected ShareInfo(Parcel in) {
//        mPlatForm = in.readInt();
        mTitle = in.readString();
        mContent = in.readString();
        mPicUrl = in.readString();
        mUrl = in.readString();
    }

    public static final Creator<ShareInfo> CREATOR = new Creator<ShareInfo>() {
        @Override
        public ShareInfo createFromParcel(Parcel in) {
            return new ShareInfo(in);
        }

        @Override
        public ShareInfo[] newArray(int size) {
            return new ShareInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(this.mPlatForm);
        dest.writeString(this.mTitle);
        dest.writeString(this.mContent);
        dest.writeString(this.mPicUrl);
        dest.writeString(this.mUrl);
    }

//    public int getPlatForm() {
//        return mPlatForm;
//    }

    public String getTitle() {
        return mTitle;
    }

    public String getContent() {
        return mContent;
    }

    public String getPicUrl() {
        return mPicUrl;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public String toString() {
        return "ShareInfo{" +
//                "mPlatForm=" + mPlatForm +
                " mTitle='" + mTitle + '\'' +
                ", mContent='" + mContent + '\'' +
                ", mPicUrl='" + mPicUrl + '\'' +
                ", mUrl='" + mUrl + '\'' +
                '}';
    }
}
