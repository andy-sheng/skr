package com.wali.live.watchsdk.watch.view.watchgameview;

import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.utils.MD5;
import com.base.utils.StringUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.system.PackageUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.SupportHelper;
import com.wali.live.watchsdk.watch.adapter.GamePreviewPagerAdapter;
import com.wali.live.watchsdk.watch.download.CustomDownloadManager;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameHomeTabPresenter;

import java.io.File;
import java.util.List;

public class WatchGameHomeTabView extends RelativeLayout implements
        IComponentView<WatchGameHomeTabView.IPresenter, WatchGameHomeTabView.IView>, WatchGameTabView.GameTabChildView {

    public final static String TAG = "WatchGameHomeTabView";

    Handler mUiHanlder = new Handler();

    WatchComponentController mWatchComponentController;


    RelativeLayout mGameInfoContainer;
    BaseImageView mGameIconIv;
    TextView mGameNameTv;
    TextView mGameScoreTv;
    TextView mInstallBtn;
    ProgressBar mDownLoadProgressBar;
    RelativeLayout mGamePreviewContainer;
    GameWatchPreviewViewPager mGamePreviewViewPager;
    VideoPluginView mVideoPluginView;
    RelativeLayout mLabelContainer;
    TextView mVideoLabelTv;
    TextView mPicLabelTv;
    TextView mIndexTv;
    RelativeLayout mGameDetailContainer;
    TextView mGameDescTv;
    TextView mGameIntroduceTv;
    private GameTagView mGameTagView;
    private GameTagView mGameTagView1;


    GamePreviewPagerAdapter mGamePreviewPagerAdapter;


    WatchGameHomeTabPresenter mWatchGameHomeTabPresenter;


    ViewPager.OnPageChangeListener mPreviewPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            MyLog.d(TAG, "onPageScrolled" + " position=" + position + " positionOffset=" + positionOffset + " positionOffsetPixels=" + positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            MyLog.d(TAG, "onPageSelected" + " position=" + position);
            mIndexTv.setText(String.format("%d/%d", position + 1, mGamePreviewPagerAdapter.getCount()));
            //如果是视频，显示控制区域
            Object object = mGamePreviewPagerAdapter.getItemByPosition(position);
            if (object instanceof GameInfoModel.GameVideo) {

                mVideoPluginView.setVisibility(VISIBLE);

                mVideoLabelTv.setSelected(true);
                mPicLabelTv.setSelected(false);

                // 如果暂停模式 并且 播放是当前 item 的url，则显示视频view
                GameInfoModel.GameVideo gameVideo = (GameInfoModel.GameVideo) object;
                List<GameInfoModel.GameVideo.VideoBaseInfo> list = gameVideo.getVideoInfoList();
                if (list.size() > 0) {
                    GameInfoModel.GameVideo.VideoBaseInfo baseInfo = list.get(0);
                    mVideoPluginView.setVideoUrl(baseInfo.getVideoUrl());
                }

            } else {
                mVideoPluginView.setVisibility(GONE);
                mLabelContainer.setVisibility(VISIBLE);
                mVideoLabelTv.setSelected(false);
                mPicLabelTv.setSelected(true);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            MyLog.d(TAG, "onPageScrollStateChanged" + " state=" + state);
            if (state == ViewPager.SCROLL_STATE_IDLE) {

            } else {
                // 滑动时
                // 播放页面隐藏
                mVideoPluginView.setVisibility(GONE);
                mVideoPluginView.tryPauseVideo();
            }
        }
    };

    public WatchGameHomeTabView(Context context, WatchComponentController componentController) {
        super(context);
        mWatchComponentController = componentController;
        init(context, componentController);
    }

    private void init(Context context, WatchComponentController componentController) {
        inflate(context, R.layout.watch_game_tab_home_layout, this);


        mGameInfoContainer = (RelativeLayout) this.findViewById(R.id.game_info_container);
        mGameIconIv = (BaseImageView) this.findViewById(R.id.game_icon_iv);
        mGameNameTv = (TextView) this.findViewById(R.id.game_name_tv);
        mGameScoreTv = (TextView) this.findViewById(R.id.game_score_tv);
        mInstallBtn = (TextView) this.findViewById(R.id.install_btn);
        mDownLoadProgressBar = (ProgressBar) this.findViewById(R.id.install_progress);
        mGamePreviewContainer = (RelativeLayout) this.findViewById(R.id.game_preview_container);
        mGamePreviewViewPager = (GameWatchPreviewViewPager) this.findViewById(R.id.game_preview_view_pager);
        mVideoPluginView = (VideoPluginView) this.findViewById(R.id.video_plugin_view);
        mLabelContainer = (RelativeLayout) this.findViewById(R.id.label_container);
        mVideoLabelTv = (TextView) this.findViewById(R.id.video_label_tv);
        mPicLabelTv = (TextView) this.findViewById(R.id.pic_label_tv);
        mIndexTv = (TextView) this.findViewById(R.id.index_tv);
        mGameDetailContainer = (RelativeLayout) this.findViewById(R.id.game_detail_container);
        mGameDescTv = (TextView) this.findViewById(R.id.game_desc_tv);
        mGameIntroduceTv = (TextView) this.findViewById(R.id.game_introduce_tv);

        mGameTagView = (GameTagView) this.findViewById(R.id.game_tag_view);
        mGameTagView.setSingleLine(true);
        mGameTagView.setLineCenter(true);

        mGameTagView1 = (GameTagView) this.findViewById(R.id.game_tag_1_view);
        mGameTagView1.setSingleLine(true);
        mGameTagView1.setLineCenter(true);
        mGamePreviewPagerAdapter = new GamePreviewPagerAdapter();
        mGamePreviewViewPager.setAdapter(mGamePreviewPagerAdapter);

        mWatchGameHomeTabPresenter = new WatchGameHomeTabPresenter(componentController);
        mWatchGameHomeTabPresenter.setView(this.getViewProxy());
        setPresenter(mWatchGameHomeTabPresenter);

        mVideoPluginView.setEventController(componentController);
        mVideoPluginView.setOnClickListener(null);
        mVideoPluginView.setClickable(false);
        mVideoPluginView.setViewListener(new VideoPluginView.ViewListener() {
            @Override
            public void onControlViewVisiable(boolean visiable) {
                if (visiable) {
                    mLabelContainer.setVisibility(GONE);
                } else {
                    mLabelContainer.setVisibility(VISIBLE);
                }
            }
        });
        /**
         * 这里为了解决如果在  VideoPluginView setOnClickListener 的话，他就会吃掉点击事件
         * 导致他的兄弟节点的 viewpager 无法滑动。
         * 所以这里将事件源全部聚合到 mGamePreviewViewPager 中
         */
        mGamePreviewViewPager.setOutClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoPluginView.processControlView();
            }
        });

        mGamePreviewViewPager.addOnPageChangeListener(mPreviewPageChangeListener);

        mInstallBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = 0;
                if (mInstallBtn.getTag() != null) {
                    flag = (int) mInstallBtn.getTag();
                }
                if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD) {
                    mWatchGameHomeTabPresenter.beginDownload(true);
                } else if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING) {
                    mWatchGameHomeTabPresenter.pauseDownload();
                } else if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD) {
                    mWatchGameHomeTabPresenter.beginDownload(false);
                } else if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED) {
                    if (mWatchGameHomeTabPresenter.tryInstall()) {

                    } else {
                        // 安装失败，可能有地方信息不对称，重新下载吧。
                        ToastUtils.showToast("apk包解析失败，重新下载");
                        mDownLoadProgressBar.setVisibility(GONE);
                        mInstallBtn.setText(R.string.download);
                        mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.game_watch_home_install_btn_bg));
                    }
                } else if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH) {
                    if (mWatchGameHomeTabPresenter.tryLaunch()) {

                    } else {
                        ToastUtils.showToast("启动失败");
                    }
                }
            }
        });

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mWatchGameHomeTabPresenter != null) {
            mWatchGameHomeTabPresenter.startPresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWatchGameHomeTabPresenter != null) {
            mWatchGameHomeTabPresenter.stopPresenter();
        }
        if (mUiHanlder != null) {
            mUiHanlder.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public IView getViewProxy() {
        return new IView() {
            @Override
            public void updateUi(GameInfoModel gameInfoModel) {
                if (gameInfoModel != null) {
                    MyLog.d(TAG, "updateUi" + " gameInfoModel=" + gameInfoModel);
                    mGameNameTv.setText(StringUtils.subString(gameInfoModel.getGameName(), 6));
                    mGameScoreTv.setText(String.valueOf(gameInfoModel.getScore()));
                    BaseImage baseImage = ImageFactory.newHttpImage(GameInfoModel.getUrlWithPrefix(gameInfoModel.getIconUrl(), 480))
                            .setCornerRadius(DisplayUtils.dip2px(10))
                            .build();
                    FrescoWorker.loadImage(mGameIconIv, baseImage);

                    if (gameInfoModel.getGameVideoList().isEmpty()) {
                        mLabelContainer.setVisibility(GONE);
                        mVideoPluginView.setVisibility(GONE);
                    } else {
                        mLabelContainer.setVisibility(VISIBLE);
                        mVideoPluginView.setVisibility(VISIBLE);
                        mVideoPluginView.getVideoView().setVisibility(GONE);

                        List<GameInfoModel.GameVideo.VideoBaseInfo> list = gameInfoModel.getGameVideoList().get(0).getVideoInfoList();
                        if (list.size() > 0) {
                            GameInfoModel.GameVideo.VideoBaseInfo baseInfo = list.get(0);
                            mVideoPluginView.setVideoUrl(baseInfo.getVideoUrl());
                        }

                    }
                    mGamePreviewPagerAdapter.setData(gameInfoModel);
                    String introTitle = gameInfoModel.getIntroTitle();
                    if (TextUtils.isEmpty(introTitle)) {
                        introTitle = gameInfoModel.getGameName();
                    }
                    mGameDescTv.setText(introTitle);
                    mGameIntroduceTv.setText(gameInfoModel.getIntro());

                    List<GameInfoModel.GameTag> gameTagList = gameInfoModel.getGameTagList();
                    if (gameTagList != null && !gameTagList.isEmpty()) {
                        for (GameInfoModel.GameTag tag : gameTagList) {
                            if (tag.getTagType() == 0) {
                                mGameTagView.addTag(tag);
                            } else {
                                //做容错-没有的就不展示了
                                if (SupportHelper.contain(tag.getTagName()) || SupportHelper.getSupportResByUrl(tag.getActUrl()) != null) {
                                    GameUsageTagItemView gameUsageTagItemView = new GameUsageTagItemView(getContext());
                                    gameUsageTagItemView.bind(tag);
                                    mGameTagView1.addTag(gameUsageTagItemView);
                                }
                            }
                        }
                    }

                    checkInstalledOrUpdate(gameInfoModel);
                }
            }

            @Override
            public void updateDownLoadUi(int status, int progress, int reason, GameInfoModel gameInfoModel) {
                MyLog.d(TAG, " status " + status + " progress " + progress);
                switch (status) {
                    case CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD:
                        mDownLoadProgressBar.setVisibility(GONE);
                        mInstallBtn.setText(R.string.download);
                        mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.game_watch_home_install_btn_bg));
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING:
                        mDownLoadProgressBar.setVisibility(VISIBLE);
                        mDownLoadProgressBar.setProgress(progress);
                        mInstallBtn.setText(progress + "%");
                        mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.transparent_drawable));
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD:
                        mDownLoadProgressBar.setVisibility(VISIBLE);
                        mDownLoadProgressBar.setProgress(progress);
                        if (reason == DownloadManager.PAUSED_WAITING_FOR_NETWORK) {
                            mInstallBtn.setText("等待网络");
                        } else {
                            mInstallBtn.setText("继续");
                        }
                        mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.transparent_drawable));
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED:
                        mDownLoadProgressBar.setVisibility(GONE);
                        mInstallBtn.setText(R.string.install);
                        mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.game_watch_home_install_btn_bg));
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH:
                        mDownLoadProgressBar.setVisibility(GONE);
                        mInstallBtn.setText("启动");
                        mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.game_watch_home_install_btn_bg));
                        break;
                    case CustomDownloadManager.ApkStatusEvent.STATUS_REMOVE:
                        // 卸载,纠正tag和状态
                        checkInstalledOrUpdate(gameInfoModel);
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void notifyTaskRemove(int status) {
                int flag = 0;
                if (mInstallBtn.getTag() != null) {
                    flag = (int) mInstallBtn.getTag();
                }
                if (flag == CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOADING
                        || flag == CustomDownloadManager.ApkStatusEvent.STATUS_PAUSE_DOWNLOAD) {
                    // 任务被取消了，已下载或者下载中变为未下载
                    mDownLoadProgressBar.setVisibility(GONE);
                    mInstallBtn.setText(R.string.download);
                    mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD);
                    mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.game_watch_home_install_btn_bg));
                }
            }

            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameHomeTabView.this;
            }
        };
    }


    private void checkInstalledOrUpdate(GameInfoModel gameInfoModel) {
        if (gameInfoModel == null) {
            MyLog.w(TAG, "gameInfoModel is null");
            return;
        }
        String packageName = gameInfoModel.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            // 无效的包名
            mInstallBtn.setVisibility(GONE);
            return;
        } else {
            mInstallBtn.setVisibility(VISIBLE);
            if (PackageUtils.isInstallPackage(packageName)) {
                mInstallBtn.setText("启动");
                mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_LAUNCH);
            } else {
                String apkPath = CustomDownloadManager.getInstance().getDownloadPath(gameInfoModel.getPackageUrl());
                if (PackageUtils.isCompletedPackage(apkPath, gameInfoModel.getPackageName())) {
                    mInstallBtn.setText("安装");
                    mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_DOWNLOAD_COMPELED);
                } else {
                    mInstallBtn.setText("下载");
                    mInstallBtn.setTag(CustomDownloadManager.ApkStatusEvent.STATUS_NO_DOWNLOAD);
                }
            }
        }
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {

    }

    @Override
    public void select() {

    }

    @Override
    public void unselect() {
        if (mVideoPluginView != null) {
            mVideoPluginView.tryPauseVideo();
        }
    }

    public interface IView extends IViewProxy {
        void updateUi(GameInfoModel gameInfoModel);


        /**
         * 下载回调
         *
         * @param status   下载的状态
         * @param progress 进度条
         */
        void updateDownLoadUi(int status, int progress, int reason, GameInfoModel gameInfoModel);

        void notifyTaskRemove(int status);
    }

    public interface IPresenter {
        void beginDownload(boolean isFirst);

        void pauseDownload();

        boolean tryInstall();

        boolean tryLaunch();
    }


}
