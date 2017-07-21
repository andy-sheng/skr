package com.wali.live.watchsdk.channel.viewmodel;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.TestConstants;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.CommonChannelProto.BannerItemData;
import com.wali.live.proto.CommonChannelProto.ChannelItem;
import com.wali.live.proto.CommonChannelProto.UiTemplateBanner;
import com.wali.live.watchsdk.channel.util.BannerManger;
import com.wali.live.watchsdk.channel.util.Base64;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道广告位数据模型，对应服务器ChannelItem，UiType=6
 */
public class ChannelBannerViewModel extends ChannelViewModel<ChannelItem> {
    public static final String TAG = ChannelBannerViewModel.class.getSimpleName();

    private List<Banner> mItemDatas;
    private int mSizeType;
    private boolean mIsLargeSize;

    /**
     * 私有构造函数用于构造测试数据
     */
    private ChannelBannerViewModel() {
        super();
        mUiType = ChannelUiType.TYPE_BANNER;
        for (int i = 0; i < 3; i++) {
            addBanner(Banner.newTestInstance());
        }
    }

    protected ChannelBannerViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mSectionId = protoItem.getSectionId();
        parseUI(UiTemplateBanner.parseFrom(protoItem.getUiData()));
    }

    private void parseUI(UiTemplateBanner protoItem) {
        mSizeType = protoItem.getType();
        mIsLargeSize = mSizeType == 2;

        parseOneItem(protoItem.getItemDatasList());
    }

    private void parseOneItem(List<BannerItemData> protoList) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        for (BannerItemData protoItem : protoList) {
            mItemDatas.add(new Banner(protoItem));
        }
    }

    public List<Banner> getItemDatas() {
        return mItemDatas;
    }

    public boolean isLargeSize() {
        return mIsLargeSize;
    }

    public void addBanner(Banner banner) {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        mItemDatas.add(banner);
    }

    public void addBanner(BannerItemData protoItem) {
        if (protoItem == null) {
            MyLog.d(TAG, "addBanner protoBanner is null");
            return;
        }
        addBanner(new Banner(protoItem));
    }

    public static ChannelBannerViewModel newTestInstance() {
        return new ChannelBannerViewModel();
    }

    @Override
    public boolean isNeedRemove() {
        return false;
    }

    public static class Banner extends BaseJumpItem implements Serializable {
        private String mBgUrl;
        private String mLinkUrl;
        private String mDescription;

        // 注意这里有ChannelBanner BannerItemData有区别
        private long mLastUpdateTs;
        private int mBannerId;
        private String mShareIconUrl;
        private String mShareTitle;
        private int mChannelId;

        private String mEncodeKey;

        private BannerManger.BannerItem mBannerItem;

        private Banner() {
            mBgUrl = TestConstants.TEST_IMG_URL;
            mDescription = TestConstants.TEST_TITLE;
        }

        public Banner(CommonChannelProto.ChannelBanner protoBanner) {
            parse(protoBanner);
        }

        public Banner(CommonChannelProto.BannerItemData protoBanner) {
            parse(protoBanner);
        }

        public Banner(ChannelLiveViewModel.BaseItem item) throws Exception {
            parse(item);
        }

        public void parse(CommonChannelProto.ChannelBanner protoBanner) {
            if (protoBanner == null) {
                return;
            }
            mBgUrl = protoBanner.getBgUrl();
            mLinkUrl = protoBanner.getLinkUrl();
            mSchemeUri = protoBanner.getLinkUrl();
            mDescription = protoBanner.getDesc();

            generateEncodeKey();
        }

        public void parse(CommonChannelProto.BannerItemData protoBanner) {
            if (protoBanner == null) {
                return;
            }
            mBgUrl = protoBanner.getPicUrl();
            mLinkUrl = protoBanner.getSkipUrl();
            mSchemeUri = protoBanner.getSkipUrl();
            mDescription = protoBanner.getShareDesc();

            mLastUpdateTs = protoBanner.getLastUpdateTs();
            mBannerId = protoBanner.getBannerId();
            mShareIconUrl = protoBanner.getShareIconUrl();
            mShareTitle = protoBanner.getShareTitle();
            mChannelId = protoBanner.getChannelId();

            generateEncodeKey();
        }

        public void parse(ChannelLiveViewModel.BaseItem item) throws Exception {
            if (item == null) {
                throw new Exception("ChannelLiveViewModel BaseItem is null");
            }
            mBgUrl = item.getImageUrl();
            mLinkUrl = item.getSchemeUri();
            mSchemeUri = item.getSchemeUri();
            mDescription = item.getLineOneText();
        }

        protected void generateEncodeKey() {
            if (!TextUtils.isEmpty(mLinkUrl)) {
                try {
                    mEncodeKey = "banner_" + Base64.encode(mLinkUrl.getBytes("utf-8"));
                } catch (UnsupportedEncodingException e) {
                    MyLog.d(TAG, e);
                }
            }
        }

        public String getBgUrl() {
            return mBgUrl;
        }

        public String getLinkUrl() {
            return mLinkUrl;
        }

        public String getDescription() {
            return mDescription;
        }

        public String getEncodeKey() {
            return mEncodeKey;
        }

        public BannerManger.BannerItem toBannerItem() {
            if (mBannerItem == null) {
                mBannerItem = new BannerManger.BannerItem();
                mBannerItem.picUrl = mBgUrl;
                mBannerItem.skipUrl = mLinkUrl;
                mBannerItem.shareDesc = mDescription;

                mBannerItem.lastUpdateTs = mLastUpdateTs;
                mBannerItem.bannerId = mBannerId;
                mBannerItem.shareIconUrl = mShareIconUrl;
                mBannerItem.shareTitle = mShareTitle;
                mBannerItem.channelId = mChannelId;
            }
            return mBannerItem;
        }

        protected static Banner newTestInstance() {
            return new Banner();
        }
    }
}
