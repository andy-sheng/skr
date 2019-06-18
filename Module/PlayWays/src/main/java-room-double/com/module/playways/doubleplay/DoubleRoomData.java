package com.module.playways.doubleplay;

import com.common.log.MyLog;
import com.zq.live.proto.Common.UserInfo;

import java.io.Serializable;

public class DoubleRoomData implements Serializable {
    public final static String TAG = "DoubleRoomData";

    DoubleGameState mDoubleGameState = DoubleGameState.HAS_NOT_START;
    UserInfo mGuestUserInfo;
    boolean mGuestMicroState;
    DoubleRoundInfo mCurRoundInfo;

    public DoubleRoomData(DoubleGameState doubleGameState) {
        mDoubleGameState = doubleGameState;
    }

    public void updateRoundInfo(DoubleRoundInfo roundInfo) {
        if (mDoubleGameState != DoubleGameState.END) {
            if (roundInfo == null) {
                //结束
                mDoubleGameState = DoubleGameState.END;
                return;
            }

            if (mDoubleGameState == DoubleGameState.HAS_NOT_START) {
                mDoubleGameState = DoubleGameState.START;
            }

            if (mCurRoundInfo == null) {
                //游戏开始
            } else if (mCurRoundInfo.getRoundSeq() == roundInfo.getRoundSeq()) {
                //更新
            } else if (roundInfo.getRoundSeq() > mCurRoundInfo.getRoundSeq()) {
                //切换
            } else {
                MyLog.d(TAG, "updateRoundInfo" + " roundInfo=" + roundInfo + ", mCurRoundInfo=" + mCurRoundInfo);
            }
        }
    }

    public void updateGameState(DoubleGameState doubleGameState) {
        if (doubleGameState.getValue() > mDoubleGameState.getValue()) {
            mDoubleGameState = doubleGameState;
        }
    }

    //未开始，已开始，已结束
    enum DoubleGameState {
        HAS_NOT_START(0), START(1), END(2);

        private final Integer status;

        DoubleGameState(Integer state) {
            this.status = state;
        }

        public int getValue() {
            return status;
        }
    }
}
