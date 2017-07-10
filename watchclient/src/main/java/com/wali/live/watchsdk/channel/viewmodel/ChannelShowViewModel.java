package com.wali.live.watchsdk.channel.viewmodel;

import android.net.Uri;
import android.text.TextUtils;

import com.mi.live.data.user.User;
import com.wali.live.proto.CommonChannelProto.ChannelItem;
import com.wali.live.proto.CommonChannelProto.OneTextOneImgItemData;
import com.wali.live.proto.CommonChannelProto.UiTemplateOneTextOneImg;
import com.wali.live.watchsdk.scheme.SchemeConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道一文一图数据模型
 */
public class ChannelShowViewModel extends ChannelViewModel<ChannelItem> {
    private List<OneTextItem> mItemDatas;

    protected ChannelShowViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mSectionId = protoItem.getSectionId();
        parseUI(UiTemplateOneTextOneImg.parseFrom(protoItem.getUiData()));
    }

    private void parseUI(UiTemplateOneTextOneImg protoItem) {
        mHead = protoItem.getHeaderName();
        mHeadUri = protoItem.getHeaderViewAllUri();

        parseOneItem(protoItem.getItemDatasList());
    }

    private void parseOneItem(List<OneTextOneImgItemData> protoOneItemList) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        for (OneTextOneImgItemData protoOneItem : protoOneItemList) {
            mItemDatas.add(new OneTextItem(protoOneItem));
        }
    }

    public List<OneTextItem> getItemDatas() {
        return mItemDatas;
    }

    public boolean hasHead() {
        return !TextUtils.isEmpty(mHead) || !TextUtils.isEmpty(mHeadUri);
    }

    @Override
    public boolean isNeedRemove() {
        for (OneTextItem item : mItemDatas) {
            if (TextUtils.isEmpty(item.getSchemeUri())) {
                return true;
            }
            Uri uri = Uri.parse(item.getSchemeUri());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!SchemeConstants.ALL_CHANNEL_SCHEME_TYPE.contains(scheme) ||
                    !SchemeConstants.ALL_CHANNEL_SCHEME_TYPE.contains(host)) {
                return true;
            }
        }
        return false;
    }

    public static class OneTextItem extends BaseJumpItem {
        private String mText;
        private String mImgUrl;
        private User mUser;

        public OneTextItem(OneTextOneImgItemData protoOneItem) {
            parse(protoOneItem);
        }

        public void parse(OneTextOneImgItemData protoOneItem) {
            mText = protoOneItem.getText();
            mImgUrl = protoOneItem.getImgUrl();
            mSchemeUri = protoOneItem.getJumpSchemeUri();
            mUser = new User(protoOneItem.getUserInfo());
        }

        public String getText() {
            return mText;
        }

        public String getImgUrl() {
            return mImgUrl;
        }

        public User getUser() {
            return mUser;
        }
    }
}
