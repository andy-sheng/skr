package com.module.playways.grab.room;

import com.common.log.MyLog;
import com.common.utils.U;
import com.component.busilib.constans.GameModeType;
import com.module.playways.BaseRoomData;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.event.GrabGameOverEvent;
import com.module.playways.grab.room.event.GrabMyCoinChangeEvent;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.JoinGrabRoomRspModel;
import com.zq.live.proto.Room.EQRoundOverReason;
import com.zq.live.proto.Room.EQRoundResultType;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class GrabRoomData extends BaseRoomData<GrabRoundInfoModel> {
//    public static final int ACC_OFFSET_BY_LYRIC = 5000;// 伴奏是比歌词提前 5 秒的
    protected int mCoin;// 金币数
    protected List<GrabResultInfoModel> mResultList = new ArrayList<>(); // 一唱到底对战结果数据
    protected int mTagId;//一场到底歌曲分类
    protected GrabConfigModel mGrabConfigModel = new GrabConfigModel();// 一唱到底配置
    protected boolean mHasExitGame = false;// 是否已经正常退出房间
    private boolean mIsAccEnable = false;// 是否开启伴奏
    private Integer mSongLineNum;

    public GrabRoomData() {
        mIsAccEnable = U.getPreferenceUtils().getSettingBoolean("grab_acc_enable", false);
    }

    @Override
    public List<GrabPlayerInfoModel> getPlayerInfoList() {
        List<GrabPlayerInfoModel> l = new ArrayList<>();
        l.addAll(mExpectRoundInfo.getPlayUsers());
        l.addAll(mExpectRoundInfo.getWaitUsers());
        return l;
    }

    @Override
    public int getGameType() {
        return GameModeType.GAME_MODE_GRAB;
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
                lastRoundInfoModel.updateStatus(false, GrabRoundInfoModel.STATUS_OVER);
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
                lastRoundInfoModel.updateStatus(false, GrabRoundInfoModel.STATUS_OVER);
            }
            mRealRoundInfo = mExpectRoundInfo;
            if (mRealRoundInfo != null) {
                ((GrabRoundInfoModel) mRealRoundInfo).updateStatus(false, GrabRoundInfoModel.STATUS_GRAB);
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

    public void setResultList(List<GrabResultInfoModel> resultList) {
        mResultList.clear();
        mResultList.addAll(resultList);
    }

    public List<GrabResultInfoModel> getResultList() {
        return mResultList;
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

    public boolean isAccEnable() {
        return mIsAccEnable;
    }

    public void setAccEnable(boolean accEnable) {
        mIsAccEnable = accEnable;
        U.getPreferenceUtils().setSettingBoolean("grab_acc_enable", mIsAccEnable);
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

    public void loadFromRsp(JoinGrabRoomRspModel rsp) {
        this.setGameId(rsp.getRoomID());
        this.setCoin(rsp.getCoin());
        if (rsp.getConfig() != null) {
            this.setGrabConfigModel(rsp.getConfig());
        } else {
            MyLog.w(TAG, "JoinGrabRoomRspModel rsp==null");
        }
        GrabRoundInfoModel grabRoundInfoModel = rsp.getCurrentRound();
        if (rsp.isNewGame()) {
            grabRoundInfoModel.setParticipant(true);
        } else {
            grabRoundInfoModel.setParticipant(false);
            grabRoundInfoModel.setEnterStatus(grabRoundInfoModel.getStatus());
        }
        grabRoundInfoModel.setElapsedTimeMs(rsp.getElapsedTimeMs());
        this.setExpectRoundInfo(grabRoundInfoModel);
        this.setRealRoundInfo(null);
//            mRoomData.setRealRoundInfo(rsp.getCurrentRound());
        this.setTagId(rsp.getTagID());
        this.setGameCreateTs(rsp.getGameCreateMs());
        if (this.getGameCreateTs() == 0) {
            this.setGameCreateTs(System.currentTimeMillis());
        }
        if (this.getGameStartTs() == 0) {
            this.setGameStartTs(this.getGameCreateTs());
        }
        setIsGameFinish(false);
        setHasExitGame(false);
        mResultList.clear();
    }

    public Integer getSongLineNum() {
        return mSongLineNum;
    }

    public void setSongLineNum(Integer songLineNum) {
        mSongLineNum = songLineNum;
    }
}
