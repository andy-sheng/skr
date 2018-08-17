package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.SupportHelper;
import com.wali.live.watchsdk.watch.adapter.GamePreviewPagerAdapter;
import com.wali.live.watchsdk.watch.download.GameDownLoadUtil;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameHomeTabPresenter;

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
    ProgressBar mDownLoad;
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
        mDownLoad = (ProgressBar) this.findViewById(R.id.install_progress);
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
                if (mInstallBtn.getText().equals(GlobalData.app().getResources().getString(R.string.download))) {
                    //下载
                    mWatchGameHomeTabPresenter.beginDownload();
                } else if (mInstallBtn.getText().equals(GlobalData.app().getResources().getString(R.string.install))) {
                    //安装

                } else if (mInstallBtn.getText().equals(GlobalData.app().getResources().getString(R.string.open))) {
                    //启动 先不做

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
                    mGameNameTv.setText(gameInfoModel.getGameName());
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
            public void updateDownLoadUi(int status, int progress) {
                MyLog.d(TAG, " status " + status + " progress " + progress);
                switch (status) {
                    case GameDownLoadUtil.DOWNLOAD:
                        mDownLoad.setVisibility(GONE);
                        mInstallBtn.setText(R.string.download);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.game_watch_home_install_btn_bg));
                        break;
                    case GameDownLoadUtil.DOWNLOAD_RUNNING:
                        mDownLoad.setVisibility(VISIBLE);
                        mDownLoad.setProgress(progress);
                        mInstallBtn.setText(progress + "%");
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.transparent_drawable));
                        break;
                    case GameDownLoadUtil.GAME_INSTALL:
                        mDownLoad.setVisibility(GONE);
                        mInstallBtn.setText(R.string.install);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.game_watch_home_install_btn_bg));
                        break;
                    case GameDownLoadUtil.GAME_LUNCH:
                        mDownLoad.setVisibility(GONE);
                        mInstallBtn.setText("已安装");
//                        mInstallBtn.setText(R.string.open);
                        mInstallBtn.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.game_watch_home_install_btn_bg));
                        break;
                    default:
                        break;
                }

            }

            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameHomeTabView.this;
            }
        };
    }

    private void checkInstalledOrUpdate(GameInfoModel gameInfoModel) {
        // TODO 需要优化,检查是否有下载完成的包
        String packageName = gameInfoModel.getPackageName();
        if (TextUtils.isEmpty(packageName)) {
            // 未安装
            mInstallBtn.setText("下载");
            return;
        }

        PackageManager packageManager = GlobalData.app().getPackageManager();
        List<PackageInfo> infos = null;
        PackageInfo localApp = null;
        try {
            infos = packageManager.getInstalledPackages(0);
            localApp = getContext().getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            mInstallBtn.setText("下载");
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (infos == infos || infos.size() == 0) {
            return;
        }

        if (infos.contains(localApp)) {
            // 已安装
            mInstallBtn.setText("已安装");
            return;
        } else {
            mInstallBtn.setText("下载");
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
        void updateDownLoadUi(int status, int progress);
    }

    public interface IPresenter {
        void beginDownload();
    }

}
