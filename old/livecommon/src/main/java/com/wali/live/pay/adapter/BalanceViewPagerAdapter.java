package com.wali.live.pay.adapter;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.wali.live.pay.fragment.BalanceFragment;
import com.wali.live.pay.model.BalanceDetail;
import com.wali.live.pay.view.BalanceItemView;

/**
 * 余额ViewPager的适配器
 *
 * @module 余额
 * Created by rongzhisheng on 16-12-10.
 */

public class BalanceViewPagerAdapter extends PagerAdapter {
    private static final int PAGE_COUNT = 2;
    private static final int ITEM_HEIGHT = DisplayUtils.dip2px(63.33f);
    private BalanceDetail mBalanceDetail;
    private String mFrom;

    private BalanceViewPagerAdapter() {
    }

    public static BalanceViewPagerAdapter obtain(@NonNull BalanceDetail balanceDetail, String from) {
        BalanceViewPagerAdapter adapter = new BalanceViewPagerAdapter();
        adapter.mBalanceDetail = balanceDetail;
        adapter.mFrom = from;
        return adapter;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return getViewKey(view) == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return GlobalData.app().getString(R.string.can_use_text);
            case 1:
                return GlobalData.app().getString(R.string.out_date_text);
        }
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        switch (position) {
            case 0:
                return getViewKey(getEffectivePage(container));
            case 1:
                return getViewKey(getExpiredPage(container));
        }
        return null;
    }

    private View getEffectivePage(ViewGroup container) {
        Context context = container.getContext();
        LayoutInflater factory = LayoutInflater.from(context);
        View root = factory.inflate(R.layout.balance_pager, container, false);
        ViewHolder viewHolder = new ViewHolder(root);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ITEM_HEIGHT);

        BalanceItemView balanceItemView = new BalanceItemView(context, BalanceItemView.ItemType.ITEM_TYPE_GOLD_GEM,
                true, GlobalData.app().getResources().getQuantityString(R.plurals.recharge_gold_diamond,
                mBalanceDetail.getDiamondCount(), mBalanceDetail.getDiamondCount()), null, null);
        // 添加金钻
        viewHolder.mGemContainer.addView(balanceItemView, params);
        if (mBalanceDetail.getVirtualDiamondList().isEmpty() || mBalanceDetail.getGiftCardList().isEmpty()) {
            balanceItemView.setSeparatorVisibility(View.GONE);
        }
        balanceItemView = null;
        // 生效的虚拟钻石
        for (BalanceDetail.VirtualDiamond virtualDiamond : mBalanceDetail.getVirtualDiamondList()) {
            balanceItemView = new BalanceItemView(context, BalanceItemView.ItemType.ITEM_TYPE_SILVER_GEM,
                    false, GlobalData.app().getResources().getQuantityString(R.plurals.recharge_silver_diamond, virtualDiamond.count, virtualDiamond.count),
                    virtualDiamond.expireDate, getLeftDaySpan(virtualDiamond.leftDay, R.color.color_e5aa1e));
            viewHolder.mGemContainer.addView(balanceItemView, params);
        }
        if (balanceItemView != null) {
            balanceItemView.setSeparatorVisibility(View.GONE);
            balanceItemView = null;
        }

        //如果是从直播间 礼物橱窗 包裹礼物 详情来的，则隐藏金钻区域
        if(!TextUtils.isEmpty(mFrom) && mFrom.equals(BalanceFragment.BUNDLE_VALUE_FROM_GIFT)){
            viewHolder.mGemContainer.setVisibility(View.GONE);
            viewHolder.mGemTitle.setVisibility(View.GONE);
            viewHolder.mTipContainer.setVisibility(View.GONE);
            viewHolder.mPktTip.setVisibility(View.VISIBLE);
        }

        // 生效的礼物
        for (BalanceDetail.GiftCard giftCard : mBalanceDetail.getGiftCardList()) {
            balanceItemView = new BalanceItemView(context, BalanceItemView.ItemType.ITEM_TYPE_GIFT_CARD,
                    false, getGiftCardInfo(giftCard), giftCard.expireDate,
                    getLeftDaySpan(giftCard.leftDay, R.color.color_e5aa1e));
            viewHolder.mBackpackContainer.addView(balanceItemView, params);
        }
        if (balanceItemView != null) {
            balanceItemView.setSeparatorVisibility(View.GONE);
        }
        viewHolder.mBackpackTitle.setVisibility(mBalanceDetail.getGiftCardList().isEmpty() ? View.GONE : View.VISIBLE);
        container.addView(root);
        return root;
    }

    private View getExpiredPage(ViewGroup container) {
        Context context = container.getContext();
        LayoutInflater factory = LayoutInflater.from(context);
        View root = factory.inflate(R.layout.balance_pager, container, false);
        ViewHolder viewHolder = new ViewHolder(root);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ITEM_HEIGHT);

        BalanceItemView balanceItemView = null;
        // 已经过期的虚拟钻石
        for (BalanceDetail.VirtualDiamond virtualDiamond : mBalanceDetail.getExpiredVirtualDiamondList()) {
            balanceItemView = new BalanceItemView(context, BalanceItemView.ItemType.ITEM_TYPE_SILVER_GEM,
                    false, GlobalData.app().getResources().getQuantityString(R.plurals.recharge_silver_diamond, virtualDiamond.count, virtualDiamond.count),
                    virtualDiamond.expireDate);
            viewHolder.mGemContainer.addView(balanceItemView, params);
        }
        if (balanceItemView != null) {
            balanceItemView.setSeparatorVisibility(View.GONE);
            balanceItemView = null;
        }
        viewHolder.mGemTitle.setVisibility(mBalanceDetail.getExpiredVirtualDiamondList().isEmpty() ? View.GONE : View.VISIBLE);

        //如果是从直播间 礼物橱窗 包裹礼物 详情来的，则隐藏金钻区域
        if(!TextUtils.isEmpty(mFrom) && mFrom.equals(BalanceFragment.BUNDLE_VALUE_FROM_GIFT)){
            viewHolder.mTipContainer.setVisibility(View.GONE);
            viewHolder.mPktTip.setVisibility(View.VISIBLE);
        }

        // 过期的礼物
        for (BalanceDetail.GiftCard giftCard : mBalanceDetail.getExpiredGiftCardList()) {
            balanceItemView = new BalanceItemView(context, BalanceItemView.ItemType.ITEM_TYPE_GIFT_CARD,
                    false, getGiftCardInfo(giftCard), giftCard.expireDate);
            viewHolder.mBackpackContainer.addView(balanceItemView, params);
        }
        if (balanceItemView != null) {
            balanceItemView.setSeparatorVisibility(View.GONE);
        }
        viewHolder.mBackpackTitle.setVisibility(mBalanceDetail.getExpiredGiftCardList().isEmpty() ? View.GONE : View.VISIBLE);

        if (viewHolder.mGemTitle.getVisibility() == View.GONE
                && viewHolder.mBackpackTitle.getVisibility() == View.GONE) {
            viewHolder.mNoEmptySection.setVisibility(View.GONE);
            int emptyTipHeight = DisplayUtils.getScreenHeight()
                    // 状态栏高度 + BackTitleBar的高度（不算状态栏） + Tab高度
                    - (BaseActivity.getStatusBarHeight()
                    + GlobalData.app().getResources().getDimensionPixelSize(com.base.common.R.dimen.title_bar_height)
                    + GlobalData.app().getResources().getDimensionPixelSize(R.dimen.height_130));
            ViewGroup.LayoutParams layoutParams = viewHolder.mEmptySection.getLayoutParams();
            layoutParams.height = emptyTipHeight;
            viewHolder.mEmptySection.setLayoutParams(layoutParams);

            int measureSpec = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
            viewHolder.mEmptyTipTv.measure(measureSpec, measureSpec);
            int marginBottom = (DisplayUtils.getScreenHeight() - viewHolder.mEmptyTipTv.getMeasuredHeight()) / 2;
            viewHolder.mEmptyTipTv.setPadding(0, 0, 0, marginBottom);

            viewHolder.mEmptySection.setVisibility(View.VISIBLE);
        }
        container.addView(root);
        return root;
    }

    private Object getViewKey(View view) {
        return view;
    }

    private SpannableStringBuilder getLeftDaySpan(int leftDay, @ColorRes int colorResId) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (leftDay < 0) {// 实际上不会走到这个分支
            ssb.append(GlobalData.app().getString(R.string.recharge_already_expired_tip));
        } else if (leftDay == 0) {
            ssb.append(GlobalData.app().getString(R.string.recharge_today_expire_tip));
            ssb.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorResId)), 0, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            ssb.append(GlobalData.app().getString(R.string.recharge_expire_left_day_tip, leftDay));
            String numStr = String.valueOf(leftDay);
            int start = ssb.toString().indexOf(numStr);
            int end = start + numStr.length();
            ssb.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorResId)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ssb;
    }

    private CharSequence getGiftCardInfo(BalanceDetail.GiftCard giftCard) {
        //String price = GlobalData.app().getResources().getQuantityString(R.plurals.diamond, giftCard.totalPrice, giftCard.totalPrice);
        CharSequence giftCardInfo = GlobalData.app().getString(R.string.balance_gift_card_count, giftCard.count, giftCard.giftName);
        return giftCardInfo;
        //SpannableStringBuilder ssb = new SpannableStringBuilder(giftCardInfo);
        //String numStr = String.valueOf(giftCard.count);
        ////TODO 如何确定数量的起始位置？当礼物价格为1钻的时候，如果在某些语言中总价格的位置在礼物数量的前面的时候，就会出现给总价格改变颜色的问题
        //int start = ssb.toString().indexOf(numStr);
        //int end = start + numStr.length();
        //ssb.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(colorResId)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //return ssb;
    }

}

class ViewHolder {
    View mNoEmptySection;
    View mEmptySection;
    View mGemTitle;
    LinearLayout mGemContainer;
    View mBackpackTitle;
    LinearLayout mBackpackContainer;
    TextView mEmptyTipTv;
    LinearLayout mTipContainer;
    TextView mPktTip;

    ViewHolder(@NonNull View view) {
        mNoEmptySection = view.findViewById(R.id.no_empty_section);
        mEmptySection = view.findViewById(R.id.empty_tip_section);
        mGemTitle = mNoEmptySection.findViewById(R.id.gem_title);
        mGemContainer = (LinearLayout) mNoEmptySection.findViewById(R.id.gem_container);
        mBackpackTitle = mNoEmptySection.findViewById(R.id.backpack_title);
        mBackpackContainer = (LinearLayout) mNoEmptySection.findViewById(R.id.backpack_container);
        mEmptyTipTv = (TextView) mEmptySection.findViewById(R.id.empty_tip_tv);
        mTipContainer = (LinearLayout) view.findViewById(R.id.ll_tip_container);
        mPktTip = (TextView) view.findViewById(R.id.tv_pkt_tip1);
    }
}