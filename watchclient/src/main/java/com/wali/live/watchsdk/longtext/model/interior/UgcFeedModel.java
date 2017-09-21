package com.wali.live.watchsdk.longtext.model.interior;

import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.wali.live.proto.Feeds.UGCFeed;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

public abstract class UgcFeedModel extends BaseViewModel {
    //ugc的类型
    public final static int UGC_TYPE_PIC = 1;               //图片
    public final static int UGC_TYPE_VIDEO = 2;             //视频
    public final static int UGC_TYPE_SMALLVIDEO_WORKS = 6;  //作品

    //ugc扩展类型
    public final static int UGC_EXT_TYPE_JOURNAL = 1;       //ugc扩展类型, 日志
    public final static int UGC_EXT_TYPE_RECORD = 2;        //ugc扩展类型, 直播录屏
    public final static int UGC_EXT_TYPE_LONG_TEXT = 3;     //ugc扩展类型,长文

    protected int mType;
    protected String mUrl;
    protected String mCoverUrl;

    protected String mTitle;
    protected String mDesc;

    protected String mAddr;
    protected String mShareUrl;

    protected int mWidth;
    protected int mHeight;

    protected int mViewerCount;

    protected int mExtType;

    protected UgcFeedModel(UGCFeed protoUgcFeed) throws Exception {
        parse(protoUgcFeed);
    }

    public void parse(UGCFeed protoUgcFeed) throws Exception {
        if (protoUgcFeed == null) {
            throw new NullPointerException("protoUgcFeed is null");
        }

        mType = protoUgcFeed.getType();

        mUrl = protoUgcFeed.getUrl();
        mCoverUrl = protoUgcFeed.getCoverPage();
        if (TextUtils.isEmpty(mCoverUrl)) {
            mCoverUrl = mUrl;
        }

        mTitle = protoUgcFeed.getTiltle();
        mDesc = protoUgcFeed.getDesc();

        mAddr = protoUgcFeed.getAddr();
        mShareUrl = protoUgcFeed.getShareUrl();
        mWidth = protoUgcFeed.getWidth();
        mHeight = protoUgcFeed.getHeight();

        mViewerCount = protoUgcFeed.getViewCount();

        mExtType = protoUgcFeed.getExtType();
        parseExtData(protoUgcFeed.getExtData());
    }

    protected abstract void parseExtData(ByteString extData) throws Exception;

    public int getViewerCount() {
        return mViewerCount;
    }

    public String toPrint() {
        return String.valueOf(mExtType);
    }
}