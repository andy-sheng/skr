package com.module.playways.rank.room.view;

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
}
