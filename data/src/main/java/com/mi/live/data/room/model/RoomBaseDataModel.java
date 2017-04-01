package com.mi.live.data.room.model;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.push.presenter.RoomMessagePresenter;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.user.User;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by chengsimin on 16/4/1.
 */
public class RoomBaseDataModel implements Serializable {
    public static int NO = 0;
    public static final int SINGLE_MODEL = 0;
    public static final int PK_MODEL = 1;
    private CopyOnWriteArrayList<ViewerModel> mViewers = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<ViewerModel> mManagers = new CopyOnWriteArrayList<>();
    private User mOwner = new User();
    private String mCity;
    private int mViewCnt = 0;
    private int mInitTicket = -1;

    private boolean mTicketFirstIn = true;       //票数据初始好时 判断是否第一次进入房间  第一次进入不显示本场动画
    private boolean mIsTicketing = false;        //星票 是否是本场模式
    private String mCoverUrl;
    private String mLiveTitle;
    private boolean mIsShop;
    private boolean mHideGift;
    private long mEnterRoomTime = 0;             //观众端进入直播房间的时间
    private int mHideIcon = 0;


    /**
     * 后续这些可以当做房间状态用另外一个类存起来
     **/
    // 是否在voip连线
    private boolean mIsLine = false;

    // 直播是否结束
    private boolean mLiveEnd = false;

    // 观众离开
    private boolean mAnchorLeave = false;

    // 上一次更新观众数的时间
    private long mLastUpdateViewerCountTs;

    // 能发言
    private boolean mCanSpeak = true;

    // 在前台
    private boolean mIsForeground = false;

    //消息发送规则
    private Map<MessageRule.MessageRuleType, MessageRule> mMsgRule = new HashMap<>(2);

    /**
     * 口令直播、回放的密码
     */
    private String password;

    /**
     * 分享url
     */
    private String mShareUrl;

    /**
     * 直播类型，定义在Live.proto EnterLiveRsp.type 和 LiveManager
     */
    private int mLiveType;
    /**
     * 回放类型，定义在LiveShow.proto BackInfo.replay_type 和 BackShowListData
     */
    private int mReplayType;

    public int getTicketId() {
        return mTicketId;
    }

    public void setTicketId(int ticketId) {
        this.mTicketId = ticketId;
    }

    /**
     * 门票直播ID
     */
    private int mTicketId;
    /**
     * 门票直播价格，回放的价格是这个的一半
     */
    private int mTicketPrice;
    /**
     * 是否允许试看
     */
    private boolean mGlanceEnable;

    public int getGetMessageMode() {
        return getMessageMode;
    }

    public void setGetMessageMode(int getMessageMode) {
        this.getMessageMode = getMessageMode;
    }

    /**
     * 拉取消息类型
     */
    private int getMessageMode = RoomMessagePresenter.PUSH_MODE;

    public RoomBaseDataModel(String name) {
        MyLog.d("RoomBaseDataModel", "name:" + name + ",new NO:" + NO++);
    }

    public String getShareUrl() {
        return mShareUrl;
    }

    public void setShareUrl(String mShareUrl) {
        this.mShareUrl = mShareUrl;
    }

    /**
     * 获取房间类型
     *
     * @return liveType Live.proto里定义的type 和 LiveManager
     */
    public int getLiveType() {
        return mLiveType;
    }

    /**
     * 设置房间类型
     *
     * @param liveType Live.proto里定义的type 和 LiveManager
     */
    public void setLiveType(int liveType) {
        mLiveType = liveType;
    }

    /**
     * 获取回放类型
     *
     * @return 定义在LiveShow.proto BackInfo.replay_type 和 BackShowListData
     */
    public int getReplayType() {
        return mReplayType;
    }

    /**
     * 设置回放类型
     *
     * @param replayType 定义在LiveShow.proto BackInfo.replay_type 和 BackShowListData
     */
    public void setReplayType(int replayType) {
        mReplayType = replayType;
    }

    public boolean isShop() {
        return mIsShop;
    }

    public void setShop(boolean shop) {
        mIsShop = shop;
    }

    public boolean isHideGift() {
        return mHideGift;
    }

    public void setHideGift(boolean hideGift) {
        mHideGift = hideGift;
    }

    public int getHideIcon() {
        return mHideIcon;
    }

    public void setHideIcon(int hideIcon) {
        mHideIcon = hideIcon;
    }

    public void setUid(long id) {
        mOwner.setUid(id);
    }

    public long getUid() {
        return mOwner.getUid();
    }

    public void setNickname(String nickName) {
        mOwner.setNickname(nickName);
    }

    public String getNickName() {
        return mOwner.getNickname();
    }

    public String getSignature() {
        return mOwner.getSign();
    }

    public void setAvatarTs(long ts) {
        mOwner.setAvatar(ts);
    }

    public long getAvatarTs() {
        return mOwner.getAvatar();
    }

    public void setRoomId(String roomId) {
        mOwner.setRoomId(roomId);
    }

    public String getRoomId() {
        return mOwner.getRoomId();
    }

    public long getEnterRoomTime() {
        return mEnterRoomTime;
    }

    //只需要观众设置
    public void setEnterRoomTime(long l) {
        mEnterRoomTime = l;
    }

    public void setVideoUrl(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        mOwner.setViewUrl(videoUrl);
    }

    public String getVideoUrl() {
        return mOwner.getViewUrl();
    }

    public void setCity(String city) {
        this.mCity = city;
    }

