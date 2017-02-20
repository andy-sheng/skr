package com.wali.live.common.gift.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.live.module.common.R;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.functions.Action1;

/**
 * Created by zjn on 16-9-5.
 *
 * @Module 礼物橱窗
 */
public class GiftDiamondTips extends RelativeLayout{

    private TextView mDiamondTipsClose;

    private TextView mTipsText;

    private RelativeLayout mTipsContainer;

    private static final int TIPS_TYPE_BALANCE = 1;

    private static final int TIPS_TYPE_SILIVER_DIAMOND = 2;

    private static final int TIPS_TYPE_MI_COIN = 3;

    private int mTipsType;

    public GiftDiamondTips(Context context, int tipsType) {
        super(context);
        mTipsType = tipsType;
        init(context);
    }

    public GiftDiamondTips(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiftDiamondTips(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.siliver_diamond_tips_view, this);
        bindView();
    }

    private void bindView() {
        mDiamondTipsClose = (TextView) findViewById(R.id.siliver_diamond_tips_close);
        mTipsText = (TextView) findViewById(R.id.gift_tips_tv);
        mTipsContainer = (RelativeLayout) findViewById(R.id.toast_container);

        mTipsText.setMaxWidth(DisplayUtils.dip2px(260));
        if(mTipsType == TIPS_TYPE_MI_COIN) {
            mTipsText.setText(getResources().getString(R.string.gift_mall_mi_coin_tips));
        } else if(mTipsType == TIPS_TYPE_BALANCE) {
            mTipsText.setText(getResources().getString(R.string.gift_mall_balance_tips));
        } else {
            mTipsText.setText(getResources().getString(R.string.gift_mall_silver_balance_tips));
        }

        RxView.clicks(mDiamondTipsClose)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        setVisibility(GONE);

                        mOnShowDiamondTipListener.onCloseTips();
                    }
                });
    }

    public void changeTipsBackGroup(boolean isLandscape) {
        if(isLandscape) {
            mTipsContainer.setBackgroundResource(R.drawable.toast_landscape_bg);
        } else {
            mTipsContainer.setBackgroundResource(R.drawable.toast_bg);
        }
    }

    private onShowDiamondTipListener mOnShowDiamondTipListener;

    public void setOnShowDiamondTipListener(onShowDiamondTipListener l) {
        mOnShowDiamondTipListener = l;
    }

    public interface onShowDiamondTipListener {
        void onCloseTips();
    }
}
