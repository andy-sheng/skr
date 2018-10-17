package com.wali.live.modulechannel.model.viewmodel;

import android.support.annotation.Keep;
import android.text.TextUtils;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.UserInfo;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.google.protobuf.GeneratedMessage;
import com.wali.live.proto.CommonChannel.BackInfo;
import com.wali.live.proto.CommonChannel.ChannelItem;
import com.wali.live.proto.CommonChannel.LiveInfo;
import com.wali.live.proto.CommonChannel.LiveOrReplayItemInfo;
import com.wali.live.proto.CommonChannel.ShopBrief;
import com.wali.live.proto.CommonChannel.UiTemplateLiveOrReplayInfo;
import com.wali.live.proto.CommonChannel.UserBrief;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道直播数据模型，包括直播，回放，用户，小视频
 */
@Keep
public class ChannelLiveViewModel extends ChannelViewModel<ChannelItem> {
    private static final String TAG = ChannelLiveViewModel.class.getSimpleName();
    private List<BaseItem> mItemDatas;

    private boolean mThreeCardTwoLine;

    /**
     * 私有构造函数用于构造测试数据
     */
    private ChannelLiveViewModel(int uiType) {
        super();
        mUiType = uiType;
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }
        mItemDatas.add(BaseItem.newTestInstance(BaseItem.ITEM_TYPE_LIVE));
    }

    protected ChannelLiveViewModel(ChannelItem protoItem) throws Exception {
        super(protoItem);
    }

    @Override
    protected void parseTemplate(ChannelItem protoItem) throws Exception {
        mUiType = protoItem.getUiType();
        mFullColumn = protoItem.getFullColumn();

        mThreeCardTwoLine = mUiType == ChannelUiType.TYPE_THREE_CARD;
        parseUI(UiTemplateLiveOrReplayInfo.parseFrom(protoItem.getUiData().toByteArray()));
    }

    private void parseUI(UiTemplateLiveOrReplayInfo protoItem) throws Exception {
        parseLive(protoItem.getItemsList());
    }

    private void parseLive(List<LiveOrReplayItemInfo> protoItemList) throws Exception {
        if (mItemDatas == null) {
            mItemDatas = new ArrayList<>();
        }

        for (LiveOrReplayItemInfo protoItem : protoItemList) {
            if (protoItem.getType() == BaseItem.ITEM_TYPE_LIVE) {
                mItemDatas.add(new LiveItem(protoItem));
            } else if (protoItem.getType() == BaseItem.ITEM_TYPE_BACK) {
                mItemDatas.add(new BackItem(protoItem));
            } else {
                throw new Exception("ChannelLiveViewModel parseLive unknown type=" + protoItem.getType());
            }
        }
    }

    public boolean isThreeCardTwoLine() {
        return mThreeCardTwoLine;
    }

    public List<BaseItem> getItemDatas() {
        return mItemDatas;
    }

    public BaseItem getFirstItem() {
        if (mItemDatas == null || mItemDatas.size() == 0) {
            return null;
        }
        return mItemDatas.get(0);
    }

    public static ChannelLiveViewModel newTestInstance(int uiType) {
        return new ChannelLiveViewModel(uiType);
    }

    public static abstract class BaseItem<GM extends GeneratedMessage> extends BaseJumpItem {
        public static final int ITEM_TYPE_LIVE = 1;
        public static final int ITEM_TYPE_BACK = 2;
        public static final int ITEM_TYPE_USER = 3;
        public static final int ITEM_TYPE_VIDEO = 4;
        public static final int ITEM_TYPE_TV = 5;
        public static final int ITEM_TYPE_SIMPLE = 6;

        // 共有的
        protected int mType;
        protected String mUpRightText;
        protected String mLineOneText;
        protected String mLineTwoText;
        protected String mImgUrl;
        protected long mPublishTime;

        protected UserInfo mUser;
        protected boolean mIsFocused;

        protected BaseItem() {
        }

        public BaseItem(LiveOrReplayItemInfo protoItem) {
            mType = protoItem.getType();
            mSchemeUri = protoItem.getJumpSchemeUri();
            mUpRightText = protoItem.getUpRightText();
            mLineOneText = protoItem.getDownText1();
            mLineTwoText = protoItem.getDownText2();
            mImgUrl = protoItem.getImgUrl();
            mPublishTime = protoItem.getPublishTime();
        }

        public int getType() {
            return mType;
        }

        public String getUpRightText() {
            return mUpRightText;
        }

        // 可以考虑用getNameText代替
        public String getLineOneText() {
            return mLineOneText;
        }

        // 可应考虑getDisplayText代替
        public String getLineTwoText() {
            return mLineTwoText;
        }

        public long getPublishTime() {
            return mPublishTime;
        }

        public UserInfo getUser() {
            return mUser;
        }

        public abstract String getImageUrl();

        public abstract String getImageUrl(ImageUtils.SIZE sizeType);

        public abstract String getNameText();

        public abstract String getDisplayText();

        public boolean getIsFocus() {
            return mIsFocused;
        }

        public void setIsFocus(boolean isFocused) {
            mIsFocused = isFocused;
        }

        public static BaseItem newTestInstance(int type) {
            if (type == BaseItem.ITEM_TYPE_LIVE) {
                return LiveItem.newTestInstance();
            } else if (type == BaseItem.ITEM_TYPE_BACK) {
                return BackItem.newTestInstance();
            }
            return null;
        }
    }

    @Keep
    public static class BaseLiveItem extends BaseItem {
        // LiveInfo & BackInfo共有的
        protected String mId;
        protected String mTitle;
        protected int mViewerCnt;
        protected String mVideoUrl;
        protected String mCoverUrl;
        protected String mLocation;

        protected boolean mIsEnterRoom;
        protected String mCountString;

        protected BaseLiveItem() {
            super();
        }

        public BaseLiveItem(LiveOrReplayItemInfo protoItem) {
            super(protoItem);
        }

        public String getId() {
            return mId;
        }

        public String getTitle() {
            return mTitle;
        }

        public int getViewerCnt() {
            return mViewerCnt;
        }

        public String getVideoUrl() {
            return mVideoUrl;
        }

        public boolean isEnterRoom() {
            return mIsEnterRoom;
        }

        public String getCountString() {
            return mCountString;
        }

        // 用getImageUrl
        protected String getCoverUrl() {
            if (TextUtils.isEmpty(mCoverUrl)) {
                MyLog.w(TAG, "getCoverUrl coverUrl is Empty, getHead=" + getTitle());
                return null;
            }
            return mCoverUrl + U.getImageUtils().getSizeSuffix(ImageUtils.SIZE.SIZE_160);
        }

        public String getLocation() {
            return mLocation;
        }

        @Override
        public String getImageUrl() {
            return getImageUrl(ImageUtils.SIZE.SIZE_480);
        }

        @Override
        public String getImageUrl(ImageUtils.SIZE sizeType) {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl + U.getImageUtils().getSizeSuffix(sizeType);
            } else if (!TextUtils.isEmpty(mCoverUrl)) {
                return mCoverUrl + U.getImageUtils().getSizeSuffix(sizeType);
            } else {
                return AvatarUtils.getAvatarUrl(mUser.getUid(), sizeType, mUser.getAvatar());
            }
        }

        @Override
        public String getNameText() {
            if (!TextUtils.isEmpty(mLineOneText)) {
                return mLineOneText;
            } else {
                return mUser.getNickName();
            }
        }

        public String getTitleText() {
            if (!TextUtils.isEmpty(mLineOneText)) {
                return mLineOneText;
            } else {
                return mTitle;
            }
        }

        @Override
        public String getDisplayText() {
            return mLineTwoText;
        }
    }

    public static class LiveItem extends BaseLiveItem {
        private static final int LIVE_TYPE_TICKET = 3;

        private String mLocation;
        private long mStartTime;

        private boolean mShowShop;
        private int mShopCnt;

        private int mAppType = 0;
        private int mLiveType = 0;

        private LiveItem() {
            super();
        }

        public LiveItem(LiveOrReplayItemInfo protoItem) throws Exception {
            super(protoItem);
            parse(LiveInfo.parseFrom(protoItem.getItems().toByteArray()));
        }

        public void parse(LiveInfo protoItem) {
            mId = protoItem.getLiveId();
            mUser = parseUserInfo(protoItem.getUser());

            mTitle = protoItem.getLiTitle();
            mViewerCnt = protoItem.getViewerCnt();
            mVideoUrl = protoItem.getUrl();
            mCoverUrl = protoItem.getCoverUrl();

            mLocation = protoItem.getLocation();
            mStartTime = protoItem.getStartTime();

            mAppType = protoItem.getAppType();
            mLiveType = protoItem.getLiveType();

            if (protoItem.hasShop()) {
                parseShop(protoItem.getShop());
            }
        }

        private UserInfo parseUserInfo(UserBrief protoUser) {
            if (protoUser == null) {
                return null;
            }

            UserInfo userInfo = new UserInfo();
            userInfo.setUid(protoUser.getUId());
            userInfo.setAvatar(protoUser.getAvatar());
            userInfo.setLevel(protoUser.getLevel());
            userInfo.setCertificationType(protoUser.getCertType());

            return userInfo;
        }

        public boolean isYiZhiBo() {
            return mAppType == 4;
        }

        public boolean isTicket() {
            return mLiveType == LIVE_TYPE_TICKET;
        }

        public void parseShop(ShopBrief shopBrief) {
            if (shopBrief == null) {
                return;
            }
            mShowShop = true;
            mShopCnt = shopBrief.getDesiredCnt();
        }

        public String getLocation() {
            return mLocation;
        }

        public long getStartTime() {
            return mStartTime;
        }

        private String getLiveCoverUrl() {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl;
            } else if (!TextUtils.isEmpty(mCoverUrl)) {
                return mCoverUrl;
            } else {
                return AvatarUtils.getAvatarUrl(mUser.getUid(), ImageUtils.SIZE.SIZE_480, mUser.getAvatar());
            }
        }

        public boolean isShowShop() {
            return mShowShop;
        }

        public int getShopCnt() {
            return mShopCnt;
        }

        public static LiveItem newTestInstance() {
            return new LiveItem();
        }
    }



    public static class BackItem extends BaseLiveItem {
        private long mStartTime;
        private long mEndTime;
        private String mShareUrl;

        private BackItem() {
            super();
        }

        public BackItem(LiveOrReplayItemInfo protoItem) throws Exception {
            super(protoItem);
            parse(BackInfo.parseFrom(protoItem.getItems().toByteArray()));
        }

        public void parse(BackInfo protoItem) {
            mId = protoItem.getBackId();
            UserBrief userBrief = protoItem.getUser();
            mUser = parseUserInfo(userBrief);

            mTitle = protoItem.getBackTitle();
            mViewerCnt = protoItem.getViewerCnt();
            mVideoUrl = protoItem.getUrl();
            mCoverUrl = protoItem.getCoverUrl();
            mLocation = protoItem.getLocation();

            mStartTime = protoItem.getStartTime();
            mEndTime = protoItem.getEndTime();
            mShareUrl = protoItem.getShareUrl();
        }

        public long getStartTime() {
            return mStartTime;
        }

        public long getEndTime() {
            return mEndTime;
        }

        public String getShareUrl() {
            return mShareUrl;
        }

        public static BackItem newTestInstance() {
            return new BackItem();
        }

        private UserInfo parseUserInfo(UserBrief protoUser) {
            if (protoUser == null) {
                return null;
            }

            UserInfo userInfo = new UserInfo();
            userInfo.setUid(protoUser.getUId());
            userInfo.setAvatar(protoUser.getAvatar());
            userInfo.setLevel(protoUser.getLevel());
            userInfo.setCertificationType(protoUser.getCertType());

            return userInfo;
        }
    }
}
