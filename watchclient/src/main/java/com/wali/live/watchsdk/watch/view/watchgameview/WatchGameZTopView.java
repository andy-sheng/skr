package com.wali.live.watchsdk.watch.view.watchgameview;

import android.animation.ValueAnimator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.GameNewLandscapeInputViewPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vera on 2018/8/7.
 * 直播顶部浮层 包括返回按钮 分享 横屏时的关注按钮、下载等等
 * 内部处理横竖屏样式
 */

public class WatchGameZTopView extends RelativeLayout implements View.OnClickListener,
        IComponentView<WatchGameZTopView.IPresenter, WatchGameZTopView.IView>,PortraitLineUpButtons.OnPortraitButtonClickListener {
    private final String TAG = getClass().getSimpleName();

    private static final int ANIMATION_DURATION = 300;

    private Context mContext;

    @Nullable
    protected IPresenter mPresenter;

    private boolean mIsLandscape = false; // 是否是横屏

    private List<View> mPortritViews; // 竖屏子View集合
    private List<View> mLandscapeViews; // 横屏子View集合

    // 竖屏下展示的控件 TODO 水印还没加上
    private ImageView mPortraitBackBtn;
    private PortraitLineUpButtons mPortraitLinUpButtons;
    // 竖屏相关


    // 横屏下展示的控件
    private RelativeLayout mLandscapeTopLayout; // 横屏下上半部分的布局
    private RelativeLayout mLandscapeBottomLayout; // 横屏下下半部分的布局
    private ImageView mLandscapeBackBtn;
    private TextView mLandscapeLiveTitle;
    private RelativeLayout mLandscapeAnchorLayout;
    private BaseImageView mLandscapeAnchorAvatar;
    private TextView mLandscapeAnchorNameTv;
    private TextView mLandscapeFollowBtn;
    private ImageView mLandscapeDownloadBtn;
    private ImageView mLandscapeShareBtn;
    private ImageView mLandscapeSuspend;
    private ImageView mLandscapeRefresh;
    private ImageView mLandscapeBarrageHideBtn;
    private ImageView getmLandscapeGiftBtn;
    private WatchGameMenuDialog mWatchGameMenuDialog;
    private GameNewLandscapeInputView mGameNewLandscapeInputView;

    // 横屏相关
    private boolean mEnableFollow = false;
    private ValueAnimator mFollowAniamator;
    private View mTouchView;

    //animator
    private boolean mIsLandscapeHideOptMode = false;
    private boolean mIsPortraitHideOptMode = false;
    private AnimatorSet mHideLandscapeOptBarAnimatorSet;
    private AnimatorSet mShowLandscapeOptBarAnimatorSet;
    private AnimatorSet mHidePortraitOptBarAnimatorSet;
    private AnimatorSet mShowPortraitOptBarAnimatorSet;

    private GameNewLandscapeInputViewPresenter mGameNewLandscapeInputViewPresenter;
    private boolean mIsVideoPause;
    private boolean mHasHideBarrage;

    public WatchGameZTopView(Context context) {
        super(context);
        setUpLayout(context, false);
    }

    public WatchGameZTopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpLayout(context, false);
    }

    public void setEnableFollow(boolean enableFollow) {
        this.mEnableFollow = enableFollow;
    }


    /**
     * 根据横竖屏加载或切换不同布局
     * 仅当首次加载布局或者横竖屏切换时被调用
     * @param context
     * @param lastIsLandscape 切换前是横屏还是竖屏 首次加载传任意值都可
     */
    private void setUpLayout(Context context, boolean lastIsLandscape) {
        mContext = context;
        if (mIsLandscape) { // 切换到横屏
            if (getChildCount() > 0 && !lastIsLandscape) {
                // 切换前是竖屏
                if (mPortritViews == null) {
                    mPortritViews = new ArrayList<>();
                } else {
                    mPortritViews.clear();
                }
                // 将切换前竖屏的View保存起来
                for (int i = 0; i < getChildCount(); i ++) {
                    mPortritViews.add(getChildAt(i));
                }
            }
            // 清空当前布局上所有的子View
            if(mWatchGameMenuDialog != null) {
                mWatchGameMenuDialog.tryDismiss();
            }
            removeAllViews();

            if (mLandscapeViews == null) {
                // 还没有加载过横屏布局 先加载
                inflate(context, R.layout.watch_z_top_lanscape_layout, this);
                bindLandscapeViews();
            } else {
                // 加载过横屏布局 重新add
                for (View view: mLandscapeViews) {
                    addView(view);
                }

                mLandscapeSuspend.setBackgroundDrawable(mIsVideoPause ?
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_play)
                        : GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_suspended));

                mLandscapeBarrageHideBtn.setBackground(mHasHideBarrage ?
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_banbarrage) :
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_subtitles));
            }

            if(mPresenter != null
                    && mPresenter.getController() != null
                    && mPresenter.getController().getRoomBaseDataModel() != null
                    && mPresenter.getController().getRoomBaseDataModel().getLiveType() == LiveManager.TYPE_LIVE_HUYA) {
                getmLandscapeGiftBtn.setVisibility(GONE);
                View view = findViewById(R.id.splite_line_view_2);
                view.setVisibility(GONE);
            }

            if(mGameNewLandscapeInputViewPresenter == null) {
                initInputPresenter();
            }
        } else { // 切换到竖屏
            if (getChildCount() > 0 && lastIsLandscape) {
                // 切换前是横屏
                if (mLandscapeViews == null) {
                    mLandscapeViews = new ArrayList<>();
                } else {
                    mLandscapeViews.clear();
                }
                // 将切换前横屏的View保存起来
                for (int i = 0; i < getChildCount(); i ++) {
                    mLandscapeViews.add(getChildAt(i));
                }
            }
            // 清空当前布局上所有的子View
            removeAllViews();

            if (mPortritViews == null) {
                // 还没有加载过竖屏布局 先加载
                inflate(context, R.layout.watch_z_top_portrait_layout, this);
                bindPortraitViews();
            } else {
                // 加载过竖屏布局 重新add
                for (View view: mPortritViews) {
                    if(view instanceof PortraitLineUpButtons) {
                        View v = ((PortraitLineUpButtons) view).getViewById(R.id.game_watch_portrait_suspended);
                        if(v != null
                                && v instanceof ImageView) {
                            ((ImageView) v).setImageDrawable(mIsVideoPause ? GlobalData.app().getResources().getDrawable(R.drawable.live_video_function_icon_play)
                                    : GlobalData.app().getResources().getDrawable(R.drawable.live_video_function_icon_suspended));
                        }
                    }
                    addView(view);
                }
            }
        }
    }

    /**
     * 竖屏首次加载时bindView
     */
    private void bindPortraitViews() {
        mPortraitBackBtn = (ImageView) findViewById(R.id.portrait_back_btn);
        mPortraitBackBtn.setOnClickListener(this);

        mPortraitLinUpButtons = (PortraitLineUpButtons) findViewById(R.id.portrait_line_up_buttons);
//        // 分享
//        mPortraitLinUpButtons.addButton(R.drawable.live_video_function_icon_share, R.id.game_watch_portrait_share);
        // 更多
        mPortraitLinUpButtons.addButton(R.drawable.live_video_function_icon_more, R.id.game_watch_portrait_more);
        // 暂停　播放
        mPortraitLinUpButtons.addButton((mIsVideoPause ? R.drawable.live_video_function_icon_play : R.drawable.live_video_function_icon_suspended), R.id.game_watch_portrait_suspended);
        // 全屏
        mPortraitLinUpButtons.addButton(R.drawable.live_video_function_icon_fullscreen, R.id.game_watch_portrait_fullscreen);
        mPortraitLinUpButtons.setOnButtonClickListener(this);

        mTouchView = findViewById(R.id.touch_view);
        mTouchView.setOnClickListener(this);
    }

    /**
     * 横屏首次加载时bindView
     */
    private void bindLandscapeViews() {
        mLandscapeTopLayout = (RelativeLayout) findViewById(R.id.landscape_top_layout);
        mLandscapeBottomLayout = (RelativeLayout) findViewById(R.id.landscape_bottom_layout);

        mLandscapeBackBtn = (ImageView) findViewById(R.id.landscape_back_btn);
        mLandscapeBackBtn.setOnClickListener(this);

        mLandscapeLiveTitle = (TextView) findViewById(R.id.landscape_live_title);

        // 主播相关
        mLandscapeAnchorLayout = (RelativeLayout) findViewById(R.id.landscape_anchor_layout);
        mLandscapeAnchorLayout.setOnClickListener(this);
        mLandscapeAnchorAvatar = (BaseImageView) findViewById(R.id.landscape_anchor_avatar);
        mLandscapeAnchorNameTv = (TextView) findViewById(R.id.landscape_anchor_name);
        mLandscapeFollowBtn = (TextView) findViewById(R.id.landscape_follow);
        mLandscapeFollowBtn.setOnClickListener(this);

        mLandscapeDownloadBtn = (ImageView) findViewById(R.id.landscape_download);
        mLandscapeDownloadBtn.setOnClickListener(this);

        mLandscapeShareBtn = (ImageView) findViewById(R.id.landscape_share);
        mLandscapeShareBtn.setOnClickListener(this);

        mLandscapeSuspend = (ImageView) findViewById(R.id.landscape_pause_resume_btn);
        mLandscapeSuspend.setBackgroundDrawable(mIsVideoPause ?
                GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_play)
                : GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_suspended));
        mLandscapeSuspend.setOnClickListener(this);

        mLandscapeRefresh = (ImageView) findViewById(R.id.landscape_refresh_btn);
        mLandscapeRefresh.setOnClickListener(this);
        mGameNewLandscapeInputView = (GameNewLandscapeInputView) findViewById(R.id.input_container);

        mLandscapeBarrageHideBtn = (ImageView) findViewById(R.id.landscape_hide_barrage_btn);
        mLandscapeBarrageHideBtn.setOnClickListener(this);

        getmLandscapeGiftBtn = (ImageView) findViewById(R.id.landscape_gift_btn);
        getmLandscapeGiftBtn.setOnClickListener(this);

        mTouchView = findViewById(R.id.touch_view);
        mTouchView.setOnClickListener(this);

        if (mPresenter != null) {
            mPresenter.syncAnchorInfo();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (mIsLandscape) {
            // 横屏下的点击事件
            if (mPresenter == null) {
                return;
            }
            if (id == R.id.landscape_back_btn) {
                mPresenter.forceRotate();
            } else if (id == R.id.landscape_anchor_layout) {
                mPresenter.showAnchorInfo();
            } else if (id == R.id.landscape_follow) {
                if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                    mPresenter.followAnchor();
                }
            } else if (id == R.id.landscape_gift_btn) {
                if(AccountAuthManager.triggerActionNeedAccount(getContext())) {
                    mPresenter.showGiftView();
                }
            } else if(id == R.id.landscape_pause_resume_btn) {
                mLandscapeSuspend.setBackgroundDrawable(mIsVideoPause ?
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_suspended)
                        : GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_play));
                playVideoControl();
            } else if(id == R.id.landscape_refresh_btn) {
                mPresenter.vodeoReFresh();
                mIsVideoPause = false;
                if(mLandscapeSuspend != null) {
                    mLandscapeSuspend.setBackgroundDrawable(GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_suspended));
                }
            } else if(id == R.id.landscape_hide_barrage_btn) {
                mLandscapeBarrageHideBtn.setBackground(mHasHideBarrage ?
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_subtitles) :
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_banbarrage));
                mHasHideBarrage = !mHasHideBarrage;
                mPresenter.optBarrageControl(mHasHideBarrage);
            } else if(id == R.id.landscape_download){
                mPresenter.checkDownLoad();
            }
        } else {
            // 竖屏下的点击事件
            if (id == R.id.portrait_back_btn) {
                if (mPresenter != null) {
                    mPresenter.exitRoom();
                }
            }
        }

        if(id == R.id.touch_view) {
            touchViewOnclick();
        }
    }

    private void playVideoControl() {
        if(mPresenter == null) {
            return;
        }

        if(mIsVideoPause) {
            mPresenter.videoRestart();
        } else {
            mPresenter.videoPause();
        }
        mIsVideoPause = !mIsVideoPause;

    }

    /**
     * PortraitLineUpButtons的各个Button的点击回调
     * @param v
     */
    @Override
    public void onPortraitButtonClick(View v) {
        int id = v.getId();
        if (id == R.id.game_watch_portrait_share) {
            ToastUtils.showToast("点击分享");
        } else if (id == R.id.game_watch_portrait_more) {
            if(mWatchGameMenuDialog == null) {
                mWatchGameMenuDialog = new WatchGameMenuDialog(v.getContext());
                mWatchGameMenuDialog.setListener(new WatchGameMenuDialog.OnWatchGameMenuDialogListener() {
                    @Override
                    public void onVoiceControlBtnClick() {
                        ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.not_support));
                    }

                    @Override
                    public void onDisLikeBtnClick() {
                        if(mPresenter != null) {
                            mPresenter.optDisLike();
                        }
                    }

                    @Override
                    public void onReportBtnClick() {
                        if(mPresenter != null) {
                            mPresenter.optReprot();
                        }
                    }
                });
            }
            mWatchGameMenuDialog.show(WatchGameZTopView.this, v);
        } else if (id == R.id.game_watch_portrait_suspended) {
            if(v instanceof ImageView) {
                ((ImageView) v).setImageDrawable(mIsVideoPause ? GlobalData.app().getResources().getDrawable(R.drawable.live_video_function_icon_suspended)
                        : GlobalData.app().getResources().getDrawable(R.drawable.live_video_function_icon_play));
                playVideoControl();
            }
        } else if (id == R.id.game_watch_portrait_fullscreen) {
            if (mPresenter != null) {
                mPresenter.forceRotate();
            }
        }
    }

    /**
     * 接收横竖屏切换通知
     * @param isLandscape
     */
    private void onReOrient(boolean isLandscape) {
        MyLog.d(TAG, "change to" + (isLandscape ? "landscape" : "portrait"));
        if (mIsLandscape != isLandscape) {
            // 横竖屏相互切换　重新加载布局
            mIsLandscape = isLandscape;
            setUpLayout(getContext(), !mIsLandscape);
        } else {
            // 横屏切换到反向横屏　或者竖屏切换到反向竖屏
        }
    }

    /**
     * 同步主播信息
     * @param uid
     * @param avatarTs
     * @param nickName
     * @param isFollowed
     */
    private void updateAnchorInfo(long uid, long avatarTs, String nickName, boolean isFollowed) {
        if (mLandscapeAnchorAvatar != null && mLandscapeAnchorNameTv != null && mLandscapeFollowBtn != null) {
            AvatarUtils.loadAvatarByUidTs(mLandscapeAnchorAvatar, uid, avatarTs, true);
            if (!TextUtils.isEmpty(nickName)) {
                mLandscapeAnchorNameTv.setText(nickName);
            } else if (uid > 0) {
                mLandscapeAnchorNameTv.setText(String.valueOf(uid));
            } else {
                mLandscapeAnchorNameTv.setText(R.string.watch_owner_name_default);
            }

            if (mEnableFollow && !isFollowed) {
                setFollowBtnWidth(mFollowBtnWidth);
            } else {
                setFollowBtnWidth(0);
            }
        }
    }

    /**
     * 关注或者取关后修改关注按钮状态
     * @param needShow
     */
    private void showFollowBtn(boolean needShow, boolean needAnim) {
        MyLog.d(TAG, "showFollowBtn needShow " + needShow + " useAnim " + needAnim);

        if (!mEnableFollow || mLandscapeFollowBtn == null) {
            return;
        }

        if (!needAnim) {
            setFollowBtnWidth(needShow ? mFollowBtnWidth : 0);
            return;
        }

        if (mFollowAniamator == null) {
            mFollowAniamator = ValueAnimator.ofInt(0, getResources().getDimensionPixelSize(R.dimen.view_dimen_128));
            mFollowAniamator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    setFollowBtnWidth(value);
                }
            });
            mFollowAniamator.setDuration(500);
            mFollowAniamator.setRepeatCount(0);
        }
        if (mFollowAniamator.isStarted()) {
            mFollowAniamator.cancel();
        }
        if (needShow) {
            mFollowAniamator.start();
        } else {
            mFollowAniamator.reverse();
        }
    }

    private final int mFollowBtnWidth = getResources().getDimensionPixelSize(R.dimen.view_dimen_128);
    private void setFollowBtnWidth(int width) {
        ViewGroup.LayoutParams params = mLandscapeFollowBtn.getLayoutParams();
        if (params != null) {
            params.width = width;
            mLandscapeFollowBtn.setLayoutParams(params);
        }
    }

    private void cancelAnimator() {
        if (mFollowAniamator != null && mFollowAniamator.isStarted()) {
            mFollowAniamator.cancel();
        }
    }

    /**
     * 关注结果回调
     * @param resultCode
     */
    private void onFollowResult(int resultCode) {
        MyLog.d(TAG, "onFollowResult " + resultCode);
        if (!mEnableFollow || mLandscapeFollowBtn == null) {
            return;
        }
        if (resultCode == ErrorCode.CODE_RELATION_BLACK) {
            ToastUtils.showToast(getResources().getString(R.string.setting_black_follow_hint));
        } else if (resultCode == 0) {
            ToastUtils.showToast(getResources().getString(R.string.follow_success));
            showFollowBtn(false, true);
        } else if (resultCode == -1) {
            ToastUtils.showToast(getResources().getString(R.string.follow_failed));
        }else{
            ToastUtils.showToast("关注失败 code:"+resultCode);
        }
    }

    private void touchViewOnclick() {
        if(mIsLandscape) {
            if(KeyboardUtils.hideKeyboardThenReturnResult((Activity) getContext())) {
                return;
            }

            if(!mIsLandscapeHideOptMode) {
                hideOptBar();
            } else {
                showOptBar();
            }
        } else {
            if(!mIsPortraitHideOptMode) {
                hideOptBar();
            } else {
                showOptBar();
            }
        }
    }

    /**
     * 点击显示隐藏操作栏bar
     */
    private void hideOptBar() {
        if(mIsLandscape) {
            hideLandscapeOptBar();
        } else {
            hidePortraitOptBar();
        }
    }

    private void showOptBar() {
        if(mIsLandscape) {
            showLandscapeOptBar();
        } else {
            showPortraitOptBar();
        }
    }

    private void hideLandscapeOptBar() {
        if(mIsLandscapeHideOptMode) {
            return;
        }

        if(mHideLandscapeOptBarAnimatorSet == null) {
            ObjectAnimator landscapeBottomLayoutHideAnimator = ObjectAnimator.ofFloat(mLandscapeBottomLayout
                    , View.TRANSLATION_Y
                    , mLandscapeBottomLayout.getTranslationY()
                    , mLandscapeBottomLayout.getTranslationY() + mLandscapeBottomLayout.getHeight());

            ObjectAnimator andscapeTopLayoutHideAnimator = ObjectAnimator.ofFloat(mLandscapeTopLayout, View.TRANSLATION_Y
                    , mLandscapeTopLayout.getTranslationY()
                    , mLandscapeTopLayout.getTranslationY() - mLandscapeTopLayout.getHeight());
            mHideLandscapeOptBarAnimatorSet = new AnimatorSet();
            mHideLandscapeOptBarAnimatorSet.playTogether(landscapeBottomLayoutHideAnimator, andscapeTopLayoutHideAnimator);
            mHideLandscapeOptBarAnimatorSet.setDuration(ANIMATION_DURATION);
            mHideLandscapeOptBarAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsLandscapeHideOptMode = true;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }

            });
        }

        if (!mHideLandscapeOptBarAnimatorSet.isRunning()) {
            mHideLandscapeOptBarAnimatorSet.start();
        }
    }

    private void hidePortraitOptBar() {
        if(mIsPortraitHideOptMode) {
            return;
        }

        if(mHidePortraitOptBarAnimatorSet == null) {
            ObjectAnimator portraitBackBtnHideAnimator = ObjectAnimator.ofFloat(mPortraitBackBtn
                    , View.TRANSLATION_Y
                    , mPortraitBackBtn.getTranslationY()
                    , mPortraitBackBtn.getTranslationY() - mPortraitBackBtn.getBottom());
            ObjectAnimator portraitLinUpButtonsHideAnimator = ObjectAnimator.ofFloat(mPortraitLinUpButtons
                    , View.TRANSLATION_X
                    , mPortraitLinUpButtons.getTranslationX() + (DisplayUtils.getScreenWidth() - mPortraitLinUpButtons.getLeft()));

            mHidePortraitOptBarAnimatorSet = new AnimatorSet();
            mHidePortraitOptBarAnimatorSet.playTogether(portraitBackBtnHideAnimator, portraitLinUpButtonsHideAnimator);
            mHidePortraitOptBarAnimatorSet.setDuration(ANIMATION_DURATION);
            mHidePortraitOptBarAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsPortraitHideOptMode = true;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }

            });
        }

        if (!mHidePortraitOptBarAnimatorSet.isRunning()) {
            mHidePortraitOptBarAnimatorSet.start();
        }
    }

    private void showLandscapeOptBar() {
        if(!mIsLandscapeHideOptMode) {
            return;
        }

        if(mShowLandscapeOptBarAnimatorSet == null) {
            ObjectAnimator landscapeBottomLayoutShowAnimator = ObjectAnimator.ofFloat(mLandscapeBottomLayout, View.TRANSLATION_Y
                    , mLandscapeBottomLayout.getTranslationY()
                    , mLandscapeBottomLayout.getTranslationY() - mLandscapeBottomLayout.getHeight());

            ObjectAnimator andscapeTopLayoutShowAnimator = ObjectAnimator.ofFloat(mLandscapeTopLayout, View.TRANSLATION_Y
                    , mLandscapeTopLayout.getTranslationY(), mLandscapeTopLayout.getTranslationY() + mLandscapeTopLayout.getHeight());
            mShowLandscapeOptBarAnimatorSet = new AnimatorSet();
            mShowLandscapeOptBarAnimatorSet.playTogether(landscapeBottomLayoutShowAnimator, andscapeTopLayoutShowAnimator);
            mShowLandscapeOptBarAnimatorSet.setDuration(ANIMATION_DURATION);
            mShowLandscapeOptBarAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsLandscapeHideOptMode = false;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }
            });
        }

        if (!mShowLandscapeOptBarAnimatorSet.isRunning()) {
            mShowLandscapeOptBarAnimatorSet.start();
        }
    }

    private void showPortraitOptBar() {
        if(!mIsPortraitHideOptMode) {
            return;
        }

        if(mShowPortraitOptBarAnimatorSet == null) {
            ObjectAnimator portraitBackBtnShowAnimator = ObjectAnimator.ofFloat(mPortraitBackBtn
                    , View.TRANSLATION_Y
                    , mPortraitBackBtn.getTranslationY()
                    , mPortraitBackBtn.getTranslationY() + mPortraitBackBtn.getBottom());

            ObjectAnimator portraitLinUpButtonsShowAnimator = ObjectAnimator.ofFloat(mPortraitLinUpButtons
                    , View.TRANSLATION_X
                    , mPortraitLinUpButtons.getTranslationX() - (DisplayUtils.getScreenWidth() - mPortraitLinUpButtons.getLeft()));

            mShowPortraitOptBarAnimatorSet = new AnimatorSet();
            mShowPortraitOptBarAnimatorSet.playTogether(portraitBackBtnShowAnimator, portraitLinUpButtonsShowAnimator);
            mShowPortraitOptBarAnimatorSet.setDuration(ANIMATION_DURATION);
            mShowPortraitOptBarAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsPortraitHideOptMode = false;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }

            });
        }

        if (!mShowPortraitOptBarAnimatorSet.isRunning()) {
            mShowPortraitOptBarAnimatorSet.start();
        }
    }

    private void initInputPresenter() {
        if(mPresenter == null
                || mGameNewLandscapeInputView == null) {
            return;
        }
        WatchComponentController controller = mPresenter.getController();
        if(controller == null) {
            return;
        }
        mGameNewLandscapeInputViewPresenter = new GameNewLandscapeInputViewPresenter(controller
                , controller.getRoomBaseDataModel()
                , controller.getLiveRoomChatMsgManager());
        mGameNewLandscapeInputViewPresenter.setView(mGameNewLandscapeInputView.getViewProxy());
        mGameNewLandscapeInputView.setPresenter(mGameNewLandscapeInputViewPresenter);
        mGameNewLandscapeInputViewPresenter.startPresenter();
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {

            @Override
            public void reOrient(boolean isLandscape) {
                // 接收横竖屏切换通知
                WatchGameZTopView.this.onReOrient(isLandscape);
            }

            @Override
            public void updateAnchorInfo(long uid, long avatarTs, String nickName, boolean isFollowed) {
                // 接收主播信息
                WatchGameZTopView.this.updateAnchorInfo(uid, avatarTs, nickName, isFollowed);
            }

            @Override
            public void onFollowResult(int resultCode) {
                WatchGameZTopView.this.onFollowResult(resultCode);
            }

            @Override
            public void showFollowBtn(boolean needShow, boolean needAnim) {
                WatchGameZTopView.this.showFollowBtn(needShow, needAnim);
            }

            @Override
            public void stopView() {
                WatchGameZTopView.this.cancelAnimator();
                if(mGameNewLandscapeInputViewPresenter != null) {
                    mGameNewLandscapeInputViewPresenter.stopPresenter();
                }
            }

            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameZTopView.this;
            }
        }
        return new ComponentView();
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {
        this.mPresenter = iPresenter;
        initInputPresenter();
    }

    public interface  IPresenter {
        /**
         * 强制全屏
         */
        void forceRotate();

        /**
         * 退出房间
         */
        void exitRoom();

        /**
         * 获取主播信息
         */
        void syncAnchorInfo();

        /**
         * 打开主播信息浮窗
         */
        void showAnchorInfo();

        /**
         * 关注主播
         */
        void followAnchor();

        /**
         * 打开礼物面板
         */
        void showGiftView();

        void optDisLike();

        void optReprot();

        WatchComponentController getController();

        void videoPause();

        void videoRestart();

        void vodeoReFresh();

        void optBarrageControl(boolean needHide);

        void checkDownLoad();
    }

    public interface IView extends IViewProxy {

        void reOrient(boolean isLandscape);
        /**
         * 更新主播信息
         */
        void updateAnchorInfo(long uid, long avatarTs, String nickName, boolean isFollowed);

        /**
         * 关注主播结果
         */
        void onFollowResult(int resultCode);

        /**
         * 关注按钮状态变更
         */
        void showFollowBtn(boolean needShow, boolean needAnim);
        /**
         * 取消动画
         */
        void stopView();
    }
}
