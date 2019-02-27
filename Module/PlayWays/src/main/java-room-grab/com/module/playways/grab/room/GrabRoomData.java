package com.module.playways.grab.room;

import com.common.log.MyLog;
import com.component.busilib.constans.GameModeType;
import com.module.playways.BaseRoomData;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.event.GrabGameOverEvent;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class GrabRoomData extends BaseRoomData<GrabRoundInfoModel> {

    protected List<GrabResultInfoModel> mResultList; // 一唱到底对战结果数据
    protected int mTagId;//一场到底歌曲分类

    @Override
    public  List<GrabPlayerInfoModel> getPlayerInfoList() {
        List<GrabPlayerInfoModel> l = new ArrayList<>();
        l.addAll(mRealRoundInfo.getPlayUsers());
        l.addAll(mRealRoundInfo.getWaitUsers());
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

}
