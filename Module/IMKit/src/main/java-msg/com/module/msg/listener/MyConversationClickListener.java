package com.module.msg.listener;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.zq.person.fragment.OtherPersonFragment3;

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
        if (Integer.valueOf(userInfo.getUserId()) != MyUserInfoManager.getInstance().getUid()) {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(U.getActivityUtils().getTopActivity());
            Bundle bundle = new Bundle();
            bundle.putSerializable(OtherPersonFragment3.BUNDLE_USER_ID, Integer.valueOf(userInfo.getUserId()));
            U.getFragmentUtils().addFragment(FragmentUtils
                    .newAddParamsBuilder((BaseActivity) context, OtherPersonFragment3.class)
                    .setUseOldFragmentIfExist(false)
                    .setBundle(bundle)
                    .setAddToBackStack(true)
                    .setHasAnimation(true)
                    .build());
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
