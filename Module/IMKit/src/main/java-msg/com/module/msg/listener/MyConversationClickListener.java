package com.module.msg.listener;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.module.RouterConstants;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

public class MyConversationClickListener implements RongIM.ConversationClickListener {

    /**
     * @param context          上下文。
     * @param conversationType 会话类型。
     * @param userInfo         被点击的用户的信息。
     * @param s
     * @return
     */
    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        int userId = Integer.valueOf(userInfo.getUserId());
        if (userId != MyUserInfoManager.getInstance().getUid() && userId != UserInfoModel.USER_ID_XIAOZHUSHOU) {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(U.getActivityUtils().getTopActivity());
            Bundle bundle = new Bundle();
            bundle.putInt("bundle_user_id", Integer.valueOf(userInfo.getUserId()));
            ARouter.getInstance()
                    .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                    .with(bundle)
                    .navigation();
        }
        return true;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        return false;
    }

    @Override
    public boolean onMessageClick(Context context, View view, Message message) {
        return false;
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s, Message message) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }
}