    public String getCity() {
        if (TextUtils.isEmpty(mCity) || mCity.equals("null")) {
            mCity = "";
        }
        return mCity;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public String getLiveTitle() {
        if (TextUtils.isEmpty(mLiveTitle)) {
            mLiveTitle = "";
        }
        return mLiveTitle;
    }

    public void setLiveTitle(String liveTitle) {
        mLiveTitle = liveTitle;
    }

    public int getCertificationType() {
        return mOwner.getCertificationType();
    }

    public int getLevel() {
        return mOwner.getLevel();
    }

    public int getTicket() {
        return mOwner.getLiveTicketNum();
    }

    public void setTicket(int ticket) {
        if (ticket < mOwner.getLiveTicketNum()) {
            return;
        }
        mOwner.setLiveTicketNum(ticket);
        EventBus.getDefault().post(new RoomDataChangeEvent(this, RoomDataChangeEvent.TYPE_CHANGE_TICKET));
    }

    public int getViewerCnt() {
        return mViewCnt;
    }

    public void setViewerCnt(int count) {
        mViewCnt = count;
        EventBus.getDefault().post(new RoomDataChangeEvent(this, RoomDataChangeEvent.TYPE_CHANGE_VIEWER_COUNT));
    }

    public List<ViewerModel> getViewersList() {
        return this.mViewers;
    }

    public void clearViewers() {
        this.mViewers.clear();
    }

    public void setUser(User newUser) {
        if (newUser != null) {
            if (newUser.getUid() == mOwner.getUid()) {
                if (newUser.getLiveTicketNum() <= 0) {
                    newUser.setLiveTicketNum(mOwner.getLiveTicketNum());
                }
                if (TextUtils.isEmpty(newUser.getNickname())) {
                    newUser.setNickname(mOwner.getNickname());
                }
                if (newUser.getAvatar() <= 0 && mOwner.getAvatar() > 0) {
                    newUser.setAvatar(mOwner.getAvatar());
                }

                if (newUser.getFansNum() <= 0) {
                    newUser.setFansNum(mOwner.getFansNum());
                }

            }
            this.mOwner = newUser;
            // 通知票改变 通知头像改变 通知用户数改变 显示关注按钮
            EventBus.getDefault().post(new RoomDataChangeEvent(this, RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE));
        }
    }

    public User getUser() {
        return mOwner;
    }

    public int getPayBarrageGiftId() {
        return this.mOwner.getPayBarrageGiftId();
    }

    public int getGender() {
        return this.mOwner.getGender();
    }


    public void notifyViewersChange(String from) {
        MyLog.d("WatchTopInfoBaseView", "notifyViewersChange from:" + from);
        EventBus.getDefault().post(new RoomDataChangeEvent(this, RoomDataChangeEvent.TYPE_CHANGE_VIEWERS, null));
    }

    public void notifyManagersChange() {
        EventBus.getDefault().post(new RoomDataChangeEvent(this, RoomDataChangeEvent.TYPE_CHANGE_USER_MANAGER, null));
    }


    public boolean isFocused() {
        return mOwner.isFocused();
    }

    public boolean isBothwayFollowing() {
        return mOwner.isBothwayFollowing();
    }


    public List<ViewerModel> getManagerList() {
        return mManagers;
    }

    public int getInitTicket() {
        return mInitTicket;
    }

    public void setInitTicket(int mInitTicket) {
        this.mInitTicket = mInitTicket;
    }

    public void setTicketFirstIn(boolean firstIn) {
        this.mTicketFirstIn = firstIn;
    }

    public boolean isTicketFirstIn() {
        return mTicketFirstIn;
    }

    public void setTicketing(boolean isTicketing) {
        this.mIsTicketing = isTicketing;
    }

    public boolean isTicketing() {
        return mIsTicketing;
    }

    @Override
    public String toString() {
        return "RoomBaseDataModel{" +
                "mOwner=" + mOwner +
                ", mCity='" + mCity + '\'' +
                ", mViewCnt=" + mViewCnt +
                ", mInitTicket=" + mInitTicket +
                '}';
    }

    public boolean isLiveEnd() {
        return mLiveEnd;
    }

    public void setLiveEnd(boolean mLiveEnd) {
        this.mLiveEnd = mLiveEnd;
    }

    public boolean isLine() {
        return mIsLine;
    }

    public void setIsLine(boolean mIsLine) {
        this.mIsLine = mIsLine;
    }

    public boolean isAnchorLeave() {
        return mAnchorLeave;
    }

    public void setAnchorLeave(boolean mAnchorLeave) {
        this.mAnchorLeave = mAnchorLeave;
    }

    public boolean canUpdateLastUpdateViewerCount(long ts) {
        if (ts > mLastUpdateViewerCountTs) {
            mLastUpdateViewerCountTs = ts;
            return true;
        } else {
            return false;
        }
    }

    public boolean canSpeak() {
        return mCanSpeak;
    }

    public void setCanSpeak(boolean canSpeak) {
        this.mCanSpeak = canSpeak;
    }


    public boolean isForeground() {
        return mIsForeground;
    }


    public void setIsForeground(boolean mIsForeground) {
        this.mIsForeground = mIsForeground;
    }

    public MessageRule getmMsgRule() {
        if (mMsgRule.size() == 0) {
            return null;
        } else {
            return MessageRule.mergeMessageRule(mMsgRule);
        }
    }

    //合并发言频率规则，如果已经有了发言频率，则合并，否则直接设置
    public void setmMsgRule(MessageRule mMsgRule) {
        if (mMsgRule != null) {
            this.mMsgRule.put(mMsgRule.getMessageRuleType(), mMsgRule);
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTicketPrice() {
        return mTicketPrice;
    }

    public void setTicketPrice(int ticketPrice) {
        this.mTicketPrice = ticketPrice;
    }

    public boolean isGlanceEnable() {
        return mGlanceEnable;
    }

    public void setGlanceEnable(boolean glanceEnable) {
        this.mGlanceEnable = glanceEnable;
    }
}
