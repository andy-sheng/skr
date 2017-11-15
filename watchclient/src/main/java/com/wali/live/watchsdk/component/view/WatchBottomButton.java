package com.wali.live.watchsdk.component.view;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.facebook.drawee.view.SimpleDraweeView;
import com.thornbirds.component.view.IOrientationListener;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.BaseBottomButton;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;
import com.wali.live.watchsdk.view.MsgCtrlBtnView;

/**
 * Created by yangli on 16-8-29.
 *
 * @module 底部按钮视图, 游戏直播
 */
public class WatchBottomButton extends BaseBottomButton<WatchBottomButton.IPresenter, WatchBottomButton.IView> {
    private static final String TAG = "WatchBottomButton";

    private View mGiftBtn;
    //    protected View mRotateBtn;
    private View mGameBtn;
    private View mShareBtn;
    private MsgCtrlBtnView mMsgCntBtn;
    private View mVipFansBtn;

    private boolean mIsGameMode = false;
    private boolean mEnableShare;

    private Runnable mAnimatorRunnable;
    private ValueAnimator mShakeAnimator;

    @Override
    protected final String getTAG() {
        return TAG;
    }

    @Override
    public void onClick(View view) {
        if (mPresenter == null) {
            return;
        }
        int id = view.getId();
        if (id == R.id.gift_btn) {
            mPresenter.showGiftView();
        } else if (id == R.id.rotate_btn) {
            mPresenter.rotateScreen();
        } else if (id == R.id.game_btn) {
            mPresenter.showGameDownloadView();
            clearAnimator(); // 点击的同时清除动画
        } else if (id == R.id.share_btn) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.showShareView();
            }
        } else if (id == R.id.msg_ctrl_btn) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.showMsgCtrlView();
            }
        } else if (id == R.id.vip_fans_btn) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.showVipFansView();
            }
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
        mGiftBtn = createImageView(R.drawable.live_icon_gift_btn);
        addCreatedView(mGiftBtn, R.id.gift_btn);

        mMsgCntBtn = new MsgCtrlBtnView(getContext());
        addCreatedView(mMsgCntBtn, R.id.msg_ctrl_btn);

        mVipFansBtn = createImageView(R.drawable.game_live_icon_pet);
        addCreatedView(mVipFansBtn, R.id.vip_fans_btn);

//        mRotateBtn = createImageView(R.drawable.live_icon_rotate_screen);
//        addCreatedView(mGiftBtn, R.id.rotate_btn);

        // 横竖屏时按钮排列顺序
        mRightBtnSetPort.add(mGiftBtn);
        mRightBtnSetPort.add(mMsgCntBtn);
        mRightBtnSetPort.add(mVipFansBtn);

        mBottomBtnSetLand.add(mGiftBtn);
        mBottomBtnSetLand.add(mMsgCntBtn);
        mBottomBtnSetLand.add(mVipFansBtn);

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

    private void showGameIcon(final GameViewModel gameModel) {
        if (gameModel == null) {
            MyLog.w(TAG, "showGameIcon gameModel is null");
            return;
        }
        MyLog.d(TAG, "gameModel=" + gameModel.getGameId());

        mGameBtn = new SimpleDraweeView(getContext());
        // addCreatedView(mGameBtn, R.id.game_btn);
        addCreatedView(mGameBtn, mGiftBtn.getWidth(), mGiftBtn.getHeight(), R.id.game_btn);

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

    private final void destroyView() {
        clearAnimator();
    }

    public void reset() {
        clearAnimator();
        if (mGameBtn != null) {
            mRightBtnSetPort.remove(mGameBtn);
            mBottomBtnSetLand.remove(mGameBtn);
            mContentContainer.removeView(mGameBtn);
        }
    }

    private void clearAnimator() {
        if (mGameBtn != null) {
            mGameBtn.removeCallbacks(mAnimatorRunnable);
        }
        if (mShakeAnimator != null) {
            mShakeAnimator.cancel();
        }
    }

    public final void postSwitch(boolean isGameMode) {
        mIsGameMode = isGameMode;
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentContainer;
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                WatchBottomButton.this.onOrientation(isLandscape);
            }

            @Override
            public void showGameIcon(GameViewModel gameModel) {
                WatchBottomButton.this.showGameIcon(gameModel);
            }

            @Override
            public void destroyView() {
                WatchBottomButton.this.destroyView();
            }

            @Override
            public void onUpdateUnreadCount(int unreadCount) {
                mMsgCntBtn.setMsgUnreadCnt(unreadCount);
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

        /**
         * 显示私信面板
         */
        void showMsgCtrlView();

        /**
         * 显示粉丝团管理界面
         */
        void showVipFansView();
    }

    public interface IView extends IViewProxy, IOrientationListener {
        void showGameIcon(GameViewModel gameModel);

        void destroyView();

        /**
         * 更新私信未读数
         */
        void onUpdateUnreadCount(int unreadCount);
    }
}
