package com.module.playways.grab.room;

import com.common.log.MyLog;
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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class GrabRoomData extends BaseRoomData<GrabRoundInfoModel> {
    protected int mCoin;// 金币数
    protected List<GrabResultInfoModel> mResultList; // 一唱到底对战结果数据
    protected int mTagId;//一场到底歌曲分类
    protected GrabConfigModel mGrabConfigModel;

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
        MyLog.d(TAG, "checkRound mExcpectRoundInfo=" + mExpectRoundInfo + " mRealRoundInfo=" + mRealRoundInfo);
        if (mIsGameFinish) {
            MyLog.d(TAG, "游戏结束了，不需要再check");
            return;
        }
        if (mExpectRoundInfo == null) {
            // 结束状态了
            if (mRealRoundInfo != null) {
                GrabRoundInfoModel lastRoundInfoModel = (GrabRoundInfoModel) mRealRoundInfo;
                lastRoundInfoModel.updateStatus(false, GrabRoundInfoModel.STATUS_OVER);
                mRealRoundInfo = null;
                EventBus.getDefault().post(new GrabGameOverEvent(lastRoundInfoModel));
            }
            return;
        }
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
            EventBus.getDefault().post(new GrabRoundChangeEvent(lastRoundInfoModel, (GrabRoundInfoModel) mRealRoundInfo));
        }
    }

    public void setResultList(List<GrabResultInfoModel> resultList) {
        mResultList = resultList;
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
            this.mCoin = coin;
            EventBus.getDefault().post(new GrabMyCoinChangeEvent(coin));
        }
    }

    public GrabConfigModel getGrabConfigModel() {
        return mGrabConfigModel;
    }

    public void setGrabConfigModel(GrabConfigModel grabConfigModel) {
        mGrabConfigModel = grabConfigModel;
    }

    public void loadFromRsp(JoinGrabRoomRspModel rsp) {
        this.setGrabConfigModel(rsp.getConfig());
        this.setGameId(rsp.getRoomID());
        this.setCoin(rsp.getCoin());
        GrabRoundInfoModel grabRoundInfoModel = rsp.getCurrentRound();
        if (rsp.isNewGame()) {
            grabRoundInfoModel.setParticipant(true);
        } else {
            grabRoundInfoModel.setParticipant(false);
        }
        grabRoundInfoModel.setElapsedTimeMs(rsp.getElapsedTimeMs());
        this.setExpectRoundInfo(grabRoundInfoModel);
//            mRoomData.setRealRoundInfo(rsp.getCurrentRound());
        this.setTagId(rsp.getTagID());
        this.setGameCreateTs(rsp.getGameCreateMs());
        if (this.getGameCreateTs() == 0) {
            this.setGameCreateTs(System.currentTimeMillis());
        }
        if (this.getGameStartTs() == 0) {
            this.setGameStartTs(this.getGameCreateTs());
        }
    }
}
