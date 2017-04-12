//package com.wali.live.watchsdk.component.view;
//
//import android.content.Context;
//import android.support.annotation.IdRes;
//import android.support.annotation.Nullable;
//import android.util.AttributeSet;
//import android.view.View;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.base.image.fresco.BaseImageView;
//import com.base.image.fresco.FrescoWorker;
//import com.base.image.fresco.image.ImageFactory;
//import com.base.view.MyRatingBar;
//import com.wali.live.component.view.IComponentView;
//import com.wali.live.component.view.IViewProxy;
//import com.wali.live.watchsdk.R;
//import com.wali.live.watchsdk.component.viewmodel.GameViewModel;
//
///**
// * Created by lan on 2017/04/10.
// */
//public class GameDownloadView extends RelativeLayout implements IComponentView<GameDownloadView.IPresenter, GameDownloadView.IView> {
//    private static final String TAG = "GameDownloadView";
//
//    @Nullable
//    protected IPresenter mPresenter;
//
//    private BaseImageView mGameIv;
//    private TextView mGameTv;
//    private MyRatingBar mGameRb;
//    private TextView mClassTv;
//    private TextView mCountTv;
//
//    private GameViewModel mGameViewModel;
//
//    public GameDownloadView(Context context) {
//        this(context, null, 0);
//    }
//
//    public GameDownloadView(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public GameDownloadView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init(context, attrs, defStyleAttr);
//    }
//
//    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
//        inflate(context, R.layout.game_download_view, this);
//
//        mGameIv = $(R.id.game_iv);
//        mGameTv = $(R.id.game_tv);
//        mGameRb = $(R.id.game_rb);
//        mClassTv = $(R.id.class_tv);
//        mCountTv = $(R.id.count_tv);
//    }
//
//    protected final <T extends View> T $(@IdRes int resId) {
//        return (T) findViewById(resId);
//    }
//
//    protected final void $click(View view, View.OnClickListener listener) {
//        if (view != null) {
//            view.setOnClickListener(listener);
//        }
//    }
//
//    @Override
//    public void setPresenter(@Nullable IPresenter presenter) {
//        mPresenter = presenter;
//    }
//
//    private void showGameDownloadView() {
//        setVisibility(View.VISIBLE);
//
//        GameViewModel gameModel = mPresenter.getGameModel();
//        if (gameModel != mGameViewModel) {
//            FrescoWorker.loadImage(mGameIv, ImageFactory.newHttpImage(gameModel.getIconUrl()).build());
//
//            mGameTv.setText(gameModel.getName());
//            mGameRb.setStarValue(gameModel.getRatingScore());
//
//            mClassTv.setText(gameModel.getClassName());
//
//            int count = gameModel.getDownloadCount();
//            if (count >= 10000) {
//                mCountTv.setText(getContext().getString(R.string.ten_thousand_download, count / 10000));
//            } else {
//                mCountTv.setText(getContext().getString(R.string.count_download, count));
//            }
//
//            mGameViewModel = gameModel;
//        }
//    }
//
//    private void hideGameDownloadView() {
//        setVisibility(View.GONE);
//    }
//
//    @Override
//    public IView getViewProxy() {
//        /**
//         * 局部内部类，用于Presenter回调通知该View改变状态
//         */
//        class ComponentView implements IView {
//            @Override
//            public <T extends View> T getRealView() {
//                return (T) GameDownloadView.this;
//            }
//
//            @Override
//            public void showGameDownloadView() {
//                GameDownloadView.this.showGameDownloadView();
//            }
//
//            @Override
//            public void hideGameDownloadView() {
//                GameDownloadView.this.hideGameDownloadView();
//            }
//        }
//        return new ComponentView();
//    }
//
//    public interface IPresenter {
//        GameViewModel getGameModel();
//    }
//
//    public interface IView extends IViewProxy {
//        void showGameDownloadView();
//
//        void hideGameDownloadView();
//    }
//}
