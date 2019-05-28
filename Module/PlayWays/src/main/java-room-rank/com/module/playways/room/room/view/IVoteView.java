package com.module.playways.room.room.view;

import com.module.playways.room.room.model.RecordData;

public interface IVoteView {
    /**
     * 灭灯成功，被灭灯人ID
     * @param votedUserId
     */
    void voteSucess(long votedUserId);

    /**
     * 灭灯失败
     */
    void voteFailed();

    /**
     * 战绩界面
     */
    void showRecordView(RecordData recordData);
}
