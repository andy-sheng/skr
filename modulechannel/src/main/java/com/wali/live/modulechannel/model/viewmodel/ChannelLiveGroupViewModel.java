package com.wali.live.modulechannel.model.viewmodel;

import android.net.Uri;
import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;
import com.wali.live.proto.CommonChannel.ChannelItem;
import com.wali.live.proto.CommonChannel.GroupData;
import com.wali.live.proto.CommonChannel.GroupMemberData;
import com.wali.live.proto.CommonChannel.UiTemplateGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道直播数据模型，包括直播，回放，用户，小视频
 */
public class ChannelLiveGroupViewModel extends ChannelViewModel<ChannelItem> {
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

    protected ChannelLiveGroupViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    public static ChannelLiveGroupViewModel newTestInstance(int uiType) {
        return new ChannelLiveGroupViewModel(uiType);
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mFullColumn = protoItem.getFullColumn();
        mSectionId = protoItem.getSectionId();

        mIsHide = protoItem.getIsHide();
        parseUI(UiTemplateGroup.parseFrom(protoItem.getUiData().toByteArray()));
    }

    private void parseUI(UiTemplateGroup protoItem) throws Exception {
        mHead = protoItem.getHeaderName();
        mHeadUri = protoItem.getHeaderViewAllUri();
        mHeadType = protoItem.getHeaderUiType();
        mSubHead = protoItem.getSubHeaderName();
        mHeadIconUri = protoItem.getHeaderIcon();
        mHeaderViewAllText = protoItem.getHeaderViewAllText();
        generateEncodeHead();

        parseLive(protoItem.getItemsList());
    }

    private void parseLive(List<GroupData> protoItemList) throws Exception {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }

        MyLog.d(TAG, "protoItemList " + protoItemList.size());
        for (GroupData protoItem : protoItemList) {
            mItemDatas.add(new GroupDate(protoItem));
        }
    }

    public List<GroupDate> getItemDatas() {
        return mItemDatas;
    }

    @Override
    public boolean isNeedRemove() {
        //TOdo-暂时注释了
//        if (mItemDatas != null && !U.getCommonUtils().isAppInstalled(U.app(), "com.miui.video")) {
//            for (GroupDate item : mItemDatas) {
//                if (item != null && !TextUtils.isEmpty(item.getSchemeUri())) {
//                    Uri uri = Uri.parse(item.getSchemeUri());
//                    if (uri != null && !TextUtils.isEmpty(uri.getScheme())
//                            && SpecificConstants.SCHEME_MI_VIDEO.equals(uri.getScheme())) {
//                        return true;
//                    }
//                }
//            }
//        }
        return false;
    }

    public static class GroupDate extends BaseJumpItem{

        String frameUri; //边框

        String coverUri; //封面图

        String[] liveCovers;

        int groupCnt; //分组人数

        public GroupDate(GroupData groupData){
            frameUri = groupData.getFrameUri();
            coverUri = groupData.getCoverUri();
            mSchemeUri = groupData.getJumpSchemeUri();
            List<GroupMemberData> memberList = groupData.getMemberList();
            if(memberList != null
                    && !memberList.isEmpty()) {
                int liveCoverCount = groupData.getMemberList().size();
                liveCovers = new String[liveCoverCount];
                for (int i = 0; i < liveCoverCount; i++){
                    liveCovers[i] = memberList.get(i).getLiveCover();
                }
            }

            groupCnt = groupData.getGroupCnt();
        }

        public String getFrameUri() {
            return frameUri;
        }

        public String getCoverUri() {
            return coverUri;
        }

        public String[] getLiveCovers() {
            return liveCovers;
        }

        public int getGroupCnt() {
            return groupCnt;
        }
    }
}