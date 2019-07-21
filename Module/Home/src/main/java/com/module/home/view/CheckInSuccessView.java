package com.module.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.module.home.R;
import com.module.home.model.HomeGoldModel;

public class CheckInSuccessView extends RelativeLayout {
    public final String TAG = "CheckInSuccessView";
    ExRelativeLayout mRlGoldContent;
    ImageView mIvGold;
    TextView mTvGold;
    ExTextView mIvConfirm;
    ExImageView mIvAnimate;

    public CheckInSuccessView(Context context) {
        super(context);
        init();
    }

    public CheckInSuccessView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckInSuccessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.checkin_success_view_layout, this);

        mIvAnimate = (ExImageView) findViewById(R.id.iv_animate);
        mRlGoldContent = (ExRelativeLayout) findViewById(R.id.rl_gold_content);
        mIvGold = (ImageView) findViewById(R.id.iv_gold);
        mTvGold = (TextView) findViewById(R.id.tv_gold);
        mIvConfirm = (ExTextView) findViewById(R.id.iv_confirm);
    }

    public View getIvConfirm() {
        return mIvConfirm;
    }

    public void setData(HomeGoldModel homeGoldModel) {
        HomeGoldModel.BonusesBean bonusesBean = homeGoldModel.getCoinBonuses();
        if (bonusesBean == null) {
            MyLog.e(TAG, "bonusesBean is null");
            return;
        }

        mTvGold.setText(bonusesBean.getAmount() + "金币");
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mIvAnimate.setAnimation(rotateAnimation);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        rotateAnimation.setDuration(2500);
        mIvAnimate.startAnimation(rotateAnimation);

        switch (homeGoldModel.getSeq()) {
            case 1:
                mIvGold.setImageDrawable(U.getDrawable(R.drawable.wujinbi_moren));
                break;
            case 2:
                mIvGold.setImageDrawable(U.getDrawable(R.drawable.shijinbi_moren));
                break;
            case 3:
                mIvGold.setImageDrawable(U.getDrawable(R.drawable.shijinbi_moren));
                break;
            case 4:
                mIvGold.setImageDrawable(U.getDrawable(R.drawable.shiwujinbi_moren));
                break;
            case 5:
                mIvGold.setImageDrawable(U.getDrawable(R.drawable.shiwujinbi_moren));
                break;
            case 6:
                mIvGold.setImageDrawable(U.getDrawable(R.drawable.ershijinbi_moren));
                break;
            case 7:
                mIvGold.setImageDrawable(U.getDrawable(R.drawable.sanshijinbi_moren));
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIvAnimate.clearAnimation();
    }
}
