package com.wali.live.watchsdk.bigturntable.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.date.DateTimeUtils;
import com.mi.live.data.repository.model.turntable.PrizeItemModel;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.bigturntable.contact.BigTurnTableViewContact;
import com.wali.live.watchsdk.bigturntable.presenter.BigTurnTableViewPresenter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zhujianning on 18-7-10.
 */

public class BigTurnTableView extends RelativeLayout {
    private static final String TAG = "BigTurnTableView";

    //data
    private List<PrizeItemModel> mDatas;
    private int mMinTime;
    private int mLastPos;
    private boolean mIsRotating;
    private boolean mIsShowLant = true;

    //ui
    private BigTurnTableContentView mBigTurnTableView;
    private TextView mBeforeBg;
    private RelativeLayout mContainer;

    private Handler mHandler = new Handler();

    private OnBigTurnTableListener mOnBigTurnTableListener;

    private BigTurnTableViewPresenter mPresenter;

    public BigTurnTableView(Context context) {
        this(context, null);
    }

    public BigTurnTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigTurnTableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.trun_table_panel_view, this);
        mMinTime = 3;
        mBigTurnTableView = (BigTurnTableContentView) findViewById(R.id.turn_table_view);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mBeforeBg = (TextView) findViewById(R.id.lant_tv);

        initPresenter();

        mHandler.postDelayed(mSwitchRunnable, 200);
    }

    private void initPresenter() {
        mPresenter = new BigTurnTableViewPresenter(new BigTurnTableViewContact.IView() {
            @Override
            public void loadBmpsSuccess() {
                HashMap<String, Bitmap> bmpMap = mPresenter.getBmpMap();
                if(!bmpMap.isEmpty()) {
                    mBigTurnTableView.setDatas(mDatas, bmpMap);
                } else {
                    MyLog.w(TAG, "load big turn table drawable fail");
                }
            }
        });
    }

    public void changeBigTurnTableBg(boolean isBig) {
        mContainer.setBackground(GlobalData.app().getResources().getDrawable(isBig ? R.drawable.big_turn_table_b_bg : R.drawable.big_turn_table_s_bg));
    }

    public void startRotate(final int pos, final String prizeKey) {
        int newAngle = (int) (360 * mMinTime - pos * mBigTurnTableView.getAngle() + mBigTurnTableView.getCurAngle() + (mLastPos == 0 ? 0 : ((mLastPos) * mBigTurnTableView.getAngle())));
        MyLog.d(TAG, "lottery time:" + DateTimeUtils.formatFeedsJournalCreateData(System.currentTimeMillis(), System.currentTimeMillis()) +
                "startRotate pos:" + pos + ", newAngle:" + newAngle + ", lastPos:" + mLastPos + ", curAngle:" + mBigTurnTableView.getCurAngle() + ", angle:" + mBigTurnTableView.getAngle());
        int num = (int) ((newAngle - mBigTurnTableView.getCurAngle()) / mBigTurnTableView.getAngle());
        ObjectAnimator anim = ObjectAnimator.ofFloat(BigTurnTableView.this, "rotation", mBigTurnTableView.getCurAngle(), newAngle - mBigTurnTableView.getAngle() / 2);
        mBigTurnTableView.setCurAngle(newAngle);
        mLastPos = pos;
        anim.setDuration(num * 80);
        final float[] f = {0};
        anim.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                f[0] = (float) (Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
                return f[0];
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mHandler.removeCallbacks(mSwitchRunnable);
                mIsRotating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mHandler != null) {
                    mHandler.postDelayed(mSwitchRunnable, 200);
                }
                mOnBigTurnTableListener.onRotateAnimatorFinish(mDatas.get(pos), prizeKey);
                mIsRotating = false;
            }
        });
        anim.start();
    }

    public void setDatas(List<PrizeItemModel> datas) {
        MyLog.d(TAG, "setDatas");
        this.mDatas = datas;
        mPresenter.loadBmps(mDatas);
    }

    public boolean isRotating() {
        return mIsRotating;
    }

    public void switchMode(BigTurnTableProto.TurntableType mode) {
        mPresenter.changeMode(mode);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public void destory() {
        if(mPresenter != null) {
            mPresenter.destroy();
        }
        mBigTurnTableView.destory();
    }

    private Runnable mSwitchRunnable = new Runnable() {
        @Override
        public void run() {
            if(mBeforeBg.getVisibility() == VISIBLE) {
                mBeforeBg.setVisibility(GONE);
            } else {
                mBeforeBg.setVisibility(VISIBLE);
            }

            if(mBeforeBg.getVisibility() == VISIBLE) {
                if(mIsShowLant) {
                    mBeforeBg.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.big_turn_table_content_lant_1));
                    mIsShowLant = false;
                } else {
                    mBeforeBg.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.big_turn_table_content_lant));
                    mIsShowLant = true;
                }
            }

            mHandler.postDelayed(mSwitchRunnable, 200);
        }
    };

    public void setOnBigTurnTableListener(OnBigTurnTableListener listener) {
        this.mOnBigTurnTableListener = listener;
    }

    public interface OnBigTurnTableListener {
        void onRotateAnimatorFinish(PrizeItemModel data, String prizeKey);
    }
}
