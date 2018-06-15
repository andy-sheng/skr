package com.wali.live.watchsdk.channel.viewmodel;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.google.protobuf.GeneratedMessage;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.channel.util.Base64;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道数据模型抽象基类，提供title的数据处理
 */
public abstract class ChannelViewModel<GM extends GeneratedMessage> extends BaseViewModel {
    protected int mUiType;
    protected boolean mFullColumn;
    protected int mSectionId;

    protected String mHead;
    protected String mHeadUri;
    protected String mSubHead;
    protected String mHeadIconUri;
    protected int mHeadType;
    protected String mHeaderViewAllText;

    // 客户端添加的栏目标识
    protected String mHeadKey;

    protected String mStatisticsKey;
    protected int mGroupPosition;
    protected boolean mIsLast;

    protected int mImageWidth;
    protected int mImageHeight;
    protected float mRatio = 1f;
    protected int mFrameHeight;

    protected boolean mIsHide; // 是否隐藏

    /**
     * 注意：此构造函数用于构造测试数据
     */
    protected ChannelViewModel() {
    }

    protected ChannelViewModel(GM protoItem) throws Exception {
        parse(protoItem);
    }

    protected void parse(GM protoItem) throws Exception {
        parseTemplate(protoItem);
    }

    protected abstract void parseTemplate(GM protoItem) throws Exception;

    protected void generateEncodeHead() {
        if (hasHead()) {
            mHeadKey = "";
            if (!TextUtils.isEmpty(mHead)) {
                mHeadKey = Base64.encode(mHead.getBytes());
            }
            String encodeUri = "";
            if (!TextUtils.isEmpty(mHeadUri)) {
                encodeUri = Base64.encode(mHeadUri.getBytes());
            }
            mStatisticsKey = String.format(StatisticsKey.KEY_CHANNEL_CLICK_MORE, mHeadKey, encodeUri);
        }
    }

    public int getUiType() {
        return mUiType;
    }

    public boolean isFullColumn() {
        return mFullColumn;
    }

    public int getSectionId() {
        return mSectionId;
    }

    public String getHead() {
        return mHead;
    }

    public String getHeadUri() {
        return mHeadUri;
    }

    public String getHeadIconUri(){
        return mHeadIconUri;
    }

    public String getHeaderViewAllText() {
        return mHeaderViewAllText;
    }

    public String getStatisticsKey() {
        return mStatisticsKey;
    }

    public int getGroupPosition() {
        return mGroupPosition;
    }

    public void setGroupPosition(int groupPosition) {
        mGroupPosition = groupPosition;
    }

    public boolean isFirst() {
        return mGroupPosition == 0;
    }

    public boolean isLast() {
        return mIsLast;
    }

    public void setLast(boolean isLast) {
        mIsLast = isLast;
    }

    public int getHeadType() {
        return mHeadType;
    }

    public boolean hasHead() {
        return !TextUtils.isEmpty(mHead) || !TextUtils.isEmpty(mHeadUri) || !TextUtils.isEmpty(mSubHead);
    }

    public boolean hasSubHead() {
        return mHeadType == 1;
    }

    public String getSubHead() {
        return mSubHead;
    }

    public boolean isNeedRemoved() {
        return mUiType == ChannelUiType.TYPE_BANNER || mUiType == ChannelUiType.TYPE_VIDEO_BANNER;
    }

    public boolean isHide() {
        return mIsHide;
    }

    public void setIsHide(boolean mIsHide) {
        this.mIsHide = mIsHide;
    }

    public void setImageSize(int width, int height) {
        if (mImageWidth == width && mImageHeight == height) {
            return;
        }
        mImageWidth = width;
        mImageHeight = height;

        if (mImageWidth != 0) {
            mRatio = 1f * mImageHeight / mImageWidth;
            mFrameHeight = (int) (mRatio * DisplayUtils.getPhoneWidth());
        } else {
            mFrameHeight = 0;
        }
    }

    public float getRatio() {
        return mRatio;
    }

    public int getFrameHeight() {
        return mFrameHeight;
    }

    public abstract boolean isNeedRemove();
}
