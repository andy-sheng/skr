package com.module.playways.room.room.comment.listener;

import com.module.playways.room.room.comment.model.CommentAudioModel;

public interface CommentViewItemListener {
    /**
     * 头像的点击
     *
     * @param userId
     */
    void clickAvatar(int userId);

    void clickAudio(boolean isPlaying, CommentAudioModel commentAudioModel);
}
