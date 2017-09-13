package com.wali.live.watchsdk.channel.viewmodel;

import com.mi.live.data.user.User;
import com.wali.live.proto.CommonChannelProto.ChannelItem;
import com.wali.live.proto.CommonChannelProto.UiTemplateUserInfo;
import com.wali.live.proto.CommonChannelProto.UserInfoItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道用户数据模型
 */
public class ChannelUserViewModel extends ChannelViewModel<ChannelItem> {
    private List<UserItemData> mItemDatas;

    protected ChannelUserViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    public ChannelUserViewModel() {
        super();
        mUiType = ChannelUiType.TYPE_THREE_CONCERN_CARD;
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        mItemDatas.add(new UserItemData());
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mSectionId = protoItem.getSectionId();
        parseUI(UiTemplateUserInfo.parseFrom(protoItem.getUiData()));
    }

    private void parseUI(UiTemplateUserInfo protoItem) {
        mHead = protoItem.getHeaderName();
        mHeadUri = protoItem.getHeaderViewAllUri();
        mHeadType = protoItem.getHeaderUiType();
        mHeadIconUrl = protoItem.getHeaderIcon();
        mHeadMoreText = protoItem.getHeaderViewAllText();

        parseUserItem(protoItem.getItemDatasList());
    }

    private void parseUserItem(List<UserInfoItemData> protoItemList) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        for (UserInfoItemData protoItem : protoItemList) {
            mItemDatas.add(new UserItemData(protoItem));
        }
    }

    public List<UserItemData> getItemDatas() {
        return mItemDatas;
    }

    @Override
    public boolean isNeedRemove() {
        return false;
    }

    public static class UserItemData extends BaseJumpItem {
        private User mUser;

        protected boolean mIsFocused;
        protected String mDescText;
        protected String mDescTextColor;
        protected String mAvatarLayerColor;

        public UserItemData(UserInfoItemData protoItem) {
            parse(protoItem);
        }

        public UserItemData() {
            mUser = new User();
            mUser.setFansNum(10);
        }

        public void parse(UserInfoItemData protoItem) {
            mUser = new User(protoItem.getUserInfo());
            mSchemeUri = protoItem.getJumpSchemeUri();
            mDescText = protoItem.getDesc();
            mDescTextColor = protoItem.getDescBgColor();
            mAvatarLayerColor = protoItem.getAvatarLayerColor();
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

        public String getDescText() {
            return mDescText;
        }

        public String getDescTextColor() {
            return mDescTextColor;
        }

        public String getAvatarLayerColor() {
            return mAvatarLayerColor;
        }
    }
}
