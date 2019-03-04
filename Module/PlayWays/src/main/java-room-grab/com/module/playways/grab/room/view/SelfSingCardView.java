package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.HttpImage;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.lyrics.model.LyricsLineInfo;

import org.greenrobot.greendao.annotation.NotNull;

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
 * 你的主场景歌词 已弃用
 */
public class SelfSingCardView extends RelativeLayout {
    public final static String TAG = "SelfSingCardView";

    final static int MSG_ENSURE_PLAY = 1;
    final static int COUNT_DOWN_STATUS_WAIT = 1;
    final static int COUNT_DOWN_STATUS_PLAYING = 2;

    SVGAImageView mSingBgSvga;

    SimpleDraweeView mSdvIcon;

    ScrollView mSvLyric;
    TextView mTvLyric;

    ImageView mIvHStub;
    ImageView mIvTStub;
    ImageView mIvOStub;
    ImageView mIvS;
    ImageView mIvEmpty;

    float mSpeed = 0;

    SongModel mSongModel;

    Listener mListener;
    HandlerTaskTimer mCountDownTask;

    /**
     * 歌词在Y轴上的偏移量
     */
    private float mOffsetY = 0;

    /**
     * 歌词在Y轴上的偏移量
     */
    private Integer initialY = null;

    Disposable mDisposable;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_ENSURE_PLAY) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
                countDown();
            }
        }
    };

    TranslateAnimation mEnterAnimation;   // 进场动画
    TranslateAnimation mLeaveAnimation;   // 出场动画

    int mCountDownStatus = COUNT_DOWN_STATUS_WAIT;

    public SelfSingCardView(Context context) {
        super(context);
        init();
    }

    public SelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_self_sing_card_layout, this);

        mSingBgSvga = (SVGAImageView) findViewById(R.id.sing_bg_svga);
        mSvLyric = findViewById(R.id.sv_lyric);
        mTvLyric = findViewById(R.id.tv_lyric);
        mIvEmpty = (ImageView) findViewById(R.id.iv_empty);


        mIvHStub = (ImageView) findViewById(R.id.iv_h_stub);
        mIvTStub = (ImageView) findViewById(R.id.iv_t_stub);
        mIvOStub = (ImageView) findViewById(R.id.iv_o_stub);
        mIvS = (ImageView) findViewById(R.id.iv_s);


        mSvLyric.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:

                        mUiHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                resetOffsetY();
                                startScroll();
                            }
                        }, 3000);
                        break;
                    default:
                        stopScroll();
                        break;
                }
                return false;
            }
        });

        HandlerTaskTimer.newBuilder().delay(100).start(new HandlerTaskTimer.ObserverW() {
            @Override
            public void onNext(Integer integer) {
                int[] location = new int[2];
                mTvLyric.getLocationInWindow(location);
                //初始的Y轴位置
                if (initialY == null) {
                    initialY = location[1];
                    MyLog.d(TAG, "initialY " + initialY);
                }
            }
        });
    }


    public void tryStartCountDown() {
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            countDown();
        }
    }

    private void countDown() {
        if (mSongModel == null) {
            return;
        }
        mUiHandler.removeMessages(MSG_ENSURE_PLAY);
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            // 不需要播放countdown
            int num = (mSongModel.getStandLrcEndT() - mSongModel.getStandLrcBeginT()) / 1000;
            setNum(num);
            mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 3000);
            return;
        }
        cancelCountDownTask();
        mCountDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(((mSongModel.getStandLrcEndT() - mSongModel.getStandLrcBeginT()) / 1000 + 1))
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int num = ((mSongModel.getStandLrcEndT() - mSongModel.getStandLrcBeginT()) / 1000) - integer;
                        setNum(num);
                        if (num == 0) {
                            if (mListener != null) {
                                mListener.onCountDownOver();
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                    }
                });
    }

    private void cancelCountDownTask() {
        if (mCountDownTask != null) {
            mCountDownTask.dispose();
        }
    }

    private void setNum(int num) {
        mIvOStub.setImageDrawable(null);
        mIvTStub.setImageDrawable(null);
        mIvHStub.setImageDrawable(null);

        String s = String.valueOf(num);
        int[] index_num = new int[s.length()];

        for (int i = 0; i < s.length(); i++) {
            index_num[i] = Integer.parseInt(getNum(num, i + 1));

//            if (i == 0) {
//                mIvOStub.setImageDrawable(getNumDrawable(index_num[0]));
//            } else if (i == 1) {
//                mIvTStub.setImageDrawable(getNumDrawable(index_num[1]));
//            } else if (i == 2) {
//                mIvHStub.setImageDrawable(getNumDrawable(index_num[2]));
//            }
        }

        mIvS.setImageDrawable(U.getDrawable(R.drawable.daojishizi_s));
    }

    public String getNum(long num, int index) {
        MyLog.d(TAG, "getNum" + " num=" + num + " index=" + index);
        if (num < 0) {
            return "0";
        }
        String s = String.valueOf(num);
        if (index > s.length() || index < 0) {
            return "";
        }

        String result = String.valueOf(s.charAt(s.length() - index));

        return result;
    }

    public void showBackground(String avatar) {
        setVisibility(VISIBLE);
        mSingBgSvga.setVisibility(VISIBLE);
        mSingBgSvga.setLoops(0);
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse("grab_self_sing_bg.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicItem(avatar));
                    mSingBgSvga.setImageDrawable(drawable);
                    mSingBgSvga.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }
    }

    private SVGADynamicEntity requestDynamicItem(String avatar) {
        if (TextUtils.isEmpty(avatar)) {
            return null;
        }
        HttpImage image = AvatarUtils.getAvatarUrl(AvatarUtils.newParamsBuilder(avatar)
                .setWidth(U.getDisplayUtils().dip2px(90))
                .setHeight(U.getDisplayUtils().dip2px(90))
                .build());
        File file = FrescoWorker.getCacheFileFromFrescoDiskCache(image.getUrl());

        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (file != null && file.exists()) {
            dynamicEntity.setDynamicImage(BitmapFactory.decodeFile(file.getPath()), "avatar");
        } else {
            dynamicEntity.setDynamicImage(image.getUrl(), "avatar");
        }
        return dynamicEntity;
    }

    public void playLyric(SongModel songModel, boolean play) {
        if (songModel == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }
        MyLog.w(TAG, "开始播放歌词 songId=" + songModel.getItemID());
        if (play) {
            if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING;
            }
        } else {
            mCountDownStatus = COUNT_DOWN_STATUS_WAIT;
        }
        mSongModel = songModel;
        mIvEmpty.setVisibility(GONE);
        // 平移动画
        if (mEnterAnimation == null) {
            mEnterAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0F, 0F, 0F);
            mEnterAnimation.setDuration(200);
        }
        this.startAnimation(mEnterAnimation);
        mTvLyric.setText("");
        mUiHandler.removeCallbacksAndMessages(null);
        showBackground(MyUserInfoManager.getInstance().getAvatar());

        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

        if (mSongModel.isIsblank()) {
            mIvEmpty.setVisibility(VISIBLE);
            countDown();
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
                        emitter.onError(new Throwable("重命名错误"));
                    }
                } else {
                    emitter.onError(new Throwable("下载失败"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RxRetryAssist(5, 1, false))
//                .compose(bindUntilEvent(FragmentEvent.DESTROY))
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
                        emitter.onNext(source.readUtf8());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                emitter.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) {
                mTvLyric.append(o);
            }
        }, throwable -> MyLog.e(TAG, throwable));

        startScroll();
        countDown();
    }

    private String getLyricFromLyricsLineInfo(LyricsLineInfo info) {
        String lyric = "";
        if (info.getSplitLyricsLineInfos() != null && info.getSplitLyricsLineInfos().size() > 0) {
            for (int i = 0; i < info.getSplitLyricsLineInfos().size(); i++) {
                lyric = lyric + info.getSplitLyricsLineInfos().get(i) + "\n";
            }
        } else {
            lyric = lyric + info.getLineLyrics() + "\n";
        }

        return lyric;
    }

    private void resetOffsetY() {
        int[] location = new int[2];
        mTvLyric.getLocationInWindow(location);
        if (initialY == null) {
            initialY = 0;
        }
        mOffsetY = initialY - location[1];
        MyLog.d(TAG, "location[1]:" + location[1] + ", mOffsetY:" + mOffsetY + ",initialY: " + initialY);
    }

    private void stopScroll() {
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void startScroll() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                int[] location = new int[2];
//                mTvLyric.getLocationInWindow(location);
////                Log.d(TAG, "mTextView.X" + location[0] + " mTextView.Y" + location[1]);
//                if(mSpeed == 0){
//                    int textHeight = mTvLyric.getHeight();
//                    mSpeed = (float)textHeight * (float) UPTATE_TIME / (float) mSongModel.getTotalMs();
//                }
//
//                if (mSpeed != 0) {
//                    mOffsetY += mSpeed;
//                    mSvLyric.scrollTo(0, (int) mOffsetY);
//                }
//
//                startScroll();
//            }
//        }, UPTATE_TIME);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUiHandler.removeCallbacksAndMessages(null);
        if (mSingBgSvga != null) {
            mSingBgSvga.setCallback(null);
            mSingBgSvga.stopAnimation(true);
        }
        if (mLeaveAnimation != null) {
            mLeaveAnimation.cancel();
        }
        cancelCountDownTask();
    }

    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveAnimation == null) {
                mLeaveAnimation = new TranslateAnimation(0F, U.getDisplayUtils().getScreenWidth(), 0F, 0F);
                mLeaveAnimation.setDuration(200);
            }
            this.startAnimation(mLeaveAnimation);
            mLeaveAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mSingBgSvga != null) {
                        mSingBgSvga.stopAnimation(false);
                    }
                    setVisibility(GONE);
                    clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            if (mSingBgSvga != null) {
                mSingBgSvga.stopAnimation(false);
            }
            setVisibility(GONE);
            clearAnimation();
        }
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void onCountDownOver();
    }
}
