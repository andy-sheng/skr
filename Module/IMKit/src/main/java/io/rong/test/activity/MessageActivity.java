package io.rong.test.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.login.interceptor.JudgeLoginInterceptor;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;

import io.rong.imkit.R;
import io.rong.test.fragment.MessageFragment;

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
