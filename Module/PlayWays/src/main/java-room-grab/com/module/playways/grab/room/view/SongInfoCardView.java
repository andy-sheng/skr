package com.module.playways.grab.room.view;


import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.crash.IgnoreException;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.ImageUtils;
import com.module.playways.grab.room.model.NewChorusLyricModel;
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
public class SongInfoCardView extends RelativeLayout {

    public final static String TAG = "SongInfoCardView";

    SimpleDraweeView mSongCoverIv;
    ExTextView mSongNameTv;
    ExTextView mSongSingerTv;
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
        mSongCoverIv = (SimpleDraweeView) findViewById(R.id.song_cover_iv);
        mSongNameTv = (ExTextView) findViewById(R.id.song_name_tv);
        mSongSingerTv = (ExTextView) findViewById(R.id.song_singer_tv);
        mCurrentSeq = (BitmapTextView) findViewById(R.id.current_seq);
        mTotalSeq = (BitmapTextView) findViewById(R.id.total_seq);
        mSongLyrics = (ExTextView) findViewById(R.id.song_lyrics);
        mGrabCd = (ImageView) findViewById(R.id.grab_cd);
        mGrabChorus = (ImageView) findViewById(R.id.grab_chorus);
        mGrabPk = (ImageView) findViewById(R.id.grab_pk);
    }

    // 该动画需要循环播放
    public void bindSongModel(int curRoundSeq, int totalSeq, SongModel songModel) {
        MyLog.d(TAG, "bindSongModel" + " songModel=" + songModel);
        if (songModel == null || TextUtils.isEmpty(songModel.getCover())) {
            return;
        }

        setVisibility(VISIBLE);
        if (!TextUtils.isEmpty(songModel.getCover())) {
            FrescoWorker.loadImage(mSongCoverIv,
                    ImageFactory.newPathImage(songModel.getCover())
                            .setCornerRadius(U.getDisplayUtils().dip2px(6))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(U.getColor(R.color.white))
                            .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.getW()).build())
                            .build());
        } else {
            FrescoWorker.loadImage(mSongCoverIv,
                    ImageFactory.newResImage(R.drawable.xuanzegequ_wufengmian)
                            .setCornerRadius(U.getDisplayUtils().dip2px(6))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setBorderColor(U.getColor(R.color.white)).build());
        }
        mSongSingerTv.setText(songModel.getOwner());
        mSongLyrics.setText("歌词加载中...");
        mCurrentSeq.setText("" + curRoundSeq);
        mTotalSeq.setText("" + totalSeq);
        if (songModel.getPlayType() == StandPlayType.PT_CHO_TYPE.getValue()) {
            // 合唱
            mSongNameTv.setText("" + songModel.getItemName());
            mGrabCd.setVisibility(GONE);
            mGrabChorus.setVisibility(VISIBLE);
            mGrabPk.setVisibility(GONE);
            // 入场动画
            animationGo(false);
        } else if (songModel.getPlayType() == StandPlayType.PT_SPK_TYPE.getValue()) {
            // PK
            mSongNameTv.setText("" + songModel.getItemName());
            mGrabCd.setVisibility(GONE);
            mGrabChorus.setVisibility(GONE);
            mGrabPk.setVisibility(VISIBLE);
            // 入场动画
            animationGo(false);
        } else {
            // 普通
            mSongNameTv.setText("《" + songModel.getItemName() + "》");
            mGrabCd.setVisibility(VISIBLE);
            mGrabChorus.setVisibility(GONE);
            mGrabPk.setVisibility(GONE);
            // 入场动画
            animationGo(true);
        }
        playLyric(songModel);
    }

    public void playLyric(SongModel songModel) {
        if (songModel == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }

        File file = SongResUtils.getGrabLyricFileByUrl(songModel.getStandLrc());

        if (file == null || !file.exists()) {
            MyLog.w(TAG, "playLyric is not in local file");
            fetchLyricTask(songModel);
        } else {
            MyLog.w(TAG, "playLyric is exist");
            final File fileName = SongResUtils.getGrabLyricFileByUrl(songModel.getStandLrc());
            drawLyric(fileName);
        }
    }

    private void fetchLyricTask(SongModel songModel) {
        MyLog.w(TAG, "fetchLyricTask" + " songModel=" + songModel);
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        mDisposable = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File tempFile = new File(SongResUtils.createStandLyricTempFileName(songModel.getStandLrc()));

                boolean isSuccess = U.getHttpUtils().downloadFileSync(songModel.getStandLrc(), tempFile, null);

                File oldName = new File(SongResUtils.createStandLyricTempFileName(songModel.getStandLrc()));
                File newName = new File(SongResUtils.createStandLyricFileName(songModel.getStandLrc()));

                if (isSuccess) {
                    if (oldName != null && oldName.renameTo(newName)) {
                        MyLog.w(TAG, "已重命名");
                        emitter.onNext(newName);
                        emitter.onComplete();
                    } else {
                        MyLog.w(TAG, "Error");
                        emitter.onError(new IgnoreException("重命名错误"));
                    }
                } else {
                    emitter.onError(new IgnoreException("下载失败" + TAG));
                }
            }
        }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity) getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RxRetryAssist(5, 1, false))
                .subscribe(file -> {
                    final File fileName = SongResUtils.getGrabLyricFileByUrl(songModel.getStandLrc());
                    drawLyric(fileName);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    private void drawLyric(final File file) {
        MyLog.w(TAG, "file is " + file);
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                if (file != null && file.exists() && file.isFile()) {
                    try (BufferedSource source = Okio.buffer(Okio.source(file))) {
                        String lyric = source.readUtf8();
                        emitter.onNext(lyric);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                emitter.onComplete();
            }
        }).compose(((BaseActivity) getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) {
                mSongLyrics.setText("");
                if (isJSON2(o)) {
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

    public boolean isJSON2(String str) {
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
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

