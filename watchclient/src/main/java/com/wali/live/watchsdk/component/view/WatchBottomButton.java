package com.wali.live.watchsdk.component.view;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.HostChannelManager;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.component.view.BaseBottomButton;
import com.wali.live.component.view.IOrientationListener;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by yangli on 16-8-29.
 *
 * @module 底部按钮视图, 游戏直播
 */
public class WatchBottomButton extends BaseBottomButton<WatchBottomButton.IPresenter, WatchBottomButton.IView> {
    private static final String TAG = "WatchBottomButton";

    protected View mCommentBtn;
    protected View mGiftBtn;
    //    protected View mRotateBtn;
    protected View mGameBtn;
    protected View mShareBtn;

    private boolean mIsGameMode = false;
    private boolean mEnableShare;

    private Runnable mAnimatorRunnable;
    private ValueAnimator mShakeAnimator;

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public void onClick(View view) {
        if (mPresenter == null) {
            return;
        }
        int id = view.getId();
        String msgType = "";
        if (id == R.id.comment_btn) {
            mPresenter.showInputView();
            msgType = StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_SENDMESSAGE;
        } else if (id == R.id.gift_btn) {
            mPresenter.showGiftView();
        } else if (id == R.id.rotate_btn) {
            mPresenter.rotateScreen();
        } else if (id == R.id.game_btn) {
            mPresenter.showGameDownloadView();
        } else if (id == R.id.share_btn) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.showShareView();
            }
        }
        if (!TextUtils.isEmpty(msgType)) {
            StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    String.format(msgType, HostChannelManager.getInstance().getChannelId()),
                    TIMES, "1");
        }
    }

    public WatchBottomButton(
            @NonNull RelativeLayout contentContainer,
            boolean isGameMode, boolean enableShare) {
        super(contentContainer);
        mIsGameMode = isGameMode;
        mEnableShare = enableShare;
        initView();
    }

    protected void initView() {
        mCommentBtn = createImageView(R.drawable.live_icon_comment_btn);
        addCreatedView(mCommentBtn, R.id.comment_btn);

        mGiftBtn = createImageView(R.drawable.live_icon_gift_btn);
        addCreatedView(mGiftBtn, R.id.gift_btn);

//        mRotateBtn = createImageView(R.drawable.live_icon_rotate_screen);
//        addCreatedView(mGiftBtn, R.id.rotate_btn);

        // 横竖屏时按钮排列顺序
        mLeftBtnSetPort.add(mCommentBtn);
        mRightBtnSetPort.add(mGiftBtn);

        mBottomBtnSetLand.add(mGiftBtn);
        mBottomBtnSetLand.add(mCommentBtn);

        //mBottomBtnSetLand.add(mRotateBtn);

        addShareBtn();

        orientChild();
    }

    private void addShareBtn() {
        if (mEnableShare) {
            mShareBtn = createImageView(R.drawable.live_icon_share_btn);
            addCreatedView(mShareBtn, R.id.share_btn);

            mRightBtnSetPort.add(mShareBtn);
            mBottomBtnSetLand.add(mShareBtn);
        }
    }

    @Override
    protected void orientSelf() {
        super.orientSelf();
        if (mIsGameMode && mIsLandscape) {
            mCommentBtn.setVisibility(View.GONE);
        } else {
            mCommentBtn.setVisibility(View.VISIBLE);
        }
    }

    private void showGameIcon(final GameViewModel gameModel) {
        if (gameModel == null) {
            MyLog.w(TAG, "showGameIcon gameModel is null");
            return;
        }
        mGameBtn = new SimpleDraweeView(getContext());
        addCreatedView(mGameBtn, mCommentBtn.getWidth(), mCommentBtn.getHeight(), R.id.game_btn);

        // ImageFactory.newResImage(R.drawable.live_icon_game_btn).build();
        BaseImage image = ImageFactory.newHttpImage(gameModel.getIconUrl()).setCornerRadius(10).build();
        FrescoWorker.loadImage((SimpleDraweeView) mGameBtn, image);

        mRightBtnSetPort.add(mGameBtn);
        mBottomBtnSetLand.add(mGameBtn);
        orientChild();

        if (mAnimatorRunnable == null) {
            mAnimatorRunnable = new Runnable() {
                @Override
                public void run() {
                    startGameAnimator();
                }
            };
        }
        mGameBtn.postDelayed(mAnimatorRunnable, 60 * 1000);
    }

    private void startGameAnimator() {
        if (mShakeAnimator == null) {
            mGameBtn.setPivotX(mGameBtn.getWidth() >> 1);
            mGameBtn.setPivotY(mGameBtn.getHeight() >> 1);

            mShakeAnimator = ValueAnimator.ofInt(0, 1100);
            mShakeAnimator.setDuration(2200);
            mShakeAnimator.setRepeatMode(ValueAnimator.RESTART);
            mShakeAnimator.setInterpolator(new LinearInterpolator());
            mShakeAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mShakeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int time = (int) animation.getAnimatedValue();
                    if (time <= 50) {
                        mGameBtn.setRotation(-1f * time / 5 * 2);
                    } else if (time <= 100) {
                        mGameBtn.setRotation(-20f + 1f * (time - 50) / 5 * 4);
                    } else if (time <= 150) {
                        mGameBtn.setRotation(20f - 1f * (time - 100) / 5 * 3);
                    } else if (time <= 180) {
                        mGameBtn.setRotation(-10f + 1f * (time - 150) / 3 * 2);
                    } else if (time <= 200) {
                        mGameBtn.setRotation(10f - 1f * (time - 180) / 2);
                    } else {
                        mGameBtn.setRotation(0);
                    }
                }
            });
        }
        mShakeAnimator.start();
    }

    private void destroyView() {
        if (mGameBtn != null) {
            mGameBtn.removeCallbacks(mAnimatorRunnable);
        }
        if (mShakeAnimator != null) {
            mShakeAnimator.cancel();
        }
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public void onOrientation(boolean isLandscape) {
                WatchBottomButton.this.onOrientation(isLandscape);
            }

            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentContainer;
            }

            @Override
            public void showGameIcon(GameViewModel gameModel) {
                WatchBottomButton.this.showGameIcon(gameModel);
            }

            @Override
            public void destroyView() {
                WatchBottomButton.this.destroyView();
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 显示输入界面
         */
        void showInputView();

        /**
         * 显示礼物界面
         */
        void showGiftView();

        /**
         * 旋转UI
         */
        void rotateScreen();

        /**
         * 增加游戏下载
         */
        void showGameDownloadView();

        /**
         * 显示分享界面
         */
        void showShareView();
    }

    public interface IView extends IViewProxy, IOrientationListener {
        void showGameIcon(GameViewModel gameModel);

        void destroyView();
    }
}
