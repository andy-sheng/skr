package com.module.playways.grab.room;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.DeviceUtils;
import com.common.utils.U;
import com.component.busilib.constans.GameModeType;
import com.module.playways.BaseRoomData;
import com.module.playways.RoomDataUtils;
import com.component.busilib.friends.SpecialModel;
import com.module.playways.grab.room.event.GrabGameOverEvent;
import com.module.playways.grab.room.event.GrabMyCoinChangeEvent;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.guide.model.GrabGuideInfoModel;
import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.WorksUploadModel;
import com.module.playways.room.gift.event.UpdateHZEvent;
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel;
import com.zq.live.proto.Room.EQRoundStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class GrabRoomData extends BaseRoomData<GrabRoundInfoModel> {
    //    public static final int ACC_OFFSET_BY_LYRIC = 5000;// 伴奏是比歌词提前 5 秒的
    protected int mCoin;// 金币数
    protected float mHzCount;// 金币数
    protected int mTagId;//一场到底歌曲分类
    protected GrabConfigModel mGrabConfigModel = new GrabConfigModel();// 一唱到底配置
    protected boolean mHasExitGame = false;// 是否已经正常退出房间
    private boolean mIsAccEnable = false;// 是否开启伴奏,只代表设置里伴奏开关
    private int mSongLineNum;// 歌词总行数
    private int roomType;// 一唱到底房间类型，公开，好友，私密，普通
    private int ownerId;// 房主id
    private boolean hasGameBegin = true;// 游戏是否已经开始
    SpecialModel mSpecialModel;

    GrabResultData mGrabResultData;    // 游戏结果
    private boolean mSpeaking; // 是否正在抢麦说话，一般用于主播控场
    private boolean mChallengeAvailable;
    private String roomName;  // 房间名称
    int mOwnerKickTimes = 100;  // 房主剩余踢人的次数,默认为100

    private boolean isNewUser = false;   // 是否是新手引导房间
    private int mOpenRecording = -1; // 是否开启高光时刻

    private List<WorksUploadModel> mWorksUploadList = new ArrayList<>();// 作品时刻本地录音文件路径

    long lastHzTs = -1;

    public GrabRoomData() {
        mIsAccEnable = U.getPreferenceUtils().getSettingBoolean("grab_acc_enable1", false);
    }

    @Override
    public List<GrabPlayerInfoModel> getPlayerInfoList() {
        List<GrabPlayerInfoModel> l = new ArrayList<>();
        if (mExpectRoundInfo != null) {
            l.addAll(mExpectRoundInfo.getPlayUsers());
            l.addAll(mExpectRoundInfo.getWaitUsers());
        } else {
            GrabPlayerInfoModel p = new GrabPlayerInfoModel();
            p.setSkrer(false);
            p.setOnline(true);
            p.setRole(GrabPlayerInfoModel.ROLE_PLAY);
            p.setUserID((int) MyUserInfoManager.getInstance().getUid());
            UserInfoModel userInfoModel = new UserInfoModel();
            userInfoModel.setUserId((int) MyUserInfoManager.getInstance().getUid());
            userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
            userInfoModel.setNickname(MyUserInfoManager.getInstance().getNickName());
            p.setUserInfo(userInfoModel);
            l.add(p);
        }
        return l;
    }

    public List<GrabPlayerInfoModel> getInSeatPlayerInfoList() {
        List<GrabPlayerInfoModel> l = new ArrayList<>();
        if (mExpectRoundInfo != null) {
            l.addAll(mExpectRoundInfo.getPlayUsers());
        }
        return l;
    }


    public boolean isInPlayerList() {
        if (mExpectRoundInfo == null || mExpectRoundInfo.getPlayUsers() == null) {
            return false;
        }

        List<GrabPlayerInfoModel> getPlayerInfoList = mExpectRoundInfo.getPlayUsers();
        for (GrabPlayerInfoModel model :
                getPlayerInfoList) {
            if (model.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                return true;
            }
        }

        return false;
    }

    public SpecialModel getSpecialModel() {
        return mSpecialModel;
    }

    public void setSpecialModel(SpecialModel specialModel) {
        mSpecialModel = specialModel;
    }

    @Override
    public int getGameType() {
        return GameModeType.GAME_MODE_GRAB;
    }

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }

    /**
     * 检查轮次信息是否需要更新
     */
    @Override
    public void checkRoundInEachMode() {
        if (mIsGameFinish) {
            MyLog.d(TAG, "游戏结束了，不需要再checkRoundInEachMode");
            return;
        }
        if (mExpectRoundInfo == null) {
            MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo == null");
            // 结束状态了
            if (mRealRoundInfo != null) {
                GrabRoundInfoModel lastRoundInfoModel = (GrabRoundInfoModel) mRealRoundInfo;
                lastRoundInfoModel.updateStatus(false, EQRoundStatus.QRS_END.getValue());
                mRealRoundInfo = null;
//                if (lastRoundInfoModel != null
//                        && lastRoundInfoModel.getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()
//                        && lastRoundInfoModel.getResultType() == EQRoundResultType.ROT_TYPE_1.getValue()) {
//                    // 一唱到底自动加金币
//                    setCoin(getCoin() + 1);
//                }
                EventBus.getDefault().post(new GrabGameOverEvent(lastRoundInfoModel));
            }
            return;
        }
        MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo.roundSeq=" + mExpectRoundInfo.getRoundSeq());
        if (RoomDataUtils.roundSeqLarger(mExpectRoundInfo, mRealRoundInfo) || mRealRoundInfo == null) {
            // 轮次大于，才切换
            GrabRoundInfoModel lastRoundInfoModel = (GrabRoundInfoModel) mRealRoundInfo;
            if (lastRoundInfoModel != null) {
                lastRoundInfoModel.updateStatus(false, EQRoundStatus.QRS_END.getValue());
            }
            mRealRoundInfo = mExpectRoundInfo;
            if (mRealRoundInfo != null) {
                ((GrabRoundInfoModel) mRealRoundInfo).updateStatus(false, EQRoundStatus.QRS_INTRO.getValue());
            }
            // 告知切换到新的轮次了
//            if (lastRoundInfoModel != null
//                    && lastRoundInfoModel.getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()
//                    && lastRoundInfoModel.getResultType() == EQRoundResultType.ROT_TYPE_1.getValue()) {
//                // 一唱到底自动加金币
//                setCoin(getCoin() + 1);
//            }
            EventBus.getDefault().post(new GrabRoundChangeEvent(lastRoundInfoModel, (GrabRoundInfoModel) mRealRoundInfo));
        }
    }

    public int getTagId() {
        return mTagId;
    }

    public void setTagId(int tagId) {
        this.mTagId = tagId;
    }

    public int getCoin() {
        return mCoin;
    }

    public void setCoin(int coin) {
        if (this.mCoin != coin) {
            EventBus.getDefault().post(new GrabMyCoinChangeEvent(coin, coin - this.mCoin));
            this.mCoin = coin;
        }
    }

    public void setCoinNoEvent(int coin) {
        if (this.mCoin != coin) {
            this.mCoin = coin;
        }
    }

    public void setHzCount(float hzCount, long ts) {
        if (lastHzTs < ts) {
            lastHzTs = ts;
            mHzCount = hzCount;
            EventBus.getDefault().post(new UpdateHZEvent(mHzCount, ts));
        }
    }

    public float getHzCount() {
        return mHzCount;
    }

    public boolean isAccEnable() {
        return mIsAccEnable;
    }

    public void setAccEnable(boolean accEnable) {
        mIsAccEnable = accEnable;
        U.getPreferenceUtils().setSettingBoolean("grab_acc_enable1", mIsAccEnable);
    }

    public GrabConfigModel getGrabConfigModel() {
        return mGrabConfigModel;
    }

    public void setGrabConfigModel(GrabConfigModel grabConfigModel) {
        mGrabConfigModel = grabConfigModel;
    }

    public boolean isHasExitGame() {
        return mHasExitGame;
    }

    public void setHasExitGame(boolean hasExitGame) {
        mHasExitGame = hasExitGame;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public GrabResultData getGrabResultData() {
        return mGrabResultData;
    }

    public void setGrabResultData(GrabResultData grabResultData) {
        mGrabResultData = grabResultData;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * 是不是房主
     *
     * @return
     */
    public boolean isOwner() {
        return this.ownerId == MyUserInfoManager.getInstance().getUid();
    }

    public boolean hasGameBegin() {
        return hasGameBegin;
    }

    public void setHasGameBegin(boolean hasGameBegin) {
        this.hasGameBegin = hasGameBegin;
    }

    public void loadFromRsp(JoinGrabRoomRspModel rsp) {
        this.setGameId(rsp.getRoomID());
        this.setCoin(rsp.getCoin());
        this.setHzCount(rsp.getHongZuan(), 0);
        if (rsp.getConfig() != null) {
            this.setGrabConfigModel(rsp.getConfig());
        } else {
            MyLog.w(TAG, "JoinGrabRoomRspModel rsp==null");
        }
        GrabRoundInfoModel grabRoundInfoModel = rsp.getCurrentRound();
        if (grabRoundInfoModel != null) {
            if (rsp.isNewGame()) {
                grabRoundInfoModel.setParticipant(true);
            } else {
                grabRoundInfoModel.setParticipant(false);
                grabRoundInfoModel.setEnterStatus(grabRoundInfoModel.getStatus());
            }
            grabRoundInfoModel.setElapsedTimeMs(rsp.getElapsedTimeMs());
        }
        this.setExpectRoundInfo(grabRoundInfoModel);
        this.setRealRoundInfo(null);
//            mRoomData.setRealRoundInfo(rsp.getCurrentRound());
        this.setTagId(rsp.getTagID());

        setIsGameFinish(false);
        setHasExitGame(false);
        this.setAgoraToken(rsp.getAgoraToken());
        this.setRoomType(rsp.getRoomType());
        this.setOwnerId(rsp.getOwnerID());

        if (this.getGameCreateTs() == 0) {
            this.setGameCreateTs(System.currentTimeMillis());
        }
        if (this.getGameStartTs() == 0) {
            this.setGameStartTs(this.getGameCreateTs());
        }
        // 游戏未开始
        this.setHasGameBegin(rsp.hasGameBegin());
        this.setChallengeAvailable(rsp.isChallengeAvailable());
        this.setRoomName(rsp.getRoomName());
    }

    public boolean isChallengeAvailable() {
        return mChallengeAvailable;
    }

    public void setChallengeAvailable(boolean challengeAvailable) {
        mChallengeAvailable = challengeAvailable;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getSongLineNum() {
        return mSongLineNum;
    }

    public void setSongLineNum(int songLineNum) {
        mSongLineNum = songLineNum;
    }

    public int getOwnerKickTimes() {
        return mOwnerKickTimes;
    }

    public void setOwnerKickTimes(int ownerKickTimes) {
        mOwnerKickTimes = ownerKickTimes;
    }

    @Override
    public String toString() {
        return "GrabRoomData{" +
                ", mTagId=" + mTagId +
                ", mGrabConfigModel=" + mGrabConfigModel +
                ", roomType=" + roomType +
                ", ownerId=" + ownerId +
                ", hasGameBegin=" + hasGameBegin +
                ", mAgoraToken=" + mAgoraToken +
                '}';
    }

    public void setSpeaking(boolean speaking) {
        mSpeaking = speaking;
    }

    public boolean isSpeaking() {
        return mSpeaking;
    }


    GrabGuideInfoModel mGrabGuideInfoModel;

    public void setGrabGuideInfoModel(GrabGuideInfoModel grabGuideInfoModel) {
        mGrabGuideInfoModel = grabGuideInfoModel;
    }

    public GrabGuideInfoModel getGrabGuideInfoModel() {
        return mGrabGuideInfoModel;
    }

    public boolean openAudioRecording() {
        if (mOpenRecording == -1) {
            if (U.getDeviceUtils().getLevel().equals(DeviceUtils.LEVEL.BAD)) {
                MyLog.w(TAG, "设备太差，不开启录制");
                mOpenRecording = 0;
            } else {
                mOpenRecording = 1;
            }
        }
        return mOpenRecording == 1;
    }


    public void addWorksUploadModel(WorksUploadModel savePath){
        mWorksUploadList.add(savePath);
    }

    public List<WorksUploadModel> getWorksUploadModel() {
        return mWorksUploadList;
    }
}
