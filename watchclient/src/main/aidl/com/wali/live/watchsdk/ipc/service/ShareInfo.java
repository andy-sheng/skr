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

    public static final int TYPE_DEFAULT = -1;

    private int mSnsType = TYPE_DEFAULT;
    private String mTitle;
    private String mContent;
    private String mPicUrl;
    private String mUrl;

    public ShareInfo(String title, String content, String picUrl, String url) {
        mTitle = title;
        mContent = content;
        mPicUrl = picUrl;
        mUrl = url;
    }

    public ShareInfo(int shareType, String title, String content, String picUrl, String url) {
        mSnsType = shareType;
        mTitle = title;
        mContent = content;
        mPicUrl = picUrl;
        mUrl = url;
    }

    protected ShareInfo(Parcel in) {
        mSnsType = in.readInt();
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
        dest.writeInt(this.mSnsType);
        dest.writeString(this.mTitle);
        dest.writeString(this.mContent);
        dest.writeString(this.mPicUrl);
        dest.writeString(this.mUrl);
    }

    public int getSnsType() {
        return mSnsType;
    }

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
                "mSnsType=" + mSnsType +
                ", mTitle='" + mTitle + '\'' +
                ", mContent='" + mContent + '\'' +
                ", mPicUrl='" + mPicUrl + '\'' +
                ", mUrl='" + mUrl + '\'' +
                '}';
    }
}
