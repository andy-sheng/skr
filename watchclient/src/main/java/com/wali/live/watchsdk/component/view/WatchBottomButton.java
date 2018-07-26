package com.wali.live.watchsdk.component.view;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.api.LiveManager;
import com.thornbirds.component.view.IOrientationListener;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.BaseBottomButton;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;
import com.wali.live.watchsdk.fastsend.view.GiftFastSendView;
import com.wali.live.watchsdk.watch.model.RoomInfo;

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
    private WatchMenuIconView mMoreBtn;
    private MyInfoIconView mMyInfoIconView;
    private GiftFastSendView mGiftFastSendView;
    private View mBigTurnTableBtn;
    private View mRotateBtn;

    private boolean mIsGameMode = false;

    private Runnable mAnimatorRunnable;
    private ValueAnimator mShakeAnimator;

    boolean mIsHuYaLive = false;


    private View mShowAllBtn;
    private RelativeLayout mClearScreenContainer;

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
        } else if (id == R.id.more_btn) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mMoreBtn.changeIconStatus(true);
                mPresenter.showWatchMenuPanel(mMoreBtn.getMsgUnreadCnt());
            }
        } else if (id == R.id.my_info_btn) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.showMyInfoPannel();
                mMyInfoIconView.setMsgUnreadCnt(0);
            }
        } else if (id == R.id.gift_fast_sent_container) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.onFastGiftClick();
            }
        } else if (id == R.id.big_turn_table_btn) {
            if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                mPresenter.onBigTurnTableClick();
            }
        } else if (id == R.id.show_btn) {
            mPresenter.cancelClearScreen();
        }
    }

    public WatchBottomButton(
            @NonNull RelativeLayout contentContainer,
            boolean isGameMode, boolean isHuYaLive) {
        super(contentContainer);
        mContentContainer.setPadding(BTN_MARGIN, BTN_MARGIN, 0, BTN_MARGIN);
        mIsGameMode = isGameMode;
        mIsHuYaLive = isHuYaLive;
        initView();
    }

    protected void initView() {
        mGiftBtn = createImageView(R.drawable.live_icon_gift_btn);
        addCreatedView(mGiftBtn, R.id.gift_btn);
        mGiftBtn.setVisibility(mIsHuYaLive ? View.GONE : View.VISIBLE);

        mMyInfoIconView = new MyInfoIconView(getContext());
        addCreatedView(mMyInfoIconView, R.id.my_info_btn);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mMyInfoIconView.getLayoutParams();
        layoutParams.setMargins(BTN_MARGIN, BTN_MARGIN, 0,  DisplayUtils.dip2px(6.67f));


        mGiftFastSendView = createFastGiftView();
        addCreatedView(mGiftFastSendView, R.id.gift_fast_sent_container);

        if (mIsHuYaLive) {
            mGiftFastSendView.setVisibility(View.GONE);
        }

        mAboveTheRightBtnSetPort.add(mMyInfoIconView);        // 横竖屏时按钮排列顺序
        if (!mIsHuYaLive) {
            mRightBtnSetPort.add(mGiftBtn);
            mRightBtnSetPort.add(mGiftFastSendView);
        }
        mBottomBtnSetLand.add(mMyInfoIconView);
        if (!mIsHuYaLive) {
            mBottomBtnSetLand.add(mGiftBtn);
            mBottomBtnSetLand.add(mGiftFastSendView);
        }

        orientChild();
    }

    private GiftFastSendView createFastGiftView() {
        GiftFastSendView view = new GiftFastSendView(getContext());
        view.setImgPic("", true);
        return view;
    }

    private void showMoreBtnIcon() {
        if (mMoreBtn == null) {
            mMoreBtn = new WatchMenuIconView(getContext());
            addCreatedView(mMoreBtn, R.id.more_btn);
        }
        mRightBtnSetPort.add(mRightBtnSetPort.isEmpty() ? 0 : 1, mMoreBtn);
        mBottomBtnSetLand.add(mBottomBtnSetLand.isEmpty() ? 0 : 1, mMoreBtn);
        orientChild();
    }

    private void showBigTurnTableBtn() {
        if (mBigTurnTableBtn == null) {
            mBigTurnTableBtn = createImageView(R.drawable.bg_big_turn_table_show);
            addCreatedView(mBigTurnTableBtn, R.id.big_turn_table_btn);
        }
        if (!mIsHuYaLive) {
            mRightBtnSetPort.add(mBigTurnTableBtn);
            mBottomBtnSetLand.add(mBigTurnTableBtn);
        }
        orientChild();
    }

    private void hideBigTurnTableBtn() {
        if (mBigTurnTableBtn != null) {
            mRightBtnSetPort.remove(mBigTurnTableBtn);
            mBottomBtnSetLand.remove(mBigTurnTableBtn);
            orientChild();
        }
    }

    //这些标志位真恶心
    public void correctType(int type) {
        if(type == LiveManager.TYPE_LIVE_GAME) {
            mIsGameMode = true;
            mIsHuYaLive = false;
            if(mGiftBtn != null) {
                mGiftBtn.setVisibility(View.VISIBLE);
            }

            if(mGiftFastSendView != null) {
                mGiftFastSendView.setVisibility(View.VISIBLE);
            }
        } else if(type == LiveManager.TYPE_LIVE_HUYA) {
            mIsGameMode = false;
            mIsHuYaLive = true;

            if(mGiftBtn != null) {
                mGiftBtn.setVisibility(View.GONE);
            }

            if(mGiftFastSendView != null) {
                mGiftFastSendView.setVisibility(View.GONE);
            }
        } else {
            mIsGameMode = false;
            mIsHuYaLive = false;

            if(mGiftBtn != null) {
                mGiftBtn.setVisibility(View.VISIBLE);
            }

            if(mGiftFastSendView != null) {
                mGiftFastSendView.setVisibility(View.VISIBLE);
            }
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

        if (mBigTurnTableBtn != null) {
            mRightBtnSetPort.remove(mBigTurnTableBtn);
            mBottomBtnSetLand.remove(mBigTurnTableBtn);
            mContentContainer.removeView(mBigTurnTableBtn);
            mBigTurnTableBtn = null;
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

    protected final void orientChild() {
        if (mIsLandscape) {
            if (mRotateBtn == null) {
                // 转屏按钮，只在横屏时显示
                mRotateBtn = createImageView(R.drawable.live_icon_rotate_screen);
                addCreatedView(mRotateBtn, R.id.rotate_btn);
                mBottomBtnSetLand.add(mRotateBtn);
                mRotateBtn.setOnClickListener(this);
            }
            mRotateBtn.setVisibility(View.VISIBLE);
            if (mMyInfoIconView != null) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mMyInfoIconView.getLayoutParams();
                layoutParams.setMargins(BTN_MARGIN, BTN_MARGIN, 0,  BTN_MARGIN);
            }
        } else {
            if (mRotateBtn != null) {
                mRotateBtn.setVisibility(View.GONE);
            }
            if (mMyInfoIconView != null) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mMyInfoIconView.getLayoutParams();
                layoutParams.setMargins(BTN_MARGIN, BTN_MARGIN, 0,  DisplayUtils.dip2px(6.67f));
            }
        }
        super.orientChild();
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
            public void onUpdateUnreadCount(int unReadCnt) {
                mMyInfoIconView.setMsgUnreadCnt(unReadCnt);
            }

            @Override
            public void updateMoreBtnStatus() {
                if (mMoreBtn != null) {
                    mMoreBtn.changeIconStatus(false);
                }
            }

            @Override
            public void tryBindAvatar() {
                if (mMyInfoIconView != null) {
                    mMyInfoIconView.tryBindAvatar();
                }
            }

            @Override
            public void showMoreBtn() {
                showMoreBtnIcon();
            }

            @Override
            public void setFastGift(String widgetIcon, boolean needGiftIcon) {
                if (mGiftFastSendView != null && !mIsHuYaLive) {
                    mGiftFastSendView.setImgPic(widgetIcon, needGiftIcon);
                    orientChild();
                }
            }

            @Override
            public void startFastGiftPBarAnim() {
                if (mGiftFastSendView != null) {
                    mGiftFastSendView.start();
                }
            }

            @Override
            public void showBigTurnTable() {
                showBigTurnTableBtn();
            }

            @Override
            public void hideBigTurnTable() {
                hideBigTurnTableBtn();
            }

            @Override
            public View getBigTurnTable() {
                return mBigTurnTableBtn;
            }

            @Override
            public void notifyClearScreen(boolean viewVisiable) {
                if (mShowAllBtn == null) {
                    mShowAllBtn = createImageView(R.drawable.live_bottom_return_btn);

                    if (mClearScreenContainer == null) {
                        mClearScreenContainer = new RelativeLayout(mContentContainer.getContext());
                        RelativeLayout parent = (RelativeLayout) mContentContainer.getParent();
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        parent.addView(mClearScreenContainer, layoutParams);
                    }
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.alignWithParent = true;
                    layoutParams.setMargins(BTN_MARGIN, BTN_MARGIN, BTN_MARGIN, BTN_MARGIN);
                    mClearScreenContainer.addView(mShowAllBtn, layoutParams);

                    mShowAllBtn.setId(R.id.show_btn);
                    mShowAllBtn.setOnClickListener(WatchBottomButton.this);
                }

                mClearScreenContainer.setVisibility(viewVisiable ? View.GONE : View.VISIBLE);
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
         * 显示更多面板
         */
        void showWatchMenuPanel(int unReadCnt);

        /**
         * 显示个人信息
         */
        void showMyInfoPannel();

        void processMoreBtnShow();

        void onFastGiftClick();

        void onBigTurnTableClick();

        void cancelClearScreen();
    }

    public interface IView extends IViewProxy, IOrientationListener {
        void showGameIcon(GameViewModel gameModel);

        void destroyView();

        /**
         * 更新私信未读数
         */
        void onUpdateUnreadCount(int unReadCnt);

        /**
         * 更新更多按钮
         */
        void updateMoreBtnStatus();

        void tryBindAvatar();

        void showMoreBtn();

        void setFastGift(String widgetIcon, boolean needGiftIcon);

        void startFastGiftPBarAnim();

        void showBigTurnTable();

        void hideBigTurnTable();

        View getBigTurnTable();

        void notifyClearScreen(boolean viewVisiable);
    }
}
