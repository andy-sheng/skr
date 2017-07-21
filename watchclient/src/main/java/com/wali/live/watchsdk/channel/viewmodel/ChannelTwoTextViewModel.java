package com.wali.live.watchsdk.channel.viewmodel;

import android.text.TextUtils;

import com.mi.live.data.user.User;
import com.wali.live.proto.CommonChannelProto.ChannelItem;
import com.wali.live.proto.CommonChannelProto.TwoTextOneImgItemData;
import com.wali.live.proto.CommonChannelProto.UiTemplateTwoTextOneImg;
import com.wali.live.utils.AvatarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道双文字，单图片数据模型
 */
public class ChannelTwoTextViewModel extends ChannelViewModel<ChannelItem> {
    private List<TwoLineItem> mItemDatas;

    public ChannelTwoTextViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mSectionId = protoItem.getSectionId();
        parseUI(UiTemplateTwoTextOneImg.parseFrom(protoItem.getUiData()));
    }

    private void parseUI(UiTemplateTwoTextOneImg protoItem) {
        mHead = protoItem.getHeaderName();
        mHeadUri = protoItem.getHeaderViewAllUri();

        parseTwoLine(protoItem.getItemDatasList());
    }

    private void parseTwoLine(List<TwoTextOneImgItemData> protoItemList) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        for (TwoTextOneImgItemData protoItem : protoItemList) {
            mItemDatas.add(new TwoLineItem(protoItem));
        }
    }

    public List<TwoLineItem> getItemDatas() {
        return mItemDatas;
    }

    @Override
    public boolean isNeedRemove() {
        return false;
    }

    public static class TwoLineItem extends BaseJumpItem {
        private String mName;
        private String mDesc;
        private String mImgUrl;
        private User mUser;

        public TwoLineItem(TwoTextOneImgItemData protoItem) {
            parse(protoItem);
        }

        public void parse(TwoTextOneImgItemData protoItem) {
            mName = protoItem.getName();
            mDesc = protoItem.getDesc();
            mImgUrl = protoItem.getImgUrl();
            mSchemeUri = protoItem.getJumpSchemeUri();
            mUser = new User(protoItem.getUserInfo());
        }

        public String getName() {
            return mName;
        }

        public String getDesc() {
            return mDesc;
        }

        public String getImgUrl() {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl + AvatarUtils.getAvatarSizeAppend(AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE);
            } else if (mUser != null) {
                return AvatarUtils.getAvatarUrlByUidTsAndFormat(mUser.getUid(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, mUser.getAvatar(), false);
            }
            return null;
        }

        public User getUser() {
            return mUser;
        }
    }
}
