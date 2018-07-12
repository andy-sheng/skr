package com.wali.live.watchsdk.bigturntable.view;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.wali.live.watchsdk.R;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-4-2.
 */

public class BigTurnTableRuleView extends PopupWindow {

    private Context mContext;
    private RelativeLayout mContainer;
    private RelativeLayout mTipsContainer;
    private TextView mKnowTv;
    private boolean mIsLandscape;
    private RelativeLayout.LayoutParams mTipsContainerParams;

    public BigTurnTableRuleView(Context context) {
        super(context);
        this.mContext = context;
        init(context);
    }

    private void init(Context context) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.big_turn_table_rule_view, null);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setBackgroundDrawable(null);
        this.setContentView(inflate);
        mContainer = (RelativeLayout) inflate.findViewById(R.id.container);
        mTipsContainer = (RelativeLayout) inflate.findViewById(R.id.tip_container);
        mTipsContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mTipsContainerParams = (RelativeLayout.LayoutParams) mTipsContainer.getLayoutParams();
        mKnowTv = (TextView) inflate.findViewById(R.id.know_tv);
        RxView.clicks(mContainer)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        tryToDismiss();
                    }
                });
        RxView.clicks(mKnowTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        tryToDismiss();
                    }
                });
    }

    private void tryToDismiss() {
        dismiss();
    }

    public void show(View view){
        this.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
    }

    public void switchOrient(boolean isLandscape) {
        if(mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            if(mIsLandscape) {
                mTipsContainerParams.width = DisplayUtils.dip2px(420f);
                mTipsContainerParams.height = DisplayUtils.dip2px(276.67f);
            } else {
                mTipsContainerParams.width = DisplayUtils.dip2px(297.67f);
                mTipsContainerParams.height = DisplayUtils.dip2px(340f);
            }

            mTipsContainer.setLayoutParams(mTipsContainerParams);
        }
    }
}
