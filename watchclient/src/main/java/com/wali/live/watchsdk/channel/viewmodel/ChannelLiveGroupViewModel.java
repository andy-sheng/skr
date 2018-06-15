package com.wali.live.watchsdk.channel.viewmodel;

import android.net.Uri;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.watchsdk.channel.helper.ModelHelper;
import com.wali.live.watchsdk.scheme.specific.SpecificConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道直播数据模型，包括直播，回放，用户，小视频
 */
public class ChannelLiveGroupViewModel extends ChannelViewModel<CommonChannelProto.ChannelItem> {
    private static final String TAG = ChannelLiveGroupViewModel.class.getSimpleName();
    private List<GroupDate> mItemDatas;

    /**
     * 私有构造函数用于构造测试数据
     */
    private ChannelLiveGroupViewModel(int uiType) {
        super();
        mUiType = uiType;
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
    }

    protected ChannelLiveGroupViewModel(CommonChannelProto.ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    public static ChannelLiveGroupViewModel newTestInstance(int uiType) {
        return new ChannelLiveGroupViewModel(uiType);
    }

    @Override
    protected void parseTemplate(CommonChannelProto.ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mFullColumn = protoItem.getFullColumn();
        mSectionId = protoItem.getSectionId();

        mIsHide = protoItem.getIsHide();
        parseUI(CommonChannelProto.UiTemplateGroup.parseFrom(protoItem.getUiData()));
    }

    private void parseUI(CommonChannelProto.UiTemplateGroup protoItem) throws Exception {
        mHead = protoItem.getHeaderName();
        mHeadUri = protoItem.getHeaderViewAllUri();
        mHeadType = protoItem.getHeaderUiType();
        mSubHead = protoItem.getSubHeaderName();
        mHeadIconUri = protoItem.getHeaderIcon();
        mHeaderViewAllText = protoItem.getHeaderViewAllText();
        generateEncodeHead();

        parseLive(protoItem.getItemsList());
    }

    private void parseLive(List<CommonChannelProto.GroupData> protoItemList) throws Exception {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }

        MyLog.d(TAG, "protoItemList " + protoItemList.size());
        for (CommonChannelProto.GroupData protoItem : protoItemList) {
            mItemDatas.add(new GroupDate(protoItem));
        }
    }

    public List<GroupDate> getItemDatas() {
        return mItemDatas;
    }

    @Override
    public boolean isNeedRemove() {
        if (mItemDatas != null && !CommonUtils.isAppInstalled(GlobalData.app(), CommonUtils.MI_VIDEO_PACKAGE)) {
            for (GroupDate item : mItemDatas) {
                if (item != null && !TextUtils.isEmpty(item.getJumpSchemeUri())) {
                    Uri uri = Uri.parse(item.getJumpSchemeUri());
                    if (uri != null && !TextUtils.isEmpty(uri.getScheme())
                            && SpecificConstants.SCHEME_MI_VIDEO.equals(uri.getScheme())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static class GroupDate {

        String frameUri; //边框

        String coverUri; //封面图

        String jumpSchemeUri; //点击封面后的跳转

        String[] liveCovers;

        int groupCnt; //分组人数

        public GroupDate(CommonChannelProto.GroupData groupData){
            frameUri = groupData.getFrameUri();
            coverUri = groupData.getCoverUri();
            jumpSchemeUri = groupData.getJumpSchemeUri();
            int liveCoverCount = groupData.getMemberCount();
            liveCovers = new String[liveCoverCount];
            for (int i = 0; i < liveCoverCount; i++){
                liveCovers[i] = groupData.getMember(i).getLiveCover();
            }
            groupCnt = groupData.getGroupCnt();
        }

        public String getFrameUri() {
            return frameUri;
        }

        public String getCoverUri() {
            return coverUri;
        }

        public String getJumpSchemeUri() {
            return jumpSchemeUri;
        }

        public String[] getLiveCovers() {
            return liveCovers;
        }

        public int getGroupCnt() {
            return groupCnt;
        }
    }
}