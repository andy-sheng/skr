package com.wali.live.watchsdk.channel.viewmodel;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.mi.live.data.user.User;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.CommonChannelProto.ChannelItem;
import com.wali.live.proto.CommonChannelProto.UiTemplateRanking;
import com.wali.live.watchsdk.R;

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
        parseUI(UiTemplateRanking.parseFrom(protoItem.getUiData()));
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
            title = GlobalData.app().getString(R.string.anchor_rank);
        }
    }

    public int getIconType() {
        return iconType;
    }

    private void parseUserItem(List<CommonChannelProto.RankingItemData> protoItemList) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        for (CommonChannelProto.RankingItemData protoItem : protoItemList) {
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
        private User mUser;

        protected boolean mIsFocused;

        public UserItemData(CommonChannelProto.RankingItemData protoItem) {
            parse(protoItem);
        }

        public UserItemData() {
            mUser = new User();
            mUser.setFansNum(10);
        }

        public void parse(CommonChannelProto.RankingItemData protoItem) {
            mUser = new User(protoItem.getUserInfo());
            mSchemeUri = protoItem.getJumpSchemeUri();
        }

        public User getUser() {
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
