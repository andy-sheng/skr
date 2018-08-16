package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.GameIntroVideoPresenter;
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
    RelativeLayout mGamePreviewContainer;
    ViewPager mGamePreviewViewPager;
    TextureView mVideoView;
    ImageView mPlayerControlBtn;
    TextView mIndexTv;
    RelativeLayout mGameDetailContainer;
    TextView mGameDescTv;
    TextView mGameIntroduceTv;
    RelativeLayout mLabelContainer;
    TextView mVideoLabelTv;
    TextView mPicLabelTv;
    private GameTagView mGameTagView;
    private GameTagView mGameTagView1;

    GamePreviewPagerAdapter mGamePreviewPagerAdapter;


    WatchGameHomeTabPresenter mWatchGameHomeTabPresenter;
    GameIntroVideoPresenter mGameIntroVideoPresenter;

    GameIntroVideoPresenter getGameIntroVideoPresenter() {
        if (mGameIntroVideoPresenter == null) {
            mGameIntroVideoPresenter = new GameIntroVideoPresenter(mWatchComponentController, true);
            mGameIntroVideoPresenter.setView(mVideoView);
            mGameIntroVideoPresenter.startPresenter();
        }
        return mGameIntroVideoPresenter;
    }

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
                mPlayerControlBtn.setVisibility(VISIBLE);
                mVideoLabelTv.setSelected(true);
                mPicLabelTv.setSelected(false);

                // 如果暂停模式 并且 播放是当前 item 的url，则显示视频view
                if (getGameIntroVideoPresenter().isPause()) {
                    GameInfoModel.GameVideo gameVideo = (GameInfoModel.GameVideo) object;
                    List<GameInfoModel.GameVideo.VideoBaseInfo> list = gameVideo.getVideoInfoList();
                    if (list.size() > 0) {
                        GameInfoModel.GameVideo.VideoBaseInfo baseInfo = list.get(0);
                        if (baseInfo.getVideoUrl().equals(getGameIntroVideoPresenter().getOriginalStreamUrl())) {
                            mVideoView.setVisibility(VISIBLE);
                        }
                    }
                }
            } else {
                mPlayerControlBtn.setVisibility(GONE);
                mVideoLabelTv.setSelected(false);
                mPicLabelTv.setSelected(true);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            MyLog.d(TAG, "onPageScrollStateChanged" + " state=" + state);
            if (state == ViewPager.SCROLL_STATE_IDLE) {
            } else {
                // 滑动时，隐藏视频控制区域
                mPlayerControlBtn.setVisibility(GONE);
                // 播放页面隐藏
                mVideoView.setVisibility(GONE);

                // 当前播放的视频停止
                if (getGameIntroVideoPresenter().isStarted()) {
                    getGameIntroVideoPresenter().pauseVideo();
                }
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
        mGamePreviewContainer = (RelativeLayout) this.findViewById(R.id.game_preview_container);
        mGamePreviewViewPager = (ViewPager) this.findViewById(R.id.game_preview_view_pager);
        mGamePreviewPagerAdapter = new GamePreviewPagerAdapter();
        mGamePreviewViewPager.setAdapter(mGamePreviewPagerAdapter);

        mVideoView = (TextureView) this.findViewById(R.id.video_view);
        mPlayerControlBtn = (ImageView) this.findViewById(R.id.player_control_btn);
        mIndexTv = (TextView) this.findViewById(R.id.index_tv);
        mLabelContainer = (RelativeLayout) this.findViewById(R.id.label_container);
        mVideoLabelTv = (TextView) this.findViewById(R.id.video_label_tv);
        mPicLabelTv = (TextView) this.findViewById(R.id.pic_label_tv);

        mGameDetailContainer = (RelativeLayout) this.findViewById(R.id.game_detail_container);
        mGameDescTv = (TextView) this.findViewById(R.id.game_desc_tv);
        mGameIntroduceTv = (TextView) this.findViewById(R.id.game_introduce_tv);

        mGameTagView = (GameTagView) this.findViewById(R.id.game_tag_view);
        mGameTagView.setSingleLine(true);
        mGameTagView.setLineCenter(true);

        mGameTagView1 = (GameTagView) this.findViewById(R.id.game_tag_1_view);
        mGameTagView1.setSingleLine(true);
        mGameTagView1.setLineCenter(true);

        mWatchGameHomeTabPresenter = new WatchGameHomeTabPresenter(componentController);
        mWatchGameHomeTabPresenter.setView(this.getViewProxy());
        setPresenter(mWatchGameHomeTabPresenter);

        mGamePreviewViewPager.addOnPageChangeListener(mPreviewPageChangeListener);

        mPlayerControlBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 视频开始播放
                int position = mGamePreviewViewPager.getCurrentItem();
                Object object = mGamePreviewPagerAdapter.getItemByPosition(position);
                if (object instanceof GameInfoModel.GameVideo) {
                    GameInfoModel.GameVideo gameVideo = (GameInfoModel.GameVideo) object;
                    List<GameInfoModel.GameVideo.VideoBaseInfo> list = gameVideo.getVideoInfoList();
                    if (list.size() > 0) {
                        GameInfoModel.GameVideo.VideoBaseInfo baseInfo = list.get(0);
                        mVideoView.setVisibility(VISIBLE);
                        mPlayerControlBtn.setVisibility(GONE);
                        getGameIntroVideoPresenter().setOriginalStreamUrl(baseInfo.getVideoUrl());
                        if (getGameIntroVideoPresenter().isStarted()) {
                            getGameIntroVideoPresenter().resumeVideo();
                        } else {
                            getGameIntroVideoPresenter().startVideo();
                        }
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
        if (mGameIntroVideoPresenter != null) {
            mGameIntroVideoPresenter.startPresenter();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWatchGameHomeTabPresenter != null) {
            mWatchGameHomeTabPresenter.stopPresenter();
        }
        if (mGameIntroVideoPresenter != null) {
            mGameIntroVideoPresenter.stopPresenter();
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
                        mPlayerControlBtn.setVisibility(GONE);
                    } else {
                        mLabelContainer.setVisibility(VISIBLE);
                    }
                    mGamePreviewPagerAdapter.setData(gameInfoModel);
                    String introTitle = gameInfoModel.getIntroTitle();
                    if (TextUtils.isEmpty(introTitle)) {
                        introTitle = gameInfoModel.getGameName();
                    }
                    mGameDescTv.setText(introTitle);
                    mGameIntroduceTv.setText(gameInfoModel.getIntro());

                    List<GameInfoModel.GameTag> gameTagList = gameInfoModel.getGameTagList();
                    if(gameTagList != null && !gameTagList.isEmpty()) {
                        for(GameInfoModel.GameTag tag : gameTagList) {
                            if(tag.getTagType() == 0) {
                                mGameTagView.addTag(tag);
                            } else {
                                //做容错-没有的就不展示了
                                if(SupportHelper.contain(tag.getTagName())) {
                                    GameUsageTagItemView gameUsageTagItemView = new GameUsageTagItemView(getContext());
                                    gameUsageTagItemView.bind(tag);
                                    mGameTagView1.addTag(gameUsageTagItemView);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public <T extends View> T getRealView() {
                return (T) WatchGameHomeTabView.this;
            }
        };
    }

    @Override
    public void setPresenter(IPresenter iPresenter) {

    }

    @Override
    public void select() {

    }

    @Override
    public void unselect() {
        if (mGameIntroVideoPresenter != null) {
            mGameIntroVideoPresenter.stopVideo();
        }
    }

    public interface IView extends IViewProxy {
        void updateUi(GameInfoModel gameInfoModel);
    }

    public interface IPresenter {
    }

}
