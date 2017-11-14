package com.wali.live.watchsdk.ranking;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.view.NoScrollViewPager;
import com.wali.live.watchsdk.view.ViewPagerIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @module 星票排行榜页面
 * Created by jiyangli on 16-6-30.
 */
public class RankingPagerFragment extends BaseFragment {
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    /*总榜点击进入*/
    public static final String PARAM_FROM_TOTAL = "total";
    /* 本场榜点击进入*/
    public static final String PARAM_FROM_CURRENT = "current";

    public static final String IS_SHOW_CURRENT = "isShowCurrent";

    public static final String EXTRA_TICKET_COUNT = "extra_ticket_count";
    public static final String EXTRA_START_TICKET_COUNT = "extra_start_ticket_count";
    public static final String EXTRA_UUID = "extra_uid";
    public static final String EXTRA_LIVEID = "extra_live_id";
    public static final String EXTRA_IS_LANDSCAPE = "extra_is_landscape";
    public static final String EXTRA_FROM_TYPE = "extra_type";

    private List<Fragment> mTabContents = new ArrayList();

    private FragmentStatePagerAdapter mAdapter;
    private NoScrollViewPager mViewPager;
    private List<String> mDatas;
    private ViewPagerIndicator mIndicator;

    protected BackTitleBar mBackTitleBar;

    private int mTicketNum;
    private int mTicketStartNum;
    private String mFromType;
    private long mUuid;
    private boolean isShowCurrent;
    private boolean isLandSpace;
    private String mLiveId;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KeyboardUtils.hideKeyboardImmediately(getActivity());
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.vp_indicator, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mTicketNum = bundle.getInt(EXTRA_TICKET_COUNT, 0);
            mUuid = bundle.getLong(EXTRA_UUID, 0);
            mTicketStartNum = bundle.getInt(EXTRA_START_TICKET_COUNT, 0);
            mLiveId = bundle.getString(EXTRA_LIVEID);
            mFromType = bundle.getString(EXTRA_FROM_TYPE);
            isShowCurrent = bundle.getBoolean(IS_SHOW_CURRENT);
            isLandSpace = bundle.getBoolean(EXTRA_IS_LANDSCAPE);
        }
        if (mUuid == 0) {
            mUuid = UserAccountManager.getInstance().getUuidAsLong();
        }

        return rootView;
    }

    @Override
    protected void bindView() {
        mViewPager = $(R.id.id_vp);
        mIndicator = $(R.id.id_indicator);
        mBackTitleBar = $(R.id.title_bar);

        if (!isShowCurrent) {
            mIndicator.setIsCanChange(false);
            mViewPager.setNoScroll(true);
        }

        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_RANKING_SHOW, 1);
    }

    private void init() {
        initDatas();

        //设置Tab上的标题
        mIndicator.setTabItemTitles(mDatas);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);
        if (mFromType.equals(PARAM_FROM_CURRENT)) {
            mViewPager.setCurrentItem(1);
            //设置关联的ViewPager
            mIndicator.setViewPager(mViewPager, 1);
        } else if (mFromType.equals(PARAM_FROM_TOTAL)) {
            mViewPager.setCurrentItem(0);
            //设置关联的ViewPager
            mIndicator.setViewPager(mViewPager, 0);
        }
        mIndicator.resetTextView();
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_RANKING_CLICK_TOTAL, 1);
                } else {
                    StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_RANKING_CLICK_CURRENT, 1);
                }
            }
        });
    }

    private void initDatas() {
        mDatas = Arrays.asList(getResources().getStringArray(R.array.rank_type));
        {
            TotalRankingFragment totalFragment = new TotalRankingFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(BaseRankingFragment.EXTRA_TICKET_NUM, mTicketNum);
            bundle.putLong(BaseRankingFragment.EXTRA_UUID, mUuid);
            bundle.putString(BaseRankingFragment.EXTRA_LIVE_ID, mLiveId);
            totalFragment.setArguments(bundle);
            mTabContents.add(totalFragment);
        }

        {
            CurrentRankingFragment currentFragment = new CurrentRankingFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(BaseRankingFragment.EXTRA_TICKET_NUM, mTicketNum);
            bundle.putInt(BaseRankingFragment.EXTRA_TICKET_START, mTicketStartNum);
            bundle.putLong(BaseRankingFragment.EXTRA_UUID, mUuid);
            bundle.putString(BaseRankingFragment.EXTRA_LIVE_ID, mLiveId);
            currentFragment.setArguments(bundle);
            mTabContents.add(currentFragment);
        }

        mBackTitleBar.setTitle(R.string.rankTitle);
        mBackTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentNaviUtils.popFragmentFromStack(getActivity());
            }
        });

        mAdapter = new FragmentStatePagerAdapter(getFragmentManager()) {
            @Override
            public int getCount() {
                return mTabContents.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mTabContents.get(position);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewPager.setAdapter(null); // FragmentStatePagerAdapter不会移除已经添加的Fragment，强制其移除，防止内存泄漏
    }

    @Override
    public boolean onBackPressed() {
        FragmentNaviUtils.popFragmentFromStack(getActivity());
        return true;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!isLandSpace) {
            if (enter) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_in);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        init();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                return animation;
            } else {
                return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_out);
            }
        } else {
            if (enter) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_alpha_in);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        init();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                return animation;
            } else {
                return AnimationUtils.loadAnimation(getActivity(), R.anim.slide_alpha_out);
            }
        }
    }

    @Override
    public boolean isStatusBarDark() {
        return true;
    }

    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    public static void openFragment(BaseActivity activity, int ticket, int startTicket, long ownerId, String roomId, String type, boolean isSHow, boolean isLandspace) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(RankingPagerFragment.EXTRA_TICKET_COUNT, ticket);
        bundle.putInt(RankingPagerFragment.EXTRA_START_TICKET_COUNT, startTicket);
        bundle.putLong(RankingPagerFragment.EXTRA_UUID, ownerId);
        bundle.putString(RankingPagerFragment.EXTRA_LIVEID, roomId);
        bundle.putBoolean(BaseFragment.PARAM_FORCE_PORTRAIT, true);
        bundle.putString(RankingPagerFragment.EXTRA_FROM_TYPE, type);
        bundle.putBoolean(RankingPagerFragment.IS_SHOW_CURRENT, isSHow);

        bundle.putBoolean(RankingPagerFragment.EXTRA_IS_LANDSCAPE, isLandspace);
        FragmentNaviUtils.addFragment(activity, R.id.main_act_container, RankingPagerFragment.class, bundle, true, false, true);
    }
}
