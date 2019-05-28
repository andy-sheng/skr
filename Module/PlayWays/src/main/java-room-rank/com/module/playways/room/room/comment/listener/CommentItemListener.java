package com.module.playways.room.room.comment.listener;

public interface CommentItemListener {
    /**
     * 头像的点击
     * @param userId
     */
   void clickAvatar(int userId);


    /**
     * 踢人的点击
     * @param userId    被踢人id
     * @param isAgree   是否同意
     */
   void clickAgreeKick(int userId, boolean isAgree);
}
