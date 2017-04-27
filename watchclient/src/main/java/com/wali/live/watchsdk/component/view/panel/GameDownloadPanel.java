package com.wali.live.watchsdk.component.view.panel;

import android.app.DownloadManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.toast.ToastUtils;
import com.base.view.MyRatingBar;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.viewmodel.GameViewModel;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.mi.milink.sdk.base.Global.getSystemService;

/**
 * Created by xiaolan on 17/4/11.
 */
public class GameDownloadPanel extends BaseBottomPanel<RelativeLayout, RelativeLayout>
        implements IComponentView<GameDownloadPanel.IPresenter, GameDownloadPanel.IView> {
    @Nullable
    protected GameDownloadPanel.IPresenter mPresenter;

    private View mHeadBgView;
    private BaseImageView mGameIv;
    private TextView mGameTv;
    private MyRatingBar mGameRb;
    private TextView mClassTv;
    private TextView mCountTv;
    private TextView mDownloadTv;

//    private GestureDetector mGestureDetector;

    private GameViewModel mGameViewModel;
    private long mDownloadId;

    public GameDownloadPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.game_download_view;
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mHeadBgView = $(R.id.head_bg_view);
        mGameIv = $(R.id.game_iv);
        mGameTv = $(R.id.game_tv);
        mGameRb = $(R.id.game_rb);
        mClassTv = $(R.id.class_tv);
        mCountTv = $(R.id.count_tv);
        mDownloadTv = $(R.id.download_tv);

//        mGestureDetector = new GestureDetector(mContentView.getContext(),
//                new GestureDetector.SimpleOnGestureListener() {
//                    public boolean onFling(MotionEvent e1, MotionEvent e2,
//                                           float velocityX, float velocityY) {
//                        if (velocityY > 1000) {
//                            hideSelf(true);
//                        }
//                        return false;
//                    }
//                });
//        mHeadBgView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                mGestureDetector.onTouchEvent(event);
//                return true;
//            }
//        });

        mHeadBgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSelf(true);
            }
        });
    }

    @Override
    public void setPresenter(@Nullable GameDownloadPanel.IPresenter presenter) {
        mPresenter = presenter;
    }

    private void inflate() {
        if (mContentView == null) {
            inflateContentView();
        }
    }

    private void showGameDownloadView() {
        inflate();

        GameViewModel gameModel = mPresenter.getGameModel();
        if (gameModel != mGameViewModel) {
            mGameViewModel = gameModel;

            FrescoWorker.loadImage(mGameIv, ImageFactory.newHttpImage(mGameViewModel.getIconUrl()).build());

            mGameTv.setText(mGameViewModel.getName());
            mGameRb.setStarValue(mGameViewModel.getRatingScore());

            mClassTv.setText(mGameViewModel.getClassName());

            int count = mGameViewModel.getDownloadCount();
            if (count >= 10000) {
                mCountTv.setText(mContentView.getContext().getString(R.string.ten_thousand_download, count / 10000));
            } else {
                mCountTv.setText(mContentView.getContext().getString(R.string.count_download, count));
            }

            if (TextUtils.isEmpty(mGameViewModel.getDownloadUrl())) {
                mDownloadTv.setVisibility(View.GONE);
            } else {
                mDownloadTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mGameViewModel.getDownloadUrl()));
                        request.setTitle(mGameViewModel.getName());
                        mDownloadId = downloadManager.enqueue(request);

                        ToastUtils.showToast(R.string.downloading);

                        mPresenter.reportDownloadKey();
                    }
                });
                mDownloadTv.setVisibility(View.VISIBLE);
            }
        }
        showSelf(true, mIsLandscape);
    }

    private void hideGameDownloadView() {
        hideSelf(true);
    }

    @Override
    public GameDownloadPanel.IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements GameDownloadPanel.IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public void showGameDownloadView() {
                GameDownloadPanel.this.showGameDownloadView();
            }

            @Override
            public void hideGameDownloadView() {
                GameDownloadPanel.this.hideGameDownloadView();
            }

            @Override
            public void inflate() {
                GameDownloadPanel.this.inflate();
            }

            @Override
            public boolean isShow() {
                return GameDownloadPanel.this.isShow();
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        GameViewModel getGameModel();

        void reportDownloadKey();
    }

    public interface IView extends IViewProxy {
        void inflate();

        void showGameDownloadView();

        void hideGameDownloadView();

        boolean isShow();
    }
}
