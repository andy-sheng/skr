package com.module.playways.room.room.comment.listener;

public interface CommentViewItemListener {
    /**
     * 头像的点击
     *
     * @param userId
     */
    void clickAvatar(int userId);

    void clickAudio(boolean isPlaying, String localPath, String msgUrl);
}
