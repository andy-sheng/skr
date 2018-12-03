package io.rong.test.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.login.interceptor.JudgeLoginInterceptor;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExButton;
import com.common.view.titlebar.CommonTitleBar;
import com.module.RouterConstants;

import java.util.ArrayList;
import java.util.List;

import io.rong.contacts.fragment.ContactsFragment;
import io.rong.imkit.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import io.rong.test.fragment.MessageFragment;
import io.rong.test.token.RCTokenManager;

/**
 * 消息列表会话页面
 */
@Route(path = RouterConstants.ACTIVITY_MESSAGE, extras = JudgeLoginInterceptor.NO_NEED_LOGIN)
public class MessageActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.conversation_list_activity;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newParamsBuilder(MessageActivity.this, MessageFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());

    }


    @Override
    public boolean useEventBus() {
        return false;
    }
}
