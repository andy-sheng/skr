package com.wali.live.pay.fragment;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.base.view.BackTitleBar;
import com.base.view.SlidingTabLayout;
import com.live.module.common.R;
import com.wali.live.pay.activity.BalanceActivity;
import com.wali.live.pay.adapter.BalanceViewPagerAdapter;
import com.wali.live.pay.model.BalanceDetail;
import com.wali.live.recharge.presenter.IRechargePresenter;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;

/**
 * 余额界面<br/>
 *
 * @author caoxiangyu
 * @module 充值
 */
public class BalanceFragment extends RxFragment {
    public static String BUNDLE_KEY_BALANCE_DETAIL = "bundle_key_balance_detail";
    public static String BUNDLE_KEY_FROM = "from";
    public static String BUNDLE_VALUE_FROM_GIFT = "gift";

    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    SlidingTabLayout mSlidingTabLayout;
    ViewPager mBalanceViewPager;

    private static WeakReference<IRechargePresenter> presenterRef;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.balance_fragment, container, false);
    }

    @Override
    protected void bindView() {
        mSlidingTabLayout = (SlidingTabLayout) mRootView.findViewById(R.id.balance_sliding_tab);
        mBalanceViewPager = (ViewPager) mRootView.findViewById(R.id.balance_view_pager);

        BackTitleBar backTitleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        backTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        backTitleBar.getTitleTv().setText(R.string.balance_title);
        backTitleBar.getTitleTv().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mSlidingTabLayout.setCustomTabView(R.layout.balance_sliding_tab, R.id.tab_tv);
        mSlidingTabLayout.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_e5aa1e));
        mSlidingTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));//36px
        mSlidingTabLayout.setIndicatorTopMargin(DisplayUtils.dip2px(5.33f),/**16px*/
                new SlidingTabLayout.ITabNameBottomPositionGetter() {
                    @Override
                    public int getTabNameBottomPosition(@Nullable View selectedTitle) {
                        if (selectedTitle != null) {
                            View tabNameTv = selectedTitle.findViewById(R.id.tab_tv);
                            if (tabNameTv != null) {
                                return tabNameTv.getBottom();
                            }
                        }
                        return 0;
                    }
                });

        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        BalanceDetail balanceDetail = (BalanceDetail) bundle.getSerializable(BUNDLE_KEY_BALANCE_DETAIL);
        if (balanceDetail == null) {
            return;
        }
        mBalanceViewPager.setAdapter(BalanceViewPagerAdapter.obtain(balanceDetail, bundle.getString(BUNDLE_KEY_FROM)));
        mSlidingTabLayout.setViewPager(mBalanceViewPager);

    }

    @Override
    public void onResume() {
        super.onResume();
    }
    //@NonNull
    //private SpannableStringBuilder getDiamondSpan(int diamondCount, @ColorRes int colorResId) {
    //    String count = getResources().getQuantityString(R.plurals.diamond, diamondCount, diamondCount);
    //    SpannableStringBuilder ssb = new SpannableStringBuilder(count);
    //    String numStr = String.valueOf(diamondCount);
    //    int start = ssb.toString().indexOf(numStr);
    //    int end = start + numStr.length();
    //    ssb.setSpan(new ForegroundColorSpan(getResources().getColor(colorResId)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    //    return ssb;
    //}

    private SpannableStringBuilder getLeftDaySpan(int leftDay, @ColorRes int colorResId) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (leftDay < 0) {// 实际上不会走到这个分支
            ssb.append(getString(R.string.recharge_already_expired_tip));
        } else if (leftDay == 0) {
            ssb.append(getString(R.string.recharge_today_expire_tip));
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(colorResId)), 0, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            ssb.append(getString(R.string.recharge_expire_left_day_tip, leftDay));
            String numStr = String.valueOf(leftDay);
            int start = ssb.toString().indexOf(numStr);
            int end = start + numStr.length();
            ssb.setSpan(new ForegroundColorSpan(getResources().getColor(colorResId)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ssb;
    }

    private CharSequence getGiftCardInfo(BalanceDetail.GiftCard giftCard) {
        //String price = getResources().getQuantityString(R.plurals.diamond, giftCard.totalPrice, giftCard.totalPrice);
        String giftCardInfo = getString(R.string.balance_gift_card_count, giftCard.count, giftCard.giftName);
        return giftCardInfo;
        //SpannableStringBuilder ssb = new SpannableStringBuilder(giftCardInfo);
        //String numStr = String.valueOf(giftCard.count);
        ////TODO 如何确定数量的起始位置？当礼物价格为1钻的时候，如果在某些语言中总价格的位置在礼物数量的前面的时候，就会出现给总价格改变颜色的问题
        //int start = ssb.toString().indexOf(numStr);
        //int end = start + numStr.length();
        //ssb.setSpan(new ForegroundColorSpan(getResources().getColor(colorResId)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //return ssb;
    }

    @Override
    public void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    /**
     * 同时只能开一个这种Fragment<br/>
     *
     * @param activity     必须包含id为main_act_container的view
     * @param bundle
     * @param presenterRef
     * @return
     */
    @Nullable
    public static BalanceFragment openFragment(BaseActivity activity, @NonNull Bundle bundle, WeakReference<IRechargePresenter> presenterRef) {
        BalanceFragment.presenterRef = presenterRef;
        return (BalanceFragment) FragmentNaviUtils.addFragment(activity, R.id.main_act_container, BalanceFragment.class, bundle, true, false, true);
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() instanceof BalanceActivity) {
            getActivity().onBackPressed();
            return true;
        } else {
            FragmentNaviUtils.popFragmentFromStack(getActivity());
            if (presenterRef != null && presenterRef.get() != null) {
                presenterRef.get().showPopup();
            }
            presenterRef = null;
            return true;
        }
    }
}

