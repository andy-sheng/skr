package com.wali.live.modulechannel.adapter.holder;

import com.common.core.userinfo.UserInfo;
import com.wali.live.modulechannel.model.viewmodel.BaseJumpItem;
import com.wali.live.modulechannel.model.viewmodel.ChannelUiType;
import com.wali.live.modulechannel.model.viewmodel.ChannelViewModel;
import com.wali.live.proto.CommonChannel.ChannelItem;
import com.wali.live.proto.CommonChannel.UiTemplateUserInfo;
import com.wali.live.proto.CommonChannel.UserInfoItemData;

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

    public ChannelUserViewModel(ChannelItem protoItem) throws Exception {
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
        parseUI(UiTemplateUserInfo.parseFrom(protoItem.getUiData().toByteArray()));
    }

    private void parseUI(UiTemplateUserInfo protoItem) {
        mHead = protoItem.getHeaderName();
        mHeadUri = protoItem.getHeaderViewAllUri();
        mHeadType = protoItem.getHeaderUiType2();
        mHeadIconUri = protoItem.getHeaderIcon();
        mHeaderViewAllText = protoItem.getHeaderViewAllText();

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
        private UserInfo mUser;

        protected boolean mIsFocused;
        protected String mDescText;
        protected String mDescTextColor;
        protected String mAvatarLayerColor;

        public UserItemData(UserInfoItemData protoItem) {
            parse(protoItem);
        }

        public UserItemData() {
            mUser = new UserInfo();
        }

        public void parse(UserInfoItemData protoItem) {
            mUser = buildBriefUser(protoItem.getUserInfo());
            mSchemeUri = protoItem.getJumpSchemeUri();
            mDescText = protoItem.getDesc();
            mDescTextColor = protoItem.getDescBgColor();
            mAvatarLayerColor = protoItem.getAvatarLayerColor();
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
