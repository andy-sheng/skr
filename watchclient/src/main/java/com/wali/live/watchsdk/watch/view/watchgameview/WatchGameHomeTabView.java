package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.gamecenter.model.GameInfoModel;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.view.ImagePagerView;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameHomeTabPresenter;

public class WatchGameHomeTabView extends RelativeLayout implements
        IComponentView<WatchGameHomeTabView.IPresenter, WatchGameHomeTabView.IView> {

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


    WatchGameHomeTabPresenter mWatchGameHomeTabPresenter;

    public WatchGameHomeTabView(Context context, WatchComponentController componentController) {
        super(context);
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
        mVideoView = (TextureView) this.findViewById(R.id.video_view);
        mPlayerControlBtn = (ImageView) this.findViewById(R.id.player_control_btn);
        mIndexTv = (TextView) this.findViewById(R.id.index_tv);
        mGameDetailContainer = (RelativeLayout) this.findViewById(R.id.game_detail_container);
        mGameDescTv = (TextView) this.findViewById(R.id.game_desc_tv);
        mGameIntroduceTv = (TextView) this.findViewById(R.id.game_introduce_tv);

        mWatchGameHomeTabPresenter = new WatchGameHomeTabPresenter(componentController);
        mWatchGameHomeTabPresenter.setView(this.getViewProxy());
        setPresenter(mWatchGameHomeTabPresenter);
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
    }

    @Override
    public IView getViewProxy() {
        return new IView() {
            @Override
            public void updateUi(GameInfoModel gameInfoModel) {
                if (gameInfoModel != null) {
                    mGameNameTv.setText(gameInfoModel.getGameName());
                    mGameScoreTv.setText(String.format(".1f%", gameInfoModel.getScore()));
                    BaseImage baseImage = ImageFactory.newHttpImage(gameInfoModel.getIconUrl())
                            .setCornerRadius(DisplayUtils.dip2px(10))
                            .build();
                    FrescoWorker.loadImage(mGameIconIv,baseImage);
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

    public interface IView extends IViewProxy {
        void updateUi(GameInfoModel gameInfoModel);
    }

    public interface IPresenter {
    }

}
