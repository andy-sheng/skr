package com.wali.live.watchsdk.watch.view.watchgameview;

import android.animation.ValueAnimator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.GameNewLandscapeInputViewPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameTouchPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by vera on 2018/8/7.
 * 直播顶部浮层 包括返回按钮 分享 横屏时的关注按钮、下载等等
 * 内部处理横竖屏样式
 */

public class WatchGameZTopView extends RelativeLayout implements View.OnClickListener,
        IComponentView<WatchGameZTopView.IPresenter, WatchGameZTopView.IView>, PortraitLineUpButtons.OnPortraitButtonClickListener {
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
    private ImageView mPortraitVoiceBtn;
    // 竖屏相关
    private boolean mIsVideoMute = false; //是否静音

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
    private boolean mNeedHideDownLoadBtn;

    private Handler mUiHanlder = new Handler();

    private WatchGameTouchPresenter mWatchGameTouchPresenter;
    private WatchGameWaterMarkView mPortraitWatchGameWaterMarkView;
    private RelativeLayout mPortraitTitleContainer;
    private WatchGameWaterMarkView mLandscapeWatchGameWaterMarkView;

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
     *
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
                for (int i = 0; i < getChildCount(); i++) {
                    mPortritViews.add(getChildAt(i));
                }
            }
            // 清空当前布局上所有的子View
            if (mWatchGameMenuDialog != null) {
                mWatchGameMenuDialog.tryDismiss();
            }
            removeAllViews();

            if (mLandscapeViews == null) {
                // 还没有加载过横屏布局 先加载
                inflate(context, R.layout.watch_z_top_lanscape_layout, this);
                bindLandscapeViews();
                checkInstalledOrUpdate(mPresenter.getController().getRoomBaseDataModel().getGameInfoModel());
            } else {
                // 加载过横屏布局 重新add
                for (View view : mLandscapeViews) {
                    addView(view);
                }

                mLandscapeSuspend.setBackgroundDrawable(mIsVideoPause ?
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_play)
                        : GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_suspended));

                mLandscapeBarrageHideBtn.setBackground(mHasHideBarrage ?
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_banbarrage) :
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_subtitles));
            }

            if (mPresenter != null
                    && mPresenter.getController() != null
                    && mPresenter.getController().getRoomBaseDataModel() != null) {

                if(mPresenter.getController().getRoomBaseDataModel().getLiveType() == LiveManager.TYPE_LIVE_HUYA) {
                    getmLandscapeGiftBtn.setVisibility(GONE);
                    View view = findViewById(R.id.splite_line_view_2);
                    view.setVisibility(GONE);
                }


                if(mLandscapeWatchGameWaterMarkView != null) {
                    mLandscapeWatchGameWaterMarkView.setRoomData(mPresenter.getController().getRoomBaseDataModel());
                    mLandscapeWatchGameWaterMarkView.onOrientation(mIsLandscape);
                }
            }

            if (mGameNewLandscapeInputViewPresenter == null) {
                initInputPresenter();
            }

            tryToHideLandscapeOptBar();
            mLandscapeDownloadBtn.setVisibility(mNeedHideDownLoadBtn ? GONE : VISIBLE);
        } else { // 切换到竖屏
            if (getChildCount() > 0 && lastIsLandscape) {
                // 切换前是横屏
                if (mLandscapeViews == null) {
                    mLandscapeViews = new ArrayList<>();
                } else {
                    mLandscapeViews.clear();
                }
                // 将切换前横屏的View保存起来
                for (int i = 0; i < getChildCount(); i++) {
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
                for (View view : mPortritViews) {
                    if (view instanceof PortraitLineUpButtons) {
                        View v = ((PortraitLineUpButtons) view).getViewById(R.id.game_watch_portrait_suspended);
                        if (v != null
                                && v instanceof ImageView) {
                            ((ImageView) v).setImageDrawable(mIsVideoPause ? GlobalData.app().getResources().getDrawable(R.drawable.live_video_function_icon_play)
                                    : GlobalData.app().getResources().getDrawable(R.drawable.live_video_function_icon_suspended));
                        }
                    }
                    addView(view);
                }

                mPortraitVoiceBtn.setImageDrawable(mIsVideoMute ? GlobalData.app().getResources().getDrawable(R.drawable.live_function_icon_mute) : GlobalData.app().getResources().getDrawable(R.drawable.live_function_icon_voice));

                if(mPresenter != null
                        && mPresenter.getController() != null
                        && mPresenter.getController().getRoomBaseDataModel() != null) {
                    mPortraitWatchGameWaterMarkView.setRoomData(mPresenter.getController().getRoomBaseDataModel());
                    mPortraitWatchGameWaterMarkView.onOrientation(mIsLandscape);
                }
            }

            tryToHidePortraitOptBar();
        }
    }

    /**
     * 竖屏首次加载时bindView
     */
    private void bindPortraitViews() {
        mPortraitBackBtn = (ImageView) findViewById(R.id.portrait_back_btn);
        mPortraitBackBtn.setOnClickListener(this);

        mPortraitVoiceBtn = (ImageView) findViewById(R.id.protrait_voice_btn);
        mPortraitVoiceBtn.setOnClickListener(this);

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

        mPortraitWatchGameWaterMarkView = (WatchGameWaterMarkView) findViewById(R.id.portrait_watch_mark_view);
        mPortraitWatchGameWaterMarkView.onOrientation(false);
        mPortraitTitleContainer = (RelativeLayout) findViewById(R.id.portrait_title_container);
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
        mWatchGameTouchPresenter = new WatchGameTouchPresenter(mPresenter.getController(), mTouchView);
        mTouchView.setOnClickListener(this);

        mLandscapeWatchGameWaterMarkView = (WatchGameWaterMarkView) findViewById(R.id.landscape_watch_mark_view);
        mLandscapeWatchGameWaterMarkView.onOrientation(true);

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
                if (AccountAuthManager.triggerActionNeedAccount(getContext())) {
                    mPresenter.showGiftView();
                }
            } else if (id == R.id.landscape_pause_resume_btn) {
                playVideoControl();
            } else if (id == R.id.landscape_refresh_btn) {
                mPresenter.vodeoReFresh();
                mIsVideoPause = false;
                if (mLandscapeSuspend != null) {
                    mLandscapeSuspend.setBackgroundDrawable(GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_suspended));
                }
            } else if (id == R.id.landscape_hide_barrage_btn) {
                mLandscapeBarrageHideBtn.setBackground(mHasHideBarrage ?
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_subtitles) :
                        GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_banbarrage));
                mHasHideBarrage = !mHasHideBarrage;
                mPresenter.optBarrageControl(mHasHideBarrage);
            } else if (id == R.id.landscape_download) {
                int flag = 0;
                if (mLandscapeDownloadBtn.getTag() != null) {
                    flag = (int) mLandscapeDownloadBtn.getTag();
                }
                mPresenter.clickDownLoad(flag);
            }
        } else {
            // 竖屏下的点击事件
            if (id == R.id.portrait_back_btn) {
                if (mPresenter != null) {
                    mPresenter.exitRoom();
                }
            } else if (id == R.id.protrait_voice_btn) {
                touchVoiceOnClick();
            }
        }

        if (id == R.id.touch_view) {
            touchViewOnclick();
        }
    }

    private void touchVoiceOnClick() {
        if (mIsVideoMute) {
            // 打开声音
//            mIsVideoMute = false;
            mPresenter.videoMute(false);
//            mPortraitVoiceBtn.setImageResource(R.drawable.live_function_icon_voice);
        } else {
            // 静音
//            mIsVideoMute = true;
            mPresenter.videoMute(true);
//            mPortraitVoiceBtn.setImageResource(R.drawable.live_function_icon_mute);
        }
    }

    private void playVideoControl() {
        if (mPresenter == null) {
            return;
        }

        mIsVideoPause = !mIsVideoPause;
        if (mIsVideoPause) {
            mPresenter.videoPause();
        } else {
            mPresenter.videoRestart();
        }
    }

    private void updatePlayBtnUi() {
        if (mIsLandscape) {
            mLandscapeSuspend.setBackgroundDrawable(mIsVideoPause ?
                    GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_play)
                    : GlobalData.app().getResources().getDrawable(R.drawable.live_video_fullscreen_bottom_icon_suspended));
        } else {
            if (mPortraitLinUpButtons != null) {
                View v = mPortraitLinUpButtons.getViewById(R.id.game_watch_portrait_suspended);
                if (v != null
                        && v instanceof ImageView) {
                    ((ImageView) v).setImageDrawable(mIsVideoPause ? GlobalData.app().getResources().getDrawable(R.drawable.live_video_function_icon_play)
                            : GlobalData.app().getResources().getDrawable(R.drawable.live_video_function_icon_suspended));
                }
            }

            mPortraitVoiceBtn.setImageDrawable(mIsVideoMute ? GlobalData.app().getResources().getDrawable(R.drawable.live_function_icon_mute) : GlobalData.app().getResources().getDrawable(R.drawable.live_function_icon_voice));
        }
    }


    /**
     * PortraitLineUpButtons的各个Button的点击回调
     *
     * @param v
     */
    @Override
    public void onPortraitButtonClick(View v) {
        int id = v.getId();
        if (id == R.id.game_watch_portrait_share) {
            ToastUtils.showToast("点击分享");
        } else if (id == R.id.game_watch_portrait_more) {
            if (mWatchGameMenuDialog == null) {
                mWatchGameMenuDialog = new WatchGameMenuDialog(v.getContext());
                mWatchGameMenuDialog.setListener(new WatchGameMenuDialog.OnWatchGameMenuDialogListener() {
                    @Override
                    public void onVoiceControlBtnClick() {
                        ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.not_support));
                    }

                    @Override
                    public void onDisLikeBtnClick() {
                        if (mPresenter != null) {
                            mPresenter.optDisLike();
                        }
                    }

                    @Override
                    public void onReportBtnClick() {
                        if (mPresenter != null) {
                            mPresenter.optReprot();
                        }
                    }

                    @Override
                    public void onDismissCallback() {
                        if (!mIsLandscape) {
                            tryToHidePortraitOptBar();
                        }
                    }
                });
            }
            mWatchGameMenuDialog.show(WatchGameZTopView.this, v);
            if (!mIsLandscape) {
                tryUnSubscribe();
            }
        } else if (id == R.id.game_watch_portrait_suspended) {
            if (v instanceof ImageView) {
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
     *
     * @param isLandscape
     */
    private void onReOrient(boolean isLandscape) {
        MyLog.d(TAG, "change to" + (isLandscape ? "landscape" : "portrait"));
        if (mIsLandscape != isLandscape) {
            // 横竖屏相互切换　重新加载布局
            tryUnSubscribe();
            mIsLandscape = isLandscape;
            setUpLayout(getContext(), !mIsLandscape);
        } else {
            // 横屏切换到反向横屏　或者竖屏切换到反向竖屏
        }
    }

    /**
     * 同步主播信息
     *
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
     *
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

        if (mHideLandscapeOptBarAnimatorSet != null
                && mHideLandscapeOptBarAnimatorSet.isRunning()) {
            mHideLandscapeOptBarAnimatorSet.cancel();
        }

        if (mHidePortraitOptBarAnimatorSet != null
                && mHidePortraitOptBarAnimatorSet.isRunning()) {
            mHidePortraitOptBarAnimatorSet.cancel();
        }

        if (mShowLandscapeOptBarAnimatorSet != null
                && mShowLandscapeOptBarAnimatorSet.isRunning()) {
            mShowLandscapeOptBarAnimatorSet.cancel();
        }

        if (mShowPortraitOptBarAnimatorSet != null
                && mShowPortraitOptBarAnimatorSet.isRunning()) {
            mShowPortraitOptBarAnimatorSet.cancel();
        }
    }

    /**
     * 关注结果回调
     *
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
        } else {
            ToastUtils.showToast("关注失败 code:" + resultCode);
        }
    }

    private void touchViewOnclick() {
        if (mIsLandscape) {
            if (KeyboardUtils.hideKeyboardThenReturnResult((Activity) getContext())) {
                return;
            }

            if (!mIsLandscapeHideOptMode) {
                hideOptBar();
            } else {
                showOptBar();
            }
        } else {
            if (!mIsPortraitHideOptMode) {
                hideOptBar();
            } else {
                showOptBar();
            }
        }
    }

    public void checkInstalledOrUpdate(GameInfoModel gameInfoModel) {
        if (gameInfoModel == null) {
            return;
        }
        String packageName = gameInfoModel.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            // 无效的包名
            mLandscapeDownloadBtn.setVisibility(GONE);
            return;
        } else {
            mLandscapeDownloadBtn.setVisibility(VISIBLE);
            if (PackageUtils.isInstallPackage(packageName)) {
                // 启动
                mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH);
            } else {
                String apkPath = CustomDownloadManager.getInstance().getDownloadPath(gameInfoModel.getPackageUrl());
                if (PackageUtils.isCompletedPackage(apkPath, gameInfoModel.getPackageName())) {
                    mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED);
                } else {
                    mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD);
                }
            }
        }
    }

    /**
     * 点击显示隐藏操作栏bar
     */
    private void hideOptBar() {
        if (mIsLandscape) {
            hideLandscapeOptBar();
        } else {
            hidePortraitOptBar();
        }
    }

    private void showOptBar() {
        if (mIsLandscape) {
            showLandscapeOptBar();
        } else {
            showPortraitOptBar();
        }
    }

    private void hideLandscapeOptBar() {
        if (mIsLandscapeHideOptMode) {
            return;
        }

        mUiHanlder.removeCallbacks(mHideLandscapeOptBarRunnable);

        if (mHideLandscapeOptBarAnimatorSet == null) {
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
        if (mIsPortraitHideOptMode) {
            return;
        }
        mUiHanlder.removeCallbacks(mHidePortraitOptBarRunnable);

        if (mHidePortraitOptBarAnimatorSet == null) {
            ObjectAnimator portraitBackBtnHideAnimator = ObjectAnimator.ofFloat(mPortraitTitleContainer
                    , View.TRANSLATION_Y
                    , mPortraitTitleContainer.getTranslationY()
                    , mPortraitTitleContainer.getTranslationY() - mPortraitTitleContainer.getBottom());
            ObjectAnimator portraitVoiceBtnHideAnimator = ObjectAnimator.ofFloat(mPortraitVoiceBtn
                    , View.TRANSLATION_Y
                    , mPortraitVoiceBtn.getTranslationY()
                    , mPortraitVoiceBtn.getBottom() - mPortraitVoiceBtn.getTranslationY());
            ObjectAnimator portraitLinUpButtonsHideAnimator = ObjectAnimator.ofFloat(mPortraitLinUpButtons
                    , View.TRANSLATION_X
                    , mPortraitLinUpButtons.getTranslationX() + (DisplayUtils.getScreenWidth() - mPortraitLinUpButtons.getLeft()));

            mHidePortraitOptBarAnimatorSet = new AnimatorSet();
            mHidePortraitOptBarAnimatorSet.playTogether(portraitBackBtnHideAnimator, portraitLinUpButtonsHideAnimator, portraitVoiceBtnHideAnimator);
            mHidePortraitOptBarAnimatorSet.setDuration(ANIMATION_DURATION);
            mHidePortraitOptBarAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    onAnimationEnd(animation);
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

    Runnable mHideLandscapeOptBarRunnable = new Runnable() {
        @Override
        public void run() {
            hideLandscapeOptBar();
        }
    };

    /**
     * 每次bar展现出来后5秒后就尝试取
     */
    private void tryToHideLandscapeOptBar() {
        if (!mIsLandscape) {
            return;
        }

        mUiHanlder.removeCallbacks(mHideLandscapeOptBarRunnable);
        mUiHanlder.postDelayed(mHideLandscapeOptBarRunnable,5000);
    }

    Runnable mHidePortraitOptBarRunnable = new Runnable() {
        @Override
        public void run() {
            hidePortraitOptBar();
        }
    };

    private void tryToHidePortraitOptBar() {
        if (mIsLandscape) {
            return;
        }

        mUiHanlder.removeCallbacks(mHidePortraitOptBarRunnable);
        mUiHanlder.postDelayed(mHidePortraitOptBarRunnable,5000);
    }

    private void showLandscapeOptBar() {
        if (!mIsLandscapeHideOptMode) {
            return;
        }

        if (mShowLandscapeOptBarAnimatorSet == null) {
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
                    tryToHideLandscapeOptBar();
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
        if (!mIsPortraitHideOptMode) {
            return;
        }

        if (mShowPortraitOptBarAnimatorSet == null) {
            ObjectAnimator portraitBackBtnShowAnimator = ObjectAnimator.ofFloat(mPortraitTitleContainer
                    , View.TRANSLATION_Y
                    , mPortraitTitleContainer.getTranslationY()
                    , mPortraitTitleContainer.getTranslationY() + mPortraitTitleContainer.getBottom());

            ObjectAnimator portraitVoiceBtnShowAnimator = ObjectAnimator.ofFloat(mPortraitVoiceBtn
                    , View.TRANSLATION_Y
                    , mPortraitVoiceBtn.getBottom()
                    , mPortraitVoiceBtn.getBottom() - mPortraitVoiceBtn.getTranslationY());

            ObjectAnimator portraitLinUpButtonsShowAnimator = ObjectAnimator.ofFloat(mPortraitLinUpButtons
                    , View.TRANSLATION_X
                    , mPortraitLinUpButtons.getTranslationX() - (DisplayUtils.getScreenWidth() - mPortraitLinUpButtons.getLeft()));

            mShowPortraitOptBarAnimatorSet = new AnimatorSet();
            mShowPortraitOptBarAnimatorSet.playTogether(portraitBackBtnShowAnimator, portraitLinUpButtonsShowAnimator, portraitVoiceBtnShowAnimator);
            mShowPortraitOptBarAnimatorSet.setDuration(ANIMATION_DURATION);
            mShowPortraitOptBarAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsPortraitHideOptMode = false;
                    tryToHidePortraitOptBar();
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
        if (mPresenter == null
                || mGameNewLandscapeInputView == null) {
            return;
        }
        WatchComponentController controller = mPresenter.getController();
        if (controller == null) {
            return;
        }
        mGameNewLandscapeInputViewPresenter = new GameNewLandscapeInputViewPresenter(controller
                , controller.getRoomBaseDataModel()
                , controller.getLiveRoomChatMsgManager());
        mGameNewLandscapeInputViewPresenter.setView(mGameNewLandscapeInputView.getViewProxy());
        mGameNewLandscapeInputView.setPresenter(mGameNewLandscapeInputViewPresenter);
        mGameNewLandscapeInputViewPresenter.startPresenter();
    }

    private void tryUnSubscribe() {
        mUiHanlder.removeCallbacks(mHidePortraitOptBarRunnable);
        mUiHanlder.removeCallbacks(mHideLandscapeOptBarRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHanlder.removeCallbacksAndMessages(null);
    }

    private void resolveKeyBoardEvent(boolean isHide) {
        if (!mIsLandscape) {
            return;
        }

        if (isHide) {
            tryToHideLandscapeOptBar();
        } else {
            tryUnSubscribe();
        }
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
                if (mGameNewLandscapeInputViewPresenter != null) {
                    mGameNewLandscapeInputViewPresenter.stopPresenter();
                }

                tryUnSubscribe();
            }

            @Override
            public void updateInstallStatus(int status, int progress, int reason) {
                MyLog.d(TAG, " status " + status + " progress " + progress);
                if (!mIsLandscape) {
                    // 竖屏不加载
                    return;
                }
                switch (status) {
                    case CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD:
                        mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD);
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING:
                        mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING);
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD:
                        if (reason == DownloadManager.PAUSED_WAITING_FOR_NETWORK) {
                            ToastUtils.showToast("等待网络");
                        }
                        mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD);
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED:
                        mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED);
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_FAILED:
                        mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD);
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH:
                        mLandscapeDownloadBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH);
                        break;
                    default:
                        break;

                }
            }

            @Override
            public void keyBoardEvent(boolean isHide) {
                resolveKeyBoardEvent(isHide);
            }

            @Override
            public void updatePauseEvent(boolean isPause) {
                mIsVideoPause = isPause;
                updatePlayBtnUi();
            }

            @Override
            public void setDownLoadBtnVisibility(boolean needShow) {
                mNeedHideDownLoadBtn = !needShow;
                if (mIsLandscape
                        && mLandscapeDownloadBtn != null) {
                    mLandscapeDownloadBtn.setVisibility(needShow ? VISIBLE : GONE);
                }
            }

            @Override
            public void updateMuteEvent(boolean isMute) {
                mIsVideoMute = isMute;
                updatePlayBtnUi();
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
        if(mPresenter != null) {
            if(mPortraitWatchGameWaterMarkView != null) {
                mPortraitWatchGameWaterMarkView.setRoomData(mPresenter.getController().getRoomBaseDataModel());
            }

            if(mLandscapeWatchGameWaterMarkView != null) {
                mLandscapeWatchGameWaterMarkView.setRoomData(mPresenter.getController().getRoomBaseDataModel());
            }
        }
    }

    public interface IPresenter {
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

        void clickDownLoad(int flag);

        void videoMute(boolean isMute);
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

        /**
         * 下载回调
         *
         * @param status   下载的状态
         * @param progress 下载的进度条
         * @param reason
         */
        void updateInstallStatus(int status, int progress, int reason);

        void keyBoardEvent(boolean isHide);

        void updatePauseEvent(boolean isPause);

        void setDownLoadBtnVisibility(boolean needShow);

        void updateMuteEvent(boolean isMute);
    }
}
