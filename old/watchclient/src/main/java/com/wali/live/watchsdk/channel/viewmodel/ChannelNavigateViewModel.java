package com.wali.live.watchsdk.channel.viewmodel;

import android.text.TextUtils;

import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.CommonChannelProto.ChannelItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道一文一图数据模型
 */
public class ChannelNavigateViewModel extends ChannelViewModel<ChannelItem> {
    protected List<NavigateItem> mItemDatas;

    /**
     * 私有构造函数用于构造测试数据
     */
    private ChannelNavigateViewModel(int uiType) {
        super();
        mUiType = uiType;
        if (mItemDatas == null) {
            mItemDatas = new ArrayList();
        }
        mItemDatas.add(NavigateItem.newTestInstance());
        mItemDatas.add(NavigateItem.newTestInstance());
        mItemDatas.add(NavigateItem.newTestInstance());
        mItemDatas.add(NavigateItem.newTestInstance());
        mItemDatas.add(NavigateItem.newTestInstance());
        mItemDatas.add(NavigateItem.newTestInstance());
        mItemDatas.add(NavigateItem.newTestInstance());
    }

    protected ChannelNavigateViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mSectionId = protoItem.getSectionId();
        parseUI(CommonChannelProto.UiTemplateNavigation.parseFrom(protoItem.getUiData()));
    }

    private void parseUI(CommonChannelProto.UiTemplateNavigation protoItem) {
        parseItem(protoItem.getItemDatasList());
    }

    protected void parseItem(List<CommonChannelProto.NavigationData> protoOneItemList) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList();
        }
        for (CommonChannelProto.NavigationData protoItem : protoOneItemList) {
            mItemDatas.add(new NavigateItem(protoItem));
        }
    }

    public List<NavigateItem> getItemDatas() {
        return mItemDatas;
    }

    public boolean hasHead() {
        return !TextUtils.isEmpty(mHead) || !TextUtils.isEmpty(mHeadUri);
    }

    @Override
    public boolean isNeedRemove() {
        return false;
    }

    public static ChannelNavigateViewModel newTestInstance(int uiType) {
        return new ChannelNavigateViewModel(uiType);
    }

    public static class NavigateItem extends BaseJumpItem {
        private String mText;
        private String mImgUrl;
        private String mIconUrl;
        private int mTextColorType;
        private String mHexColorCode;
        private String mSubText;

        private NavigateItem() {
            super();
            mText = "#TEST#";
        }

        public NavigateItem(CommonChannelProto.NavigationData protoItem) {
            parse(protoItem);
        }

        public void parse(CommonChannelProto.NavigationData protoItem) {
            mText = protoItem.getName();
            mImgUrl = protoItem.getBgImgUrl();
            mIconUrl = protoItem.getIconUrl();
            mSchemeUri = protoItem.getJumpSchemeUri();
            mTextColorType = protoItem.getTextColor();
            mHexColorCode = protoItem.getHexColorCode();
            mSubText = protoItem.getText1();
        }

        public String getText() {
            return mText;
        }

        public String getImgUrl() {
            return mImgUrl;
        }

        public String getIconUrl() {
            return mIconUrl;
        }

        public int getTextColorType() {
            return mTextColorType;
        }

        public String getHexColorCode() {
            return mHexColorCode;
        }

        public String getSubText() {
            return mSubText;
        }

        public static NavigateItem newTestInstance() {
            return new NavigateItem();
        }
    }
}
