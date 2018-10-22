package com.wali.live.modulechannel.model.viewmodel;

import android.text.TextUtils;

import com.wali.live.proto.CommonChannel.ChannelItem;
import com.wali.live.proto.CommonChannel.UiTemplatePageHeader;


/**
 * Created by zyh on 2017/8/29.
 *
 * @module 世姐header ViewModel
 */
public class ChannelPageHeaderViewModel extends ChannelNavigateViewModel {
    private String mCoverUrl;         //封面
    private String mCoverSchemeUri;   //点击封面跳转scheme
    private String mVideoUrl;         //视频播放地址
    private String mVideoCoverUrl;    //视频封面地址
    private String mVideoSchemeUri;   //点击视频区域跳转scheme

    public ChannelPageHeaderViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mSectionId = protoItem.getSectionId();
        parsePageHeader(UiTemplatePageHeader.parseFrom(protoItem.getUiData().toByteArray()));
    }

    protected void parsePageHeader(UiTemplatePageHeader header) {
        mCoverUrl = header.getCoverUrl();
        mCoverSchemeUri = header.getCoverSchemeUri();
        mVideoUrl = header.getVideoUrl();
        mVideoCoverUrl = header.getVideoCoverUrl();
        mVideoSchemeUri = header.getVideoSchemeUri();
        parseItem(header.getNaviDataList());
    }

    @Override
    public boolean isNeedRemove() {
        if (TextUtils.isEmpty(mCoverUrl)) {
            return true;
        }
        return false;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public String getCoverSchemeUri() {
        return mCoverSchemeUri;
    }

    public String getVideoUrl() {
        return mVideoUrl;
    }

    public String getVideoCoverUrl() {
        return mVideoCoverUrl;
    }

    public String getVideoSchemeUri() {
        return mVideoSchemeUri;
    }
}
