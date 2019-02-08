package com.module.home;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;

import com.common.core.upgrade.UpgradeManager;
import com.common.core.account.UserAccountManager;
import com.common.core.login.interceptor.JudgeLoginInterceptor;
import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;

import com.common.view.viewpager.NestViewPager;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.ModuleServiceManager;
import com.module.RouterConstants;
import com.module.home.fragment.GameFragment;
import com.module.home.fragment.PersonFragment;
import com.module.home.persenter.HomeCorePresenter;
import com.module.home.view.IHomeActivity;
import com.module.msg.IMsgService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


@Route(path = RouterConstants.ACTIVITY_HOME)
public class HomeActivity extends BaseActivity implements IHomeActivity {

    public final static String TAG = "HomeActivity";

    LinearLayout mBottomContainer;
    ExImageView mGameBtn;
    ExImageView mMessageBtn;
    ExImageView mPersonInfoBtn;
    NestViewPager mMainVp;
    IMsgService mMsgService;
    HomeCorePresenter mHomePresenter;
    boolean mFromCreate = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        boolean needFinish = false;
        for (Activity activity : U.getActivityUtils().getActivityList()) {
            if (activity instanceof HomeActivity) {
                MyLog.w(TAG, "已经有HomeActivity在堆栈中，取消当前的");
                needFinish = true;
                break;
            }
        }
        super.onCreate(savedInstanceState);
        if (needFinish) {
            finish();
        }
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.home_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
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
        mGameBtn.setSelected(true);

        mHomePresenter = new HomeCorePresenter(this);

        mHomePresenter.checkUserInfo("HomeActivity onCreate");

        RxView.clicks(mGameBtn)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(0, false);
                        mGameBtn.setSelected(true);
                        mMessageBtn.setSelected(false);
                        mPersonInfoBtn.setSelected(false);
                    }
                });

        RxView.clicks(mMessageBtn)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(1, false);
                        mGameBtn.setSelected(false);
                        mMessageBtn.setSelected(true);
                        mPersonInfoBtn.setSelected(false);
                    }
                });

        RxView.clicks(mPersonInfoBtn)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.trans_tab);
                        mMainVp.setCurrentItem(2, false);
                        mGameBtn.setSelected(false);
                        mMessageBtn.setSelected(false);
                        mPersonInfoBtn.setSelected(true);
                    }
                });
        mMainVp.setCurrentItem(0, false);
        mFromCreate = true;

        U.getSoundUtils().preLoad(TAG, R.raw.trans_tab);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mFromCreate) {
            mHomePresenter.checkPermiss(this);
        }
        mFromCreate = false;
        UpgradeManager.getInstance().checkUpdate1();
//        ExoPlayer exoPlayer = new ExoPlayer();
//        exoPlayer.startPlay("http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/audios/56e25fc3bccff9b8.aac");
//
//        HandlerTaskTimer.newBuilder().delay(10000).start(new HandlerTaskTimer.ObserverW() {
//            @Override
//            public void onNext(Integer integer) {
//                U.getToastUtil().showShort("换首歌");
//                exoPlayer.reset();
//                exoPlayer.startPlay("http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/audios/62366357537f356c.aac");
//            }
//        });
    }

    @Override
    protected void destroy() {
        super.destroy();
        if (mHomePresenter != null) {
            mHomePresenter.destroy();
        }
        U.getSoundUtils().release(TAG);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange foreOrBackgroundChange) {
        if (foreOrBackgroundChange.foreground) {
            // 后台到前台了
            mHomePresenter.checkPermiss(this);
        }
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        return true;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public boolean onBackPressedForActivity() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            moveTaskToBack(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onLogoff() {
        mMainVp.setCurrentItem(1, false);
        mGameBtn.setSelected(true);
        mMessageBtn.setSelected(false);
        mPersonInfoBtn.setSelected(false);
    }
}
