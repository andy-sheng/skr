package com.module.home;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.utils.CommonReceiver;
import com.common.utils.U;
import com.common.view.ex.ExImageView;

import com.common.view.viewpager.NestViewPager;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.home.fragment.GameFragment;
import com.module.home.fragment.PersonFragment;
import com.module.home.persenter.HomePresenter;
import com.module.msg.IMsgService;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity {

    LinearLayout mBottomContainer;
    ExImageView mGameBtn;
    ExImageView mMessageBtn;
    ExImageView mPersonInfoBtn;
    NestViewPager mMainVp;
    IMsgService mMsgService;
    HomePresenter mHomePresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.home_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getKeyBoardUtils().hideSoftInputKeyBoard(this);
        mBottomContainer = (LinearLayout) findViewById(R.id.bottom_container);
        mGameBtn = (ExImageView) findViewById(R.id.game_btn);
        mMessageBtn = (ExImageView) findViewById(R.id.message_btn);
        mPersonInfoBtn = (ExImageView) findViewById(R.id.person_info_btn);
        mMainVp = (NestViewPager) findViewById(R.id.main_vp);
        mMsgService = ModuleServiceManager.getInstance().getMsgService();

        mMainVp.setViewPagerCanScroll(false);
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(TAG, "getItem" + " position=" + position);
                if (position == 0) {
                    return new GameFragment();
                } else if (position == 1) {
                    if (mMsgService == null) {
                        return new PersonFragment();
                    } else {
                        return (Fragment) mMsgService.getMessageFragment();
                    }
                } else if (position == 2) {
                    return new PersonFragment();
                }
                return null;
            }

            @Override
            public int getCount() {
                if (mMsgService == null) {
                    return 2;
                } else {
                    return 3;
                }
            }
        };

        mMainVp.setAdapter(fragmentPagerAdapter);

        mHomePresenter = new HomePresenter();

        if (!UserAccountManager.getInstance().hasAccount()) {
            // 到时会有广告页或者启动页挡一下的，先不用管
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN).navigation();
        } else {
            initOnAccountReady();
        }

        RxView.clicks(mGameBtn)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        mMainVp.setCurrentItem(0, false);
                    }
                });

        RxView.clicks(mMessageBtn)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        mMainVp.setCurrentItem(1, false);
                    }
                });

        RxView.clicks(mPersonInfoBtn)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        mMainVp.setCurrentItem(2, false);
                    }
                });

    }

    public void initOnAccountReady() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mHomePresenter.checkPermiss(this);
    }

    @Override
    protected void destroy() {
        super.destroy();
        if (mHomePresenter != null) {
            mHomePresenter.destroy();
        }
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean onBackPressedForActivity() {
        moveTaskToBack(true);
        return true;
    }
}
