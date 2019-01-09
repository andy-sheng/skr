package com.module.playways.rank.room.model;

import com.common.log.MyLog;
import com.zq.live.proto.Room.UserScoreRecord;

import model.UserScoreModel;

public class UserScoreDataUtils {

    public static void transform(UserScoreModel userScoreModel, UserScoreRecord userScoreRecord){
        if (userScoreRecord == null) {
            MyLog.e("UserScoreRecord userScoreRecord == null");
            return;
        }

        userScoreModel.setUserID(userScoreRecord.getUserID());
        userScoreModel.setScoreType(userScoreRecord.getScoreType().getValue());
        userScoreModel.setScoreNow(userScoreRecord.getScoreNow());
        userScoreModel.setScoreBefore(userScoreRecord.getScoreBefore());
        userScoreModel.setScoreTypeDesc(userScoreRecord.getScoreTypeDesc());
        userScoreModel.setScoreNowDesc(userScoreRecord.getScoreNowDesc());
        userScoreModel.setScoreBeforeDesc(userScoreRecord.getScoreBeforeDesc());

    }
}
