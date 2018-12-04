package com.module.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.common.log.MyLog;
import com.common.view.ex.ExTextView;
import com.common.view.viewpager.NestViewPager;
import com.module.home.fragment.GameFragment;
import com.module.home.fragment.PersonFragment;
import com.module.msg.IMsgService;

@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity {

    LinearLayout mBottomContainer;
    ExTextView mGameBtn;
    ExTextView mMessageBtn;
    ExTextView mPersonInfoBtn;
    NestViewPager mMainVp;
    IMsgService mMsgService;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.home_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mBottomContainer = (LinearLayout) findViewById(R.id.bottom_container);
        mGameBtn = (ExTextView) findViewById(R.id.game_btn);
        mMessageBtn = (ExTextView) findViewById(R.id.message_btn);
        mPersonInfoBtn = (ExTextView) findViewById(R.id.person_info_btn);
        mMainVp = (NestViewPager) findViewById(R.id.main_vp);
        mMsgService = ModuleServiceManager.getInstance().getMsgService();
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(TAG, "getItem" + " position=" + position);
                if (position == 0) {
                    return new GameFragment();
                } else if (position == 1) {
                    if(mMsgService==null){
                        return new PersonFragment();
                    }else{
                        return (Fragment) mMsgService.getMessageFragment();
                    }
                } else if (position == 2) {
                    return new PersonFragment();
                }
                return null;
            }

            @Override
            public int getCount() {
                if(mMsgService==null){
                    return 2;
                }else{
                    return 3;
                }
            }
        };

        mMainVp.setAdapter(fragmentPagerAdapter);
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
