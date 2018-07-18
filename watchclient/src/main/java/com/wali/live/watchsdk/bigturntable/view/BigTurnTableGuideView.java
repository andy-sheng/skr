package com.wali.live.watchsdk.bigturntable.view;

import android.content.Context;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.display.DisplayUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.preference.PreferenceKeys;
import com.wali.live.watchsdk.R;

import java.util.concurrent.TimeUnit;
import rx.functions.Action1;

/**
 * Created by zhujianning on 18-4-25.
 */

public class BigTurnTableGuideView extends PopupWindow {
    private static final String TAG = "BigTurnTableGuideView";
    private static final int WIDTH_TIPS = DisplayUtils.dip2px(204.67f);
    private static final int WIDTH_ICON = DisplayUtils.dip2px(36.67f);
    private Context mContext;
    private RelativeLayout mContainer;
    private TextView mGuideIv;

    private Runnable mStartAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            startGuideAnimation();
        }
    };

    public BigTurnTableGuideView(Context context) {
        super(context);
        this.mContext = context;
        init(context);
    }

    private void init(Context context) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.big_turn_table_guide_view, null);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setBackgroundDrawable(null);
        this.setContentView(inflate);
        mContainer = (RelativeLayout) inflate.findViewById(R.id.container);
        mGuideIv = (TextView) inflate.findViewById(R.id.guide_icon_tv);
        RxView.clicks(mContainer)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        dismiss();
                    }
                });
    }

    public void show(View view, int offsetX, int offsetY){
        int rigihtMargin = DisplayUtils.getScreenWidth() - offsetX - (WIDTH_TIPS / 2) - (WIDTH_ICON / 2);
        if(rigihtMargin < 0) {
            rigihtMargin = 0;
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGuideIv.getLayoutParams();
        params.rightMargin = rigihtMargin;
        mGuideIv.setLayoutParams(params);
        this.showAtLocation(view, Gravity.NO_GRAVITY, 0, 0);
        view.postDelayed(mStartAnimationRunnable, 300);
    }

    private void startGuideAnimation() {
        MyLog.w(TAG, " startGuideAnimation");
        mGuideIv.setVisibility(View.VISIBLE);
        Animation scaleAnimation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_scale_in_big_turn_table_guide);
        mGuideIv.startAnimation(scaleAnimation);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showGuideUpAndDownAnim(mGuideIv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.PRE_KEY_HAS_SHOWED_BIG_TURN_TABLE_GUIDE, true);
    }

    private void showGuideUpAndDownAnim(final View view) {
        MyLog.d(TAG, " showGuideUpAndDownAnim");
        Animation animation = AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_big_turn_table_guide_up_down);
        view.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showGuideOutAnim(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showGuideOutAnim(View view) {
        ScaleAnimation scaleAnimation = (ScaleAnimation) AnimationUtils.loadAnimation(GlobalData.app(), R.anim.anim_scale_out_big_turn_table_guide);
        view.startAnimation(scaleAnimation);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void cancelAnimation() {
        MyLog.w(TAG, " cancelAnimation");
        if (mGuideIv != null) {
            mGuideIv.clearAnimation();
            mGuideIv.removeCallbacks(mStartAnimationRunnable);
            mGuideIv.setVisibility(View.GONE);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        cancelAnimation();
    }
}
