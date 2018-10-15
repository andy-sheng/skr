package com.wali.live.watchsdk.longtext.model.interior.item;

import com.wali.live.proto.Feeds;
import com.wali.live.watchsdk.longtext.adapter.FeedItemUiType;

public class VideoFeedItemModel extends BaseFeedItemModel {
    private String mUrl;
    private String mCoverUrl;
    private int mWidth;
    private int mHeight;
    private long mDuration;
    private long mFileSize;
    private String mDesc;

    private VideoFeedItemModel() {
        mUrl = ((int) (Math.random() * 2) == 0) ? "" : "http://zbuvideo.zb.mi.com/uv_4641299_aHJHaDBRV3h5UGlVZk0rMHRsK3ppQT09_1503538187995.mp4";
        mCoverUrl = ((int) (Math.random() * 2) == 0) ? "" : "http://bbs.nju.edu.cn/file/Pictures/1614887291.jpg";
        mWidth = ((int) (Math.random() * 2) == 0) ? 0 : 870;
        mHeight = ((int) (Math.random() * 2) == 0) ? 0 : 560;
        mDesc = ((int) (Math.random() * 2) == 0) ? "" : mCoverUrl;
    }

    public VideoFeedItemModel(Feeds.Video protoVideo) {
        parse(protoVideo);
    }

    public void parse(Feeds.Video protoVideo) {
        mUrl = protoVideo.getUrl();
        mCoverUrl = protoVideo.getCoverPage();
        mWidth = protoVideo.getWidth();
        mHeight = protoVideo.getHeight();
        mDuration = protoVideo.getDuration();
        mFileSize = protoVideo.getFileSize();
        mDesc = protoVideo.getDesc();
    }

    public String getUrl() {
        return mUrl;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public long getDuration() {
        return mDuration;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public String getDesc() {
        return mDesc;
    }

    @Override
    public int getUiType() {
        return FeedItemUiType.UI_TYPE_VIDEO;
    }

    public static VideoFeedItemModel newTestInstance() {
        return new VideoFeedItemModel();
    }
}

