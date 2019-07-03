package com.module.playways.grab.room.view;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.crash.IgnoreException;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.ImageUtils;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.room.song.model.MiniGameInfoModel;
import com.zq.live.proto.Common.EMiniGamePlayType;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;

import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.R;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.zq.live.proto.Common.StandPlayType;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSource;
import okio.Okio;


/**
 * 转场时的歌曲信息页
 */
public class SongInfoCardView extends ConstraintLayout {

    public final static String TAG = "SongInfoCardView";

    //    SimpleDraweeView mSongCoverIv;
    ExTextView mSongNameTv;
    TextView mSongTagTv;
    TextView mWriterTv;
    //    ExTextView mSongSingerTv;
    BitmapTextView mCurrentSeq;
    BitmapTextView mTotalSeq;
    ExTextView mSongLyrics;
    ImageView mGrabCd;
    ImageView mGrabChorus;
    ImageView mGrabPk;

    RotateAnimation mRotateAnimation;    // cd的旋转
    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    Disposable mDisposable;

    Drawable mChorusDrawable;
    Drawable mPKDrawable;
    Drawable mMiniGameDrawable;

    public SongInfoCardView(Context context) {
        super(context);
        init();
    }

    public SongInfoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SongInfoCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_song_info_card_layout, this);
//        mSongCoverIv = (SimpleDraweeView) findViewById(R.id.song_cover_iv);
        mSongNameTv = (ExTextView) findViewById(R.id.song_name_tv);
        mSongTagTv = (TextView) findViewById(R.id.song_tag_tv);
        mWriterTv = (TextView) findViewById(R.id.writer_tv);
