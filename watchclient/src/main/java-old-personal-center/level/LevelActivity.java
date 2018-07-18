package com.wali.live.watchsdk.personalcenter.level;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.dialog.MyProgressDialog;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.view.BackTitleBar;
import com.base.view.SlidingTabLayout;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsKeyUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.level.adapter.LevelViewPagerAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-22.
 */

public class LevelActivity extends BaseSdkActivity {
    private static final String TAG = "LevelActivity";
    private static final String KEY_EXTRAL_PAGE_INDEX = "extral_page_index";
    public static final int PAGE_INDEX_VIP = 0;
    public static final int PAGE_INDEX_LEVEL = 1;

    //data
    private int mBegIndex;
    // Views ends ******************************************
    /**是否访问过VIP等级界面*/
    private boolean mHasVisitedVipLevelPage;
    /**是否已经获取到了最新的VIP数据，关于冻结的*/
    private boolean mHasFetchedVipFrozenInfo;

    //ui
    private BackTitleBar mTitleBar;
    private SlidingTabLayout mTabLayout;
    private ViewPager mViewPager;
    private LevelViewPagerAdapter mPagerAdapter;
    private MyProgressDialog mProgressDialog;     //loading信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_level);

        initParams();
        initView();
        initListener();
    }

    private void initParams() {
        Intent intent = getIntent();
        if(intent != null) {
            mBegIndex = intent.getIntExtra(KEY_EXTRAL_PAGE_INDEX, 0);
        }
    }

    private void initView() {
        mTitleBar = (BackTitleBar) findViewById(R.id.title_bar);
        mTitleBar.getBackBtn().setText(R.string.level_title);
        mTabLayout = (SlidingTabLayout) findViewById(R.id.level_sliding_tab);
        mTabLayout.setCustomTabView(R.layout.level_sliding_tab, R.id.tab_tv);
        mTabLayout.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER);
        mTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_ff2966));
        mTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));// 36px
        mTabLayout.setIndicatorBottomMargin(DisplayUtils.dip2px(6));// 18px
        mViewPager = (ViewPager) findViewById(R.id.level_view_pager);
        mPagerAdapter = new LevelViewPagerAdapter(this);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setViewPager(mViewPager);

        switch (mBegIndex) {
            case PAGE_INDEX_VIP:
                mViewPager.setCurrentItem(PAGE_INDEX_VIP, false);
                break;
            case PAGE_INDEX_LEVEL:
                mViewPager.setCurrentItem(PAGE_INDEX_LEVEL, false);
                break;
        }
    }

    private void initListener() {
        RxView.clicks(mTitleBar.getBackBtn()).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        finish();
                    }
                });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                scribeVipLevelPage(position);
                showVipFrozenDialog(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void scribeVipLevelPage(int position) {
        if (position != PAGE_INDEX_VIP) {
            return;
        }
        if (mHasVisitedVipLevelPage) {
            return;
        }
        mHasVisitedVipLevelPage = true;
    }

    private void showVipFrozenDialog(int position) {
        MyLog.d(TAG, "determine whether show vip frozen dialog, position:" + position
                + ", fetchedVipInfo:" + mHasFetchedVipFrozenInfo);
        if (position != PAGE_INDEX_VIP
                || !mHasFetchedVipFrozenInfo
                || MyUserInfoManager.getInstance().getVipLevel() <= 0
                || !MyUserInfoManager.getInstance().isVipFrozen()) {
            return;
        }
        MyAlertDialog.Builder dialogBuilder = new MyAlertDialog.Builder(LevelActivity.this);
        View view = LayoutInflater.from(LevelActivity.this).inflate(R.layout.vip_frozen_tip_dialog_msg, null);
        ((TextView)view.findViewById(R.id.line1)).setText(R.string.vip_frozen_tip_line_1);
        ((TextView)view.findViewById(R.id.line2)).setText(R.string.vip_frozen_tip_line_2);
        dialogBuilder.setView(view);
        dialogBuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.create().show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FetchedVipFrozenInfoEvent event) {
        if (event == null) {
            return;
        }
        mHasFetchedVipFrozenInfo = true;
        showVipFrozenDialog(mViewPager.getCurrentItem());
    }

    /**已经获取到最新的VIP被冻结的信息*/
    public final static class FetchedVipFrozenInfoEvent {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.clearOnPageChangeListeners();
    }

    public static void openActivity(@NonNull Activity activity, int extralIndex) {
        Intent intent = new Intent(activity, LevelActivity.class);
        intent.putExtra(KEY_EXTRAL_PAGE_INDEX, extralIndex);
        activity.startActivity(intent);
    }
}
