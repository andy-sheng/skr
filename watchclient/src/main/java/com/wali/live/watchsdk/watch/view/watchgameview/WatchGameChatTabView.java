package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.proto.GameCenterProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.download.GameDownloadOptControl;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameChatTabPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameLiveCommentPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.wali.live.component.view.Utils.$click;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_CONTINUE;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_FAILED;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_INSTALLING;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH_SUCEESS;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD;
import static com.wali.live.watchsdk.watch.download.CustomDownloadManager.ApkStatusEvent.STATUS_REMOVE;

public class WatchGameChatTabView extends RelativeLayout implements
        IComponentView<WatchGameChatTabView.IPresenter, WatchGameChatTabView.IView>, WatchGameTabView.GameTabChildView, View.OnClickListener {

    private static final int ANCHOR_BADGE_CERT = DisplayUtils.dip2px(16f);
    private static final int ANCHOR_BADGE_UN_CERT = DisplayUtils.dip2px(11f);

    private static final String TAG = "WatchGameChatTabView";

    WatchGameChatTabPresenter mWatchGameChatTabPresenter;

    WatchGameLiveCommentView mWatchGameLiveCommentView;
    WatchGameLiveCommentPresenter mWatchGameLiveCommentPresenter;


    RelativeLayout mAnchorInfoContainer;
    BaseImageView mAnchorAvatarIv;
    TextView mAnchorNameTv;
    ImageView mUserBadgeIv;
    TextView mAnchorRoomTv;
    TextView mFocusBtn;
    TextView mViewerNum;
    RelativeLayout mCommentContainer;

    ViewStub mGameInfoPopViewStub;
    GameInfoPopView mGameInfoPopView; // 可能为空

    ImageView mGuideFollowView; //关注引导浮层

    private Handler mUiHandler = new Handler();

    public long mFollowGuideShowTimeInterval = 2 * 60 * 1000;//从开始观看到显示关注引导浮层的时间间隔
    public int mFollowGuideShowUserNum = 200;   //关注引导浮层显示的人数限制

    private boolean mEnableFollow = true;

    private boolean mIsDownLoadByGc = false;
    private WatchComponentController mController;

    public WatchGameChatTabView(Context context, WatchComponentController componentController) {
        super(context);
        init(context, componentController);
    }

    private void init(Context context, WatchComponentController componentController) {
        inflate(context, R.layout.watch_game_tab_chat_layout, this);

        mController = componentController;
        mAnchorInfoContainer = (RelativeLayout) this.findViewById(R.id.anchor_info_container);
        mAnchorAvatarIv = (BaseImageView) this.findViewById(R.id.anchor_avatar_iv);
        mUserBadgeIv = (ImageView) this.findViewById(R.id.anchor_badge_iv);
        mAnchorNameTv = (TextView) this.findViewById(R.id.anchor_name_tv);
        mAnchorRoomTv = (TextView) this.findViewById(R.id.anchor_room_tv);
        mFocusBtn = (TextView) this.findViewById(R.id.focus_btn);
        mViewerNum = (TextView) this.findViewById(R.id.viewer_num);
        mGameInfoPopViewStub = (ViewStub) this.findViewById(R.id.game_info_pop_view_stub);

        mGuideFollowView = (ImageView) this.findViewById(R.id.game_follow_guide);

        mCommentContainer = (RelativeLayout) this.findViewById(R.id.comment_container);
        mWatchGameLiveCommentView = (WatchGameLiveCommentView) this.findViewById(R.id.live_comment_view);
        mWatchGameLiveCommentView.setIsGameLive(true);
        mWatchGameLiveCommentView.setToken(componentController.mRoomChatMsgManager.toString());
        mWatchGameLiveCommentPresenter = new WatchGameLiveCommentPresenter(componentController);
        mWatchGameLiveCommentPresenter.setView(mWatchGameLiveCommentView.getViewProxy());
        mWatchGameLiveCommentView.setPresenter(mWatchGameLiveCommentPresenter);

        mWatchGameChatTabPresenter = new WatchGameChatTabPresenter(componentController);
        mWatchGameChatTabPresenter.setView(this.getViewProxy());
        this.setPresenter(mWatchGameChatTabPresenter);

        if (componentController.getRoomBaseDataModel() != null) {
            if (componentController.getRoomBaseDataModel().getGameInfoModel() != null) {
                GameDownloadOptControl.tryQueryGameDownStatus(componentController.getRoomBaseDataModel().getGameInfoModel());
            }
        }

        $click(mAnchorAvatarIv, this);
        $click(mFocusBtn, this);

        mGuideFollowView.postDelayed(mCheckShowFollowGuideRunnable, mFollowGuideShowTimeInterval);
    }

    Runnable mCheckShowFollowGuideRunnable = new Runnable() {
        @Override
        public void run() {
            checkShowFollowGuide(this);
        }
    };

    Runnable mHideFollowGuideRunnable = new Runnable() {
        @Override
        public void run() {
            dismissFollowGuidePopupWindow();
        }
    };

    private void checkShowFollowGuide(Runnable runnable) {
        // 是否已经关注了
        // 关注人数是否超过200
        if (mController.getRoomBaseDataModel().isFocused()) {
            return;
        }

        if (mController.getRoomBaseDataModel().getViewerCnt() < mFollowGuideShowUserNum) {
            return;
        }

        showFollowGuidePopupWindow();
    }

    /**
     * 显示关注引导浮层
     * <p>
     * 显示条件：1)热门主播（当前房间观众数>=200）
     * 2)观众未关注主播
     * 3)观众停留在直播间1分钟
     * 消失条件：点击弹窗区之外的屏幕，或者，3秒后消失
     */
    public void showFollowGuidePopupWindow() {
        mGuideFollowView.setVisibility(VISIBLE);

        mUiHandler.removeCallbacks(mHideFollowGuideRunnable);
        mUiHandler.postDelayed(mHideFollowGuideRunnable, 3000);
    }

    /**
     * 销毁关注引导浮层
     */
    public void dismissFollowGuidePopupWindow() {
        mGuideFollowView.setVisibility(GONE);
    }


    public void showGameInfoPopView(GameInfoModel gameInfoModel, int apkStatus, boolean isShow, boolean isDownloadBygc) {
        if (isShow) {
            if (isDownloadBygc) {
                switch (apkStatus) {
                    case STATUS_NO_DOWNLOAD: //未下载
                    case STATUS_DOWNLOADING: //下载中
                    case STATUS_PAUSE_DOWNLOAD: //暂停下载
                    case STATUS_DOWNLOAD_COMPELED://已下载待安装
                    case STATUS_DOWNLOAD_FAILED: //下载失败
                    case STATUS_INSTALLING: //安装中
                        if (mGameInfoPopView == null) {
                            mGameInfoPopView = (GameInfoPopView) mGameInfoPopViewStub.inflate().findViewById(R.id.game_info_pop_container);
                            mGameInfoPopView.setOnInstallOrLaunchListener(new GameInfoPopView.OnInstallOrLaunchListener() {
                                @Override
                                public void onInstallorLaunch() {
                                    // 点击游戏挂件安装游戏　此时要暂停视频
                                    mWatchGameChatTabPresenter.pauseVideo();
                                }
                            });
                        } else {
                            mGameInfoPopView.showOrHidePopView(VISIBLE);
                        }
                        // 绑定数据
                        mGameInfoPopView.setGameInfoModel(gameInfoModel, apkStatus, isDownloadBygc);
                        break;
                    case STATUS_LAUNCH://启动
                    case STATUS_REMOVE://卸载
                    case STATUS_CONTINUE://继续
                    case STATUS_LAUNCH_SUCEESS: //启动成功
                        if (mGameInfoPopView != null) {
                            mGameInfoPopView.showOrHidePopView(GONE);
                        }
                        break;
                }
            } else {
                if (mGameInfoPopView == null) {
                    mGameInfoPopView = (GameInfoPopView) mGameInfoPopViewStub.inflate().findViewById(R.id.game_info_pop_container);
                    mGameInfoPopView.setOnInstallOrLaunchListener(new GameInfoPopView.OnInstallOrLaunchListener() {
                        @Override
                        public void onInstallorLaunch() {
                            // 点击游戏挂件安装游戏　此时要暂停视频
                            mWatchGameChatTabPresenter.pauseVideo();
                        }
                    });
                } else {
                    mGameInfoPopView.showOrHidePopView(VISIBLE);
                }
                // 绑定数据
                mGameInfoPopView.setGameInfoModel(gameInfoModel, apkStatus, isDownloadBygc);
            }
        } else {
            if (mGameInfoPopView != null) {
                mGameInfoPopView.showOrHidePopView(GONE);
            }
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mWatchGameLiveCommentPresenter != null) {
            mWatchGameLiveCommentPresenter.startPresenter();
        }
        if (mWatchGameChatTabPresenter != null) {
            mWatchGameChatTabPresenter.startPresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWatchGameLiveCommentPresenter != null) {
            mWatchGameLiveCommentPresenter.stopPresenter();
        }
        if (mWatchGameChatTabPresenter != null) {
            mWatchGameChatTabPresenter.stopPresenter();
        }
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onClick(View v) {
        if (mWatchGameChatTabPresenter == null) {
            MyLog.w(TAG, "onClick, mPresenter is null！");
            return;
        }
        int id = v.getId();
        if (id == R.id.anchor_avatar_iv) {
            mWatchGameChatTabPresenter.getAnchorInfo();
        } else if (id == R.id.focus_btn) {
            mWatchGameChatTabPresenter.followAnchor();
        }
    }

    @Override
    public IView getViewProxy() {
        return new IView() {

            @Override
            public void showFollowBtn(boolean needShow, boolean isFollow) {
                mEnableFollow = needShow;
                if (needShow) {
                    mFocusBtn.setVisibility(VISIBLE);
                    if (isFollow) {
                        mFocusBtn.setText(R.string.followed);
                        mFocusBtn.setEnabled(false);
                        mUiHandler.removeCallbacks(mHideFollowGuideRunnable);
                        dismissFollowGuidePopupWindow();
                    } else {
                        mFocusBtn.setText(R.string.follow);
                        mFocusBtn.setEnabled(true);
                    }
                } else {
                    mFocusBtn.setVisibility(INVISIBLE);
                }

            }

            @Override
            public void onFollowResult(int resultCode) {
                if (mEnableFollow) {
                    if (resultCode == ErrorCode.CODE_RELATION_BLACK) {
                        ToastUtils.showToast(getResources().getString(R.string.setting_black_follow_hint));
                    } else if (resultCode == 0) {
                        mFocusBtn.setText(R.string.followed);
                        mFocusBtn.setEnabled(false);
                        MyUserInfoManager.getInstance().getUser().setFollowNum(MyUserInfoManager.getInstance().getUser().getFollowNum() + 1);
                        ToastUtils.showMultiToast("关注成功,小米直播更多虎牙、\n熊猫顶尖游戏玩家等你哦", Gravity.CENTER);
                    } else if (resultCode == -1) {
                        ToastUtils.showToast(getResources().getString(R.string.follow_failed));
                    } else {
                        ToastUtils.showToast("关注失败 code:" + resultCode);
                    }
                }
            }


            @Override
            public void updateViewerNum(int viewerCnt) {
                String info;
                if (viewerCnt > 9999) {
                    info = String.format("%.1f万人", viewerCnt / 10000.0);
                } else {
                    info = String.format("%d人", viewerCnt);
                }
                mViewerNum.setText(info);
            }

            @Override
            public void updateAnchorInfo(long uid, long avatarTs, int certificationType, int level, String nickName) {
                AvatarUtils.loadAvatarByUidTs(mAnchorAvatarIv, uid, avatarTs, true);
                mUserBadgeIv.setVisibility(View.VISIBLE);
                LayoutParams badgeLp = (LayoutParams) mUserBadgeIv.getLayoutParams();
                if (certificationType > 0) {
                    badgeLp.width = ANCHOR_BADGE_CERT;
                    badgeLp.height = ANCHOR_BADGE_CERT;
                    mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(certificationType));
                } else {
                    badgeLp.width = ANCHOR_BADGE_UN_CERT;
                    badgeLp.height = ANCHOR_BADGE_UN_CERT;
                    mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(level));
                }
                mUserBadgeIv.setLayoutParams(badgeLp);
                if (!TextUtils.isEmpty(nickName)) {
                    CommonUtils.setMaxEclipse(mAnchorNameTv, DisplayUtils.dip2px(75), nickName);
                } else if (uid > 0) {
                    CommonUtils.setMaxEclipse(mAnchorNameTv, DisplayUtils.dip2px(75), String.valueOf(uid));
                } else {
                    mAnchorNameTv.setText(R.string.watch_owner_name_default);
                }

                mAnchorRoomTv.setText(String.format(getResources().getString(R.string.anchor_room_id), String.valueOf(uid)));

            }

            @Override
            public void updateGameInfo(RoomBaseDataModel source) {
                if (source == null) {
                    return;
                }
                GameDownloadOptControl.tryQueryGameDownStatus(source.getGameInfoModel());
            }

            @Override
            public void updateGamePopView(GameInfoModel model, int apkStatus, boolean isShow, boolean isDownloadBygc) {
                showGameInfoPopView(model, apkStatus, isShow, isDownloadBygc);
            }


            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameChatTabView.this;
            }
        };
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {
        iPresenter.updateUi();
    }

    @Override
    public void select() {
        if (mGameInfoPopView != null) {
            mGameInfoPopView.onExposureStatistic();
        }
    }

    @Override
    public void unselect() {

    }

    public interface IView extends IViewProxy {
        /**
         * 初始化关注
         */
        void showFollowBtn(boolean needShow, boolean isFollow);

        /**
         * 关注主播结果
         */
        void onFollowResult(int resultCode);

        /**
         * 更新观众人数
         */
        void updateViewerNum(int viewerCnt);

        /**
         * 更新主播信息
         */
        void updateAnchorInfo(long uid, long avatarTs, int certificationType, int level, String nickName);

        /**
         * 更新游戏信息
         */
        void updateGameInfo(RoomBaseDataModel source);

        /**
         * 更新游戏挂件(下载安装,初始化)
         */
        void updateGamePopView(GameInfoModel model, int apkStatus, boolean isShow, boolean isDownloadBygc);
    }

    public interface IPresenter {

        /**
         * 打开主播信息框
         */
        void getAnchorInfo();

        /**
         * 关注主播
         */
        void followAnchor();

        /**
         * 更新数据
         */
        void updateUi();

        /**
         * 暂停视频
         */
        void pauseVideo();

    }
}