//        mSongSingerTv = (ExTextView) findViewById(R.id.song_singer_tv);
        mCurrentSeq = (BitmapTextView) findViewById(R.id.current_seq);
        mTotalSeq = (BitmapTextView) findViewById(R.id.total_seq);
        mSongLyrics = (ExTextView) findViewById(R.id.song_lyrics);
        mGrabCd = (ImageView) findViewById(R.id.grab_cd);
        mGrabChorus = (ImageView) findViewById(R.id.grab_chorus);
        mGrabPk = (ImageView) findViewById(R.id.grab_pk);

        mChorusDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#7088FF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2f))
                .setStrokeColor(Color.WHITE)
                .build();

        mPKDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#E55088"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2f))
                .setStrokeColor(Color.WHITE)
                .build();

        mMiniGameDrawable = new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#61B14F"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2f))
                .setStrokeColor(Color.WHITE)
                .build();
    }

    // 该动画需要循环播放
    public void bindSongModel(int curRoundSeq, int totalSeq, SongModel songModel) {
        MyLog.d(TAG, "bindSongModel" + " songModel=" + songModel);
        if (songModel == null) {
            return;
        }

        setVisibility(VISIBLE);
//        if (!TextUtils.isEmpty(songModel.getCover())) {
//            FrescoWorker.loadImage(mSongCoverIv,
//                    ImageFactory.newPathImage(songModel.getCover())
//                            .setCornerRadius(U.getDisplayUtils().dip2px(6))
//                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
//                            .setBorderColor(U.getColor(R.color.white))
//                            .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
//                            .build());
//        } else {
//            FrescoWorker.loadImage(mSongCoverIv,
//                    ImageFactory.newResImage(R.drawable.xuanzegequ_wufengmian)
//                            .setCornerRadius(U.getDisplayUtils().dip2px(6))
//                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
//                            .setBorderColor(U.getColor(R.color.white)).build());
//        }
//        mSongSingerTv.setText(songModel.getOwner());
        mSongLyrics.setText("歌词加载中...");
        mCurrentSeq.setText("" + curRoundSeq);
        mTotalSeq.setText("" + totalSeq);

        String desc = "";
        if (!TextUtils.isEmpty(songModel.getWriter())) {
            desc = "词/" + songModel.getWriter();
        }
        if (!TextUtils.isEmpty(desc)) {
            if (!TextUtils.isEmpty(songModel.getComposer())) {
                desc = " 曲/" + songModel.getComposer();
            }
        } else {
            if (!TextUtils.isEmpty(songModel.getComposer())) {
                desc = "曲/" + songModel.getComposer();
            }
        }

        if (TextUtils.isEmpty(desc)) {
            mWriterTv.setVisibility(GONE);
        } else {
            mWriterTv.setVisibility(VISIBLE);
            mWriterTv.setText(desc);
        }

        if (songModel.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
            // 合唱
            mSongNameTv.setText("" + songModel.getDisplaySongName());
            mGrabCd.clearAnimation();
            mGrabCd.setVisibility(GONE);
            mGrabChorus.setVisibility(VISIBLE);
            mGrabPk.setVisibility(GONE);
            mSongTagTv.setText("合唱");
            mSongTagTv.setVisibility(VISIBLE);
            mSongTagTv.setBackground(mChorusDrawable);
            // 入场动画
            animationGo(false);
        } else if (songModel.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
            // PK
            mSongNameTv.setText("" + songModel.getDisplaySongName());
            mGrabCd.clearAnimation();
            mGrabCd.setVisibility(GONE);
            mGrabChorus.setVisibility(GONE);
            mGrabPk.setVisibility(VISIBLE);
            mSongTagTv.setText("PK");
            mSongTagTv.setVisibility(VISIBLE);
            mSongTagTv.setBackground(mPKDrawable);
            // 入场动画
            animationGo(false);
        } else if (songModel.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
            // 小游戏
            if (songModel.getMiniGame() != null) {
                mSongNameTv.setText("【" + songModel.getMiniGame().getGameName() + "】");
            } else {
                MyLog.w(TAG, "bindSongModel" + " 给的是小游戏类型，但是结构给的空？？？服务器BYD和佳胜");
            }
            mGrabCd.clearAnimation();
            mGrabCd.setVisibility(GONE);
            // 和合唱一样的卡片
            mGrabChorus.setVisibility(VISIBLE);
            mGrabPk.setVisibility(GONE);
            mSongTagTv.setText("双人游戏");
            mSongTagTv.setVisibility(VISIBLE);
            mSongTagTv.setBackground(mMiniGameDrawable);
            // 入场动画
            animationGo(false);
        } else {
            // 普通
            mSongNameTv.setText("《" + songModel.getItemName() + "》");
            mGrabCd.setVisibility(VISIBLE);
            mGrabChorus.setVisibility(GONE);
            mGrabPk.setVisibility(GONE);
            mSongTagTv.setVisibility(GONE);
            // 入场动画
            animationGo(true);
        }
        playLyric(songModel);
    }

    public void playLyric(SongModel songModel) {
        if (songModel == null) {
            MyLog.w(TAG, "songModel 是空的");
            return;
        }
        if (songModel.getPlayType() == StandPlayType.PT_MINI_GAME_TYPE.getValue()) {
            MiniGameInfoModel gameInfoModel = songModel.getMiniGame();
            if (gameInfoModel != null) {
                mSongLyrics.setText(gameInfoModel.getDisplayGameRule());
            } else {
                MyLog.w(TAG, "miniGameInfo 是空的");
            }
        } else {
            LyricsManager.getLyricsManager(U.app())
                    .loadGrabPlainLyric(songModel.getStandLrc())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String o) {
                            mSongLyrics.setText("");
                            if (U.getStringUtils().isJSON(o)) {
                                NewChorusLyricModel newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel.class);
                                for (int i = 0; i < newChorusLyricModel.getItems().size() && i < 2; i++) {
                                    mSongLyrics.append(newChorusLyricModel.getItems().get(i).getWords());
                                    if (i == 0) {
                                        mSongLyrics.append("\n");
                                    }
                                }
                            } else {
                                mSongLyrics.setText(o);
                            }
                        }
                    }, throwable -> MyLog.e(TAG, throwable));
        }
    }

    /**
     * 入场动画
     *
     * @param isFlag 标记cd是否转动
     */
    private void animationGo(boolean isFlag) {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        this.startAnimation(mEnterTranslateAnimation);

        if (isFlag) {
            if (mRotateAnimation == null) {
                mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                mRotateAnimation.setDuration(3000);
                mRotateAnimation.setRepeatCount(Animation.INFINITE);
                mRotateAnimation.setFillAfter(true);
                mRotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            }
            mGrabCd.startAnimation(mRotateAnimation);
        }
    }

    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }
            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mGrabCd.clearAnimation();
                    clearAnimation();
                    if (mRotateAnimation != null) {
                        mRotateAnimation.cancel();
                    }
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(mLeaveTranslateAnimation);
        } else {
            if (mRotateAnimation != null) {
                mRotateAnimation.cancel();
            }
            mGrabCd.clearAnimation();
            clearAnimation();
            setVisibility(GONE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }
        if (mRotateAnimation != null) {
            mRotateAnimation.setAnimationListener(null);
            mRotateAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}

