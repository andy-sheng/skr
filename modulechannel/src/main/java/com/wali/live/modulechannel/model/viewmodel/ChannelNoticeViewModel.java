package com.wali.live.modulechannel.model.viewmodel;

import android.text.TextUtils;

import com.wali.live.proto.CommonChannel.ChannelItem;
import com.wali.live.proto.CommonChannel.NoticeData;
import com.wali.live.proto.CommonChannel.UiTemplateNotice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public class ChannelNoticeViewModel extends ChannelViewModel<ChannelItem> {
    private List<NoticeItem> mItemDatas;

    private String mAllViewUri;
    private String mRecommendTag;
    private boolean mIsExposured;

    protected ChannelNoticeViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    public ChannelNoticeViewModel() {
        mUiType = ChannelUiType.TYPE_NOTICE_SCROLL;
        if (mItemDatas == null) {
            mItemDatas = new ArrayList();
        }
        for (int i = 0; i < 3; i++) {
            mItemDatas.add(new NoticeItem(i));
        }
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mSectionId = protoItem.getSectionId();
        parseUI(UiTemplateNotice.parseFrom(protoItem.getUiData().toByteArray()));
    }

    private void parseUI(UiTemplateNotice protoItem) {
        parseItem(protoItem.getNoticeItemsList());
        mAllViewUri = protoItem.getHeaderViewAllUri();
    }

    private void parseItem(List<NoticeData> protoList) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList();
        }
        for (NoticeData protoItem : protoList) {
            mItemDatas.add(new NoticeItem(protoItem));
        }
    }

    public List<NoticeItem> getItemDatas() {
        return mItemDatas;
    }

    public String getAllViewUri() {
        return mAllViewUri;
    }

    public String getRecommendTag() {

        //TOdo-暂时去除
//        if (mRecommendTag == null) {
//            mRecommendTag = SchemeUtils.getRecommendTag(mAllViewUri);
//        }
//        return mRecommendTag;
        return null;
    }

    public boolean isExposured() {
        return mIsExposured;
    }

    public void setIsExposured(boolean mIsExposured) {
        this.mIsExposured = mIsExposured;
    }

    public boolean hasHead() {
        return !TextUtils.isEmpty(mHead) || !TextUtils.isEmpty(mHeadUri);
    }

    @Override
    public boolean isNeedRemove() {
        return false;
    }

    public static class NoticeItem implements Serializable {
        private String mId;
        private long mUid;
        private long mBeginTime;
        private String mTitle;

        public NoticeItem(NoticeData protoItem) {
            parse(protoItem);
        }

        public NoticeItem(int i) {
            mBeginTime = System.currentTimeMillis();
            mTitle = "index=" + i;
        }

        public void parse(NoticeData protoItem) {
            mId = protoItem.getNoticeId();
            mUid = protoItem.getZuid();
            mBeginTime = protoItem.getBeginTime();
            mTitle = protoItem.getTitle();
        }

        public String getId() {
            return mId;
        }

        public long getUid() {
            return mUid;
        }

        public long getBeginTime() {
            return mBeginTime;
        }

        public String getTitle() {
            return mTitle;
        }

    }
}
