package com.wali.live.modulechannel.model.viewmodel;

import android.text.TextUtils;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfo;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.wali.live.proto.CommonChannel.ChannelItem;
import com.wali.live.proto.CommonChannel.TwoTextOneImgItemData;
import com.wali.live.proto.CommonChannel.UiTemplateTwoTextOneImg;

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
        parseUI(UiTemplateTwoTextOneImg.parseFrom(protoItem.getUiData().toByteArray()));
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
        private UserInfo mUser;

        public TwoLineItem(TwoTextOneImgItemData protoItem) {
            parse(protoItem);
        }

        public void parse(TwoTextOneImgItemData protoItem) {
            mName = protoItem.getName();
            mDesc = protoItem.getDesc();
            mImgUrl = protoItem.getImgUrl();
            mSchemeUri = protoItem.getJumpSchemeUri();
            com.wali.live.proto.CommonChannel.UserInfo userInfo = protoItem.getUserInfo();
            mUser = buildBriefUser(userInfo);
        }

        public String getName() {
            return mName;
        }

        public String getDesc() {
            return mDesc;
        }

        public String getImgUrl() {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl + U.getImageUtils().getSizeSuffix(ImageUtils.SIZE.SIZE_480);
            } else if (mUser != null) {
                return AvatarUtils.getAvatarUrlByCustom(mUser.getUserId(), ImageUtils.SIZE.SIZE_480, mUser.getAvatar(), false);
            }
            return null;
        }

        public UserInfo getUser() {
            return mUser;
        }
    }
}
