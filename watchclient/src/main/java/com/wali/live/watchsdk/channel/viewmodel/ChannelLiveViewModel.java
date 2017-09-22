package com.wali.live.watchsdk.channel.viewmodel;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.TestConstants;
import com.google.protobuf.GeneratedMessage;
import com.mi.live.data.user.User;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.CommonChannelProto.BackInfo;
import com.wali.live.proto.CommonChannelProto.ChannelItem;
import com.wali.live.proto.CommonChannelProto.LiveInfo;
import com.wali.live.proto.CommonChannelProto.LiveOrReplayItemInfo;
import com.wali.live.proto.CommonChannelProto.UiTemplateLiveOrReplayInfo;
import com.wali.live.proto.CommonChannelProto.UserInfo;
import com.wali.live.proto.CommonChannelProto.VideoInfo;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.helper.ModelHelper;
import com.wali.live.watchsdk.channel.util.BannerManger;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道直播数据模型，包括直播，回放，用户，小视频
 */
public class ChannelLiveViewModel extends ChannelViewModel<ChannelItem> {
    private static final String TAG = ChannelLiveViewModel.class.getSimpleName();
    private List<BaseItem> mItemDatas;

    private List<ChannelBannerViewModel.Banner> mBannerItems;

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
        mSectionId = protoItem.getSectionId();
        mThreeCardTwoLine = mUiType == ChannelUiType.TYPE_THREE_CARD;
        parseUI(UiTemplateLiveOrReplayInfo.parseFrom(protoItem.getUiData()));
    }

    private void parseUI(UiTemplateLiveOrReplayInfo protoItem) throws Exception {
        mHead = protoItem.getHeaderName();
        mHeadUri = protoItem.getHeaderViewAllUri();
        mHeadType = protoItem.getHeaderUiType();
        mSubHead = protoItem.getSubHeaderName();

        mHeadIconUrl = protoItem.getHeaderIcon();
        mHeadMoreText = protoItem.getHeaderViewAllText();

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
            } else if (protoItem.getType() == BaseItem.ITEM_TYPE_USER) {
                mItemDatas.add(new UserItem(protoItem));
            } else if (protoItem.getType() == BaseItem.ITEM_TYPE_VIDEO) {
                mItemDatas.add(new VideoItem(protoItem));
            } else if (protoItem.getType() == BaseItem.ITEM_TYPE_TV) {
                mItemDatas.add(new TVItem(protoItem));
            } else if (protoItem.getType() == BaseItem.ITEM_TYPE_SIMPLE) {
                mItemDatas.add(new SimpleItem(protoItem));
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

    public List<ChannelBannerViewModel.Banner> getBannerItems() {
        if (mBannerItems == null) {
            mBannerItems = new ArrayList();
            for (BaseItem item : mItemDatas) {
                try {
                    mBannerItems.add(new ChannelBannerViewModel.Banner(item));
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
            }
        }
        return mBannerItems;
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

    @Override
    public boolean isNeedRemove() {
        return false;
    }

    public static abstract class BaseItem<GM extends GeneratedMessage> extends BaseJumpItem {
        public static final int ITEM_TYPE_LIVE = 1;
        public static final int ITEM_TYPE_BACK = 2;
        public static final int ITEM_TYPE_USER = 3;
        public static final int ITEM_TYPE_VIDEO = 4;
        public static final int ITEM_TYPE_TV = 5;
        public static final int ITEM_TYPE_SIMPLE = 6;
        public static final int ITEM_TYPE_BUTTON = 7; // 小视频创建button

        // 共有的
        protected int mType;
        protected String mUpRightText;
        protected String mLineOneText;
        protected String mLineTwoText;
        protected String mImgUrl;
        protected String mImgUrl2;
        protected long mPublishTime;
        protected String mUpLeftText;
        protected CommonChannelProto.ListWidgetInfo mWidgetInfo; // 左边的icon
        protected RichText mTopLeft;
        protected CommonChannelProto.MiddleInfo mMiddleInfo;
        protected List<RichText> mLabel;

        protected User mUser;
        protected boolean mIsFocused;

        //图片或者小视频的宽高度
        protected int mWidth;
        protected int mHeight;
        private BannerManger.BannerItem mBannerItem;

        protected BaseItem() {
            mImgUrl = TestConstants.TEST_IMG_URL;
            mLineOneText = TestConstants.TEST_TITLE;
            mLineTwoText = TestConstants.TEST_TEXT;

            mUser = new User();
            mUser.setUid(816666);
        }

        public BaseItem(LiveOrReplayItemInfo protoItem) {
            mType = protoItem.getType();
            mSchemeUri = protoItem.getJumpSchemeUri();
            mUpRightText = protoItem.getUpRightText();
            mLineOneText = protoItem.getDownText1();
            mLineTwoText = protoItem.getDownText2();
            mImgUrl = protoItem.getImgUrl();
            mImgUrl2 = protoItem.getImgUrl2();
            mPublishTime = protoItem.getPublishTime();
            mUpLeftText = protoItem.getUpLeftText();

            mWidth = protoItem.getWidth();
            mHeight = protoItem.getHeight();

            if (protoItem.hasWidget()) {
                mWidgetInfo = protoItem.getWidget();
            }
            if (protoItem.hasTopLeft()) {
                mTopLeft = new RichText(protoItem.getTopLeft());
            }
            if (protoItem.hasMiddle()) {
                mMiddleInfo = protoItem.getMiddle();
            }
            if (protoItem.getLabelList() != null && !protoItem.getLabelList().isEmpty()) {
                mLabel = new ArrayList<>();
                for (CommonChannelProto.RichText richText : protoItem.getLabelList()) {
                    mLabel.add(new RichText(richText));
                }
            }
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

        public String getUpLeftText() {
            return mUpLeftText;
        }

        public String getImgUrl2() {
            return mImgUrl2;
        }

        public User getUser() {
            return mUser;
        }

        public abstract String getImageUrl();

        public abstract String getImageUrl(int sizeType);

        public abstract String getNameText();

        public abstract String getDisplayText();

        public boolean getIsFocus() {
            return mIsFocused;
        }

        public void setIsFocus(boolean isFocused) {
            mIsFocused = isFocused;
        }

        public CommonChannelProto.ListWidgetInfo getWidgetInfo() {
            return mWidgetInfo;
        }

        public CommonChannelProto.MiddleInfo getMiddleInfo() {
            return mMiddleInfo;
        }

        public List<RichText> getLabel() {
            return mLabel;
        }

        public void setLabel(List<RichText> mLabel) {
            this.mLabel = mLabel;
        }

        public RichText getTopLeft() {
            return mTopLeft;
        }

        public String getUserNickName() {
            if (mUser != null) {
                return mUser.getNickname();
            }
            return "";
        }

        public BannerManger.BannerItem toBannerItem() {
            if (mBannerItem == null) {
                mBannerItem = new BannerManger.BannerItem();
                mBannerItem.picUrl = mImgUrl;
                mBannerItem.skipUrl = mSchemeUri;
            }
            return mBannerItem;
        }

        public static BaseItem newTestInstance(int type) {
            if (type == BaseItem.ITEM_TYPE_LIVE) {
                return LiveItem.newTestInstance();
            } else if (type == BaseItem.ITEM_TYPE_BACK) {
                return BackItem.newTestInstance();
            } else if (type == BaseItem.ITEM_TYPE_USER) {
                return UserItem.newTestInstance();
            } else if (type == BaseItem.ITEM_TYPE_VIDEO) {
                return VideoItem.newTestInstance();
            } else if (type == BaseItem.ITEM_TYPE_TV) {
                return TVItem.newTestInstance();
            } else if (type == BaseItem.ITEM_TYPE_SIMPLE) {
                return SimpleItem.newTestInstance();
            }
            return null;
        }
    }

    public static class BaseLiveItem extends BaseItem {
        // LiveInfo & BackInfo共有的
        protected String mId;
        protected String mTitle;
        protected int mViewerCnt;
        protected String mVideoUrl;
        protected String mCoverUrl;
        protected String mLocation;
        protected int mDistance;

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

        public String getLocation() {
            return mLocation;
        }

        @Override
        public String getImageUrl() {
            return getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE);
        }

        @Override
        public String getImageUrl(int sizeType) {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl.contains(AvatarUtils.IMG_URL_POSTFIX) ? mImgUrl : mImgUrl + AvatarUtils.getAvatarSizeAppend(sizeType);
            } else if (!TextUtils.isEmpty(mCoverUrl)) {
                return mCoverUrl.contains(AvatarUtils.IMG_URL_POSTFIX) ? mCoverUrl : mCoverUrl + AvatarUtils.getAvatarSizeAppend(sizeType);
            } else {
                return AvatarUtils.getAvatarUrlByUidTsAndFormat(mUser.getUid(), sizeType, mUser.getAvatar(), false);
            }
        }

        @Override
        public String getNameText() {
            if (!TextUtils.isEmpty(mLineOneText)) {
                return mLineOneText;
            } else {
                return mUser.getNickname();
            }
        }

        public String getTitleText() {
            if (!TextUtils.isEmpty(mLineOneText)) {
                return mLineOneText;
            } else {
                return mTitle;
            }
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        @Override
        public String getDisplayText() {
            return mLineTwoText;
        }

        /**
         * 添加开普勒文案
         */
        public String getLocationText() {
            String display = getLocation();
            if (!TextUtils.isEmpty(display)) {
                return display;
            } else {
                return GlobalData.app().getResources().getString(R.string.live_location_unknown);
            }
        }

        public int getDistance() {
            return mDistance;
        }
    }

    public static class LiveItem extends BaseLiveItem {
        private static final int LIVE_TYPE_TICKET = 3;

        private String mLocation;
        private long mStartTime;

        private boolean mShowShop;
        private int mShopCnt;

        private String mExposeTag;      //曝光打点tag

        private int mAppType = 0;
        private int mLiveType = 0;

        private int mListPosition = -1;  //list的位置，不是服务器的数据
        private RoomInfo mRoomInfo;

        private LiveItem() {
            super();
        }

        public LiveItem(LiveOrReplayItemInfo protoItem) throws Exception {
            super(protoItem);
            parse(LiveInfo.parseFrom(protoItem.getItems()));

            if (!TextUtils.isEmpty(mSchemeUri)) {
                mIsEnterRoom = ModelHelper.isLiveScheme(mSchemeUri);
            } else {
                mIsEnterRoom = true;
            }
            mDistance = protoItem.getDistance();
            mCountString = parseCountString(true, mViewerCnt);
        }

        public void parse(LiveInfo protoItem) {
            mId = protoItem.getLiveId();
            mUser = new User(protoItem.getUser());
            mTitle = protoItem.getLiTitle();
            mViewerCnt = protoItem.getViewerCnt();
            mVideoUrl = protoItem.getUrl();
            mCoverUrl = protoItem.getCoverUrl();

            mLocation = protoItem.getLocation();
            mStartTime = protoItem.getStartTime();

            mExposeTag = protoItem.getTag();
            mAppType = protoItem.getAppType();
            mLiveType = protoItem.getLiveType();

            if (protoItem.hasShop()) {
                parseShop(protoItem.getShop());
            }
        }

        public boolean isYiZhiBo() {
            return mAppType == 4;
        }

        public boolean isTicket() {
            return mLiveType == LIVE_TYPE_TICKET;
        }

        public void parseShop(CommonChannelProto.ShopBrief shopBrief) {
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

        public boolean isShowShop() {
            return mShowShop;
        }

        public int getShopCnt() {
            return mShopCnt;
        }

        public int getListPosition() {
            return mListPosition;
        }

        public void setListPosition(int listPosition) {
            mListPosition = listPosition;
        }

        public RoomInfo toRoomInfo() {
            if (mRoomInfo == null) {
                mRoomInfo = RoomInfo.Builder.newInstance(mUser.getUid(), mId, mVideoUrl)
                        .setAvatar(mUser.getAvatar())
                        .setCoverUrl(getImageUrl())
                        .setLiveType(mLiveType)
                        .build();
            }
            return mRoomInfo;
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
            parse(BackInfo.parseFrom(protoItem.getItems()));

            if (!TextUtils.isEmpty(mSchemeUri)) {
                mIsEnterRoom = ModelHelper.isPlaybackScheme(mSchemeUri);
            } else {
                mIsEnterRoom = true;
            }

            mCountString = parseCountString(false, mViewerCnt);
        }

        public void parse(BackInfo protoItem) {
            mId = protoItem.getBackId();
            mUser = new User(protoItem.getUser());
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
    }

    public static class UserItem extends BaseItem {
        private UserItem() {
            super();
        }

        public UserItem(LiveOrReplayItemInfo protoItem) throws Exception {
            super(protoItem);
            parse(UserInfo.parseFrom(protoItem.getItems()));
        }

        public void parse(UserInfo protoItem) {
            mUser = new User(protoItem);
        }

        @Override
        public String getImageUrl() {
            return getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE);
        }

        @Override
        public String getImageUrl(int sizeType) {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl + AvatarUtils.getAvatarSizeAppend(sizeType);
            } else {
                return AvatarUtils.getAvatarUrlByUidTsAndFormat(mUser.getUid(), sizeType, mUser.getAvatar(), false);
            }
        }

        @Override
        public String getNameText() {
            if (!TextUtils.isEmpty(mLineOneText)) {
                return mLineOneText;
            } else {
                return mUser.getNickname();
            }
        }

        @Override
        public String getDisplayText() {
            if (!TextUtils.isEmpty(mLineTwoText)) {
                return mLineTwoText;
            } else {
                return mUser.getSign();
            }
        }

        public static UserItem newTestInstance() {
            return new UserItem();
        }
    }

    public static class VideoItem extends BaseItem {
        private String mId;
        private long mViewCount;
        private long mLikeCount;
        private long mDuration;
        private String mCountString;
        private boolean mIsLiked;

        private VideoItem() {
            super();
        }

        public VideoItem(LiveOrReplayItemInfo protoItem) throws Exception {
            super(protoItem);
            parse(VideoInfo.parseFrom(protoItem.getItems()));
        }

        public void parse(VideoInfo protoItem) {
            mId = protoItem.getId();
            mViewCount = protoItem.getViewCount();
            mLikeCount = protoItem.getLikeCount();
            mDuration = protoItem.getDuration();
            mUser = new User(protoItem.getUserInfo());
            mCountString = parseCountString(false, (int) mViewCount);
            mIsLiked = protoItem.getIsLiked();
        }

        @Override
        public String getImageUrl() {
            return getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE);
        }

        @Override
        public String getImageUrl(int sizeType) {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl + AvatarUtils.getAvatarSizeAppend(sizeType);
            } else {
                return AvatarUtils.getAvatarUrlByUidTsAndFormat(mUser.getUid(), sizeType, mUser.getAvatar(), false);
            }
        }

        @Override
        public String getNameText() {
            return mLineOneText;
        }

        @Override
        public String getDisplayText() {
            return mLineTwoText;
        }

        public String getId() {
            return mId;
        }

        public long getViewCount() {
            return mViewCount;
        }

        public String getCountString() {
            return mCountString;
        }

        public long getLikeCount() {
            return mLikeCount;
        }

        public long getDuration() {
            return mDuration;
        }

        public boolean isLiked() {
            return mIsLiked;
        }

        public static VideoItem newTestInstance() {
            return new VideoItem();
        }
    }

    public static class TVItem extends BaseItem {
        private TVItem() {
            super();
        }

        public TVItem(LiveOrReplayItemInfo protoItem) throws Exception {
            super(protoItem);
            parse(UserInfo.parseFrom(protoItem.getItems()));
        }

        public void parse(UserInfo protoItem) {
            mUser = new User(protoItem);
        }

        @Override
        public String getImageUrl() {
            return getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE);
        }

        @Override
        public String getImageUrl(int sizeType) {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl + AvatarUtils.getAvatarSizeAppend(sizeType);
            } else {
                return AvatarUtils.getAvatarUrlByUidTsAndFormat(mUser.getUid(), sizeType, mUser.getAvatar(), false);
            }
        }

        @Override
        public String getNameText() {
            if (!TextUtils.isEmpty(mLineOneText)) {
                return mLineOneText;
            } else {
                return mUser.getNickname();
            }
        }

        @Override
        public String getDisplayText() {
            if (!TextUtils.isEmpty(mLineTwoText)) {
                return mLineTwoText;
            } else {
                return mUser.getSign();
            }
        }

        public static TVItem newTestInstance() {
            return new TVItem();
        }
    }

    public static class SimpleItem extends BaseItem {
        private SimpleItem() {
            super();
        }

        public SimpleItem(LiveOrReplayItemInfo protoItem) {
            super(protoItem);
        }

        @Override
        public String getImageUrl() {
            return getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE);
        }

        @Override
        public String getImageUrl(int sizeType) {
            if (!TextUtils.isEmpty(mImgUrl)) {
                return mImgUrl + AvatarUtils.getAvatarSizeAppend(sizeType);
            } else if (mUser != null) {
                return AvatarUtils.getAvatarUrlByUidTsAndFormat(mUser.getUid(), sizeType, mUser.getAvatar(), false);
            } else {
                return TestConstants.TEST_IMG_URL;
            }
        }

        @Override
        public String getNameText() {
            if (!TextUtils.isEmpty(mLineOneText)) {
                return mLineOneText;
            } else if (mUser != null) {
                return mUser.getNickname();
            }
            return null;
        }

        @Override
        public String getDisplayText() {
            if (!TextUtils.isEmpty(mLineTwoText)) {
                return mLineTwoText;
            } else if (mUser != null) {
                return mUser.getSign();
            }
            return null;
        }

        public static SimpleItem newTestInstance() {
            return new SimpleItem();
        }
    }

    public static String parseCountString(boolean live, int count) {
        String sCount = String.valueOf(count);
        if (count > 10000) {
            String unit = GlobalData.app().getResources().getString(R.string.ten_thousand);
            sCount = String.format("%.1f" + unit, (float) (count / 10000.0));
        }
        if (!live) {
            return GlobalData.app().getResources().getQuantityString(R.plurals.channel_view_count, count, sCount);
        } else {
            return GlobalData.app().getResources().getQuantityString(R.plurals.channel_viewer_count, count, sCount);
        }
    }

    public static class RichText {
        // 对应bgId
        public static final int[] LEFT_LABEL_BG = {
                R.drawable.shape_channel_left_top_label_bg,
                R.drawable.shape_channel_left_top_label_bg_orange,
                R.drawable.home_anchor_lebel,
                R.drawable.home_headlines_label_one,
                R.drawable.home_headlines_label_two,
                R.drawable.home_headlines_label_three,
                R.drawable.shape_channel_left_top_bg_orange,
                R.drawable.shape_channel_left_top_label_purple};
        private String text;
        private String jumpUrl;
        private int bgID;
        private String iconUrl;

        public RichText(CommonChannelProto.RichText richText) {
            text = richText.getText();
            jumpUrl = richText.getJumpSchemeUri();
            bgID = richText.getBgImageID();
            iconUrl = richText.getIconUrl();
        }

        public RichText(String text, String jumpUrl, int bgID) {
            this.text = text;
            this.jumpUrl = jumpUrl;
            this.bgID = bgID;
        }

        public RichText() {

        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getJumpUrl() {
            return jumpUrl;
        }

        public void setJumpUrl(String jumpUrl) {
            this.jumpUrl = jumpUrl;
        }

        public int getBgID() {
            return bgID;
        }

        public void setBgID(int bgID) {
            this.bgID = bgID;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }
    }
}
