package com.wali.live.modulechannel.model.viewmodel;

import android.text.TextUtils;

import com.common.core.userinfo.UserInfo;
import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.proto.CommonChannel.ChannelItem;
import com.wali.live.proto.CommonChannel.RankingItemData;
import com.wali.live.proto.CommonChannel.UiTemplateRanking;

import java.util.ArrayList;
import java.util.List;

/**
 * @module 频道
 * @description
 */
public class ChannelRankingViewModel extends ChannelViewModel<ChannelItem> {
    private List<UserItemData> mItemDatas;
    private int iconType;
    private String schemeUri;
    private String title;

    protected ChannelRankingViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    public ChannelRankingViewModel() {
        super();
        mUiType = ChannelUiType.TYPE_PLACEHOLDER_RANK;
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        mItemDatas.add(new UserItemData());
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        parseUI(UiTemplateRanking.parseFrom(protoItem.getUiData().toByteArray()));
    }

    private void parseUI(UiTemplateRanking protoItem) {
//        mHead = protoItem.getHeaderName();
//        mHeadUri = protoItem.getHeaderViewAllUri();

        parseUserItem(protoItem.getItemDatasList());

        iconType = protoItem.getIconStyle();

        schemeUri = protoItem.getJumpSchemeUri();

        title = protoItem.getText1();

        if (TextUtils.isEmpty(schemeUri)) {
            schemeUri = "";
        }

        if (TextUtils.isEmpty(title)) {
            title = U.app().getString(R.string.channel_anchor_rank);
        }
    }

    public int getIconType() {
        return iconType;
    }

    private void parseUserItem(List<RankingItemData> protoItemList) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        for (RankingItemData protoItem : protoItemList) {
            mItemDatas.add(new UserItemData(protoItem));
        }
    }

    public List<UserItemData> getItemDatas() {
        return mItemDatas;
    }

    public String getSchemeUri() {
        return schemeUri;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean isNeedRemove() {
        return false;
    }

    public static class UserItemData extends BaseJumpItem {
        private UserInfo mUser;

        protected boolean mIsFocused;

        public UserItemData(RankingItemData protoItem) {
            parse(protoItem);
        }

        public UserItemData() {
            mUser = new UserInfo();
//            mUser.setFansNum(10);
        }

        public void parse(RankingItemData protoItem) {
            mUser = buildBriefUser(protoItem.getUserInfo());
            mSchemeUri = protoItem.getJumpSchemeUri();
        }

        public UserInfo getUser() {
            return mUser;
        }

        public boolean isFocused() {
            return mIsFocused;
        }

        public void setFocused(boolean focused) {
            mIsFocused = focused;
        }
    }
}
