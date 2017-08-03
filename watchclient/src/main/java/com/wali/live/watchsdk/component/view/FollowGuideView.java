package com.wali.live.watchsdk.component.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IOrientationListener;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;

/**
 * Created by zyh on 2017/07/13.
 * <p>
 *
 * @module 游戏直播间内的观众引导的view
 */
public class FollowGuideView extends RelativeLayout implements IComponentView<FollowGuideView.IPresenter
        , FollowGuideView.IView>, View.OnClickListener, IOrientationListener {
    private static final String TAG = "FollowGuideView";

    @Nullable
    protected IPresenter mPresenter;

    private TextView mNameTv;
    private TextView mCloseTipTv;
    private TextView mFollowTv;
    private SimpleDraweeView mAvatarDv;

    private RoomBaseDataModel mMyRoomData;
    private int mMarginBottomLandscape = 223;
    private int mCountDownTime = 10;

    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public FollowGuideView(Context context) {
        this(context, null);
    }

    public FollowGuideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FollowGuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.game_guide_layout, this);
        mAvatarDv = $(R.id.avatar_dv);
        mNameTv = $(R.id.name_tv);
        mCloseTipTv = $(R.id.close_tips_tv);
        mFollowTv = $(R.id.follow_tv);
        $click(mFollowTv, this);
        $click($(R.id.close_btn), this);
    }

    public void setMyRoomData(RoomBaseDataModel roomBaseDataModel) {
        mMyRoomData = roomBaseDataModel;
        if (mMyRoomData == null || mMyRoomData.getUid() <= 0
                || TextUtils.isEmpty(mMyRoomData.getRoomId())
                || mMyRoomData.isFocused()) {
            setVisibility(GONE);
            return;
        }
        String url = AvatarUtils.getAvatarUrlByUidTs(mMyRoomData.getUid(), AvatarUtils.SIZE_TYPE_AVATAR_SMALL, mMyRoomData.getAvatarTs());
        FrescoWorker.loadImage(mAvatarDv, ImageFactory.newHttpImage(url)
                .setIsCircle(true)
                .setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.color.color_f2f2f2))
                .setFailureDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_b))
                .setBorderWidth(6)
                .setBorderColor(GlobalData.app().getResources().getColor(R.color.color_e5e5e5))
                .build());
        mNameTv.setText(!TextUtils.isEmpty(mMyRoomData.getNickName()) ?
                mMyRoomData.getNickName() :
                String.valueOf(mMyRoomData.getUid()));
        showSelf();
    }

    private void showSelf() {
        if (mHideAnimator != null && mHideAnimator.isRunning()) {
            mHideAnimator.cancel();
        }
        if (mShowAnimator == null) {
            mShowAnimator = ObjectAnimator.ofFloat(this, "alpha", 0, 1);
            mShowAnimator.setDuration(500);
            mShowAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP,
                            String.format(StatisticsKey.KEY_SDK_FOLLOW_WINDOWS_SHOW,
                                    mMyRoomData.getRoomId()), 1);
                    setVisibility(VISIBLE);
                    mPresenter.countDownIn(mCountDownTime);
                }
            });
        }
        if (!mShowAnimator.isStarted()) {
            mShowAnimator.start();
        }
    }

    private void hideSelf(boolean useAnimation) {
        if (mShowAnimator != null && mShowAnimator.isRunning()) {
            mShowAnimator.cancel();
        }
        if (useAnimation) {
            if (mHideAnimator == null) {
                mHideAnimator = ObjectAnimator.ofFloat(this, "alpha", 1, 0);
                mHideAnimator.setDuration(500);
                mHideAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        setVisibility(GONE);
                    }
                });
            }
            if (!mHideAnimator.isStarted()) {
                mHideAnimator.start();
            }
        } else {
            setVisibility(GONE);
        }
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        MyLog.w(TAG, "onOrientation isLandScape=" + isLandscape);
        LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        if (isLandscape) {
            int min = Math.min(DisplayUtils.getScreenWidth(), DisplayUtils.getScreenHeight());
            layoutParams.bottomMargin = (min - getHeight()) / 2;
        } else {
            layoutParams.bottomMargin = mMarginBottomLandscape;
        }
        setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.close_btn) {
            hideSelf(true);
            StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP,
                    String.format(StatisticsKey.KEY_SDK_FOLLOW_WINDOWS_CLOSE,
                            mMyRoomData.getRoomId()), 1);
        } else if (i == R.id.follow_tv) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.follow(mMyRoomData.getUid(), mMyRoomData.getRoomId());
            }
            StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP,
                    String.format(StatisticsKey.KEY_SDK_FOLLOW__WINDOWS_FOLLOW,
                            mMyRoomData.getRoomId()), 1);
        }
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {

            @Override
            public <T extends View> T getRealView() {
                return (T) FollowGuideView.this;
            }

            @Override
            public void onFollowSuc() {
                mFollowTv.setText(R.string.already_followed);
                FollowGuideView.this.hideSelf(true);
            }

            @Override
            public void updateCountDown(int countDownTime) {
                mCloseTipTv.setText(String.format(getResources().
                        getString(R.string.game_guide_close_tip), countDownTime));
            }

            @Override
            public void hideSelf(boolean useAnimation) {
                FollowGuideView.this.hideSelf(useAnimation);
            }

            @Override
            public void onScreenChanged(boolean isLandscape) {
                FollowGuideView.this.onOrientation(isLandscape);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 关注行为
         */
        void follow(long targetId, String roomId);

        /**
         * view显示时的倒计时函数
         */
        void countDownIn(int time);
    }

    public interface IView extends IViewProxy {
        /**
         * 关注成功后行为
         */
        void onFollowSuc();

        /**
         * 更新倒计时ui上的时间显示
         */
        void updateCountDown(int countDownTime);

        /**
         * view自身隐藏行为
         */
        void hideSelf(boolean useAnimation);

        /**
         * 旋转屏处理
         */
        void onScreenChanged(boolean isLandscape);
    }
}
