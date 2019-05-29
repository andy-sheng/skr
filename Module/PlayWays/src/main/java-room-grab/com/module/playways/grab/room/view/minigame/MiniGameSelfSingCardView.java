package com.module.playways.grab.room.view.minigame;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.crash.IgnoreException;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.NewChorusLyricModel;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.normal.view.SingCountDownView;
import com.module.playways.room.song.model.MiniGameInfoModel;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.zq.live.proto.Common.EMiniGamePlayType;
import com.zq.lyrics.utils.SongResUtils;

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
 * 小游戏自己视角的卡片
 */
public class MiniGameSelfSingCardView extends RelativeLayout {

    public final static String TAG = "MiniGameSelfSingCardView";

    GrabRoomData mGrabRoomData;
    MiniGameInfoModel mMiniGameInfoModel;
    String mMiniGameSongUrl;
    SelfSingCardView.Listener mListener;

    ImageView mIvBg;
    ScrollView mSvLyric;
    TextView mTvLyric;    //用来显示游戏内容
    SingCountDownView mSingCountDownView;

    Disposable mDisposable;

    public MiniGameSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public MiniGameSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniGameSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_mini_game_selft_sing_layout, this);

        mIvBg = findViewById(R.id.iv_bg);
        mSvLyric = findViewById(R.id.sv_lyric);
        mTvLyric = findViewById(R.id.tv_lyric);
        mSingCountDownView = findViewById(R.id.sing_count_down_view);

    }

    public void setRoomData(GrabRoomData roomData) {
        mGrabRoomData = roomData;
    }

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
        if (mSingCountDownView != null) {
            mSingCountDownView.setListener(mListener);
        }
    }

    public void playLyric() {
        GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.w(TAG, "infoModel 是空的");
            return;
        }

        if (infoModel.getMusic() == null) {
            MyLog.w(TAG, "songModel 是空的");
            return;
        }
        mMiniGameInfoModel = infoModel.getMusic().getMiniGame();
        if (mMiniGameInfoModel == null) {
            MyLog.w(TAG, "MiniGame 是空的");
            return;
        }

        int totalTs = infoModel.getSingTotalMs();
        mSingCountDownView.setTagTvText(mMiniGameInfoModel.getGameName());
        mSingCountDownView.startPlay(0, totalTs, true);

        if (mMiniGameInfoModel.getGamePlayType() == EMiniGamePlayType.EMGP_SONG_DETAIL.getValue()) {
            // TODO: 2019-05-29 带歌词的
            mMiniGameSongUrl = mMiniGameInfoModel.getSongInfo().getSongURL();
            File file = SongResUtils.getGrabLyricFileByUrl(mMiniGameSongUrl);

            if (file == null || !file.exists()) {
                MyLog.w(TAG, "playLyric is not in local file");
                fetchLyricTask();
            } else {
                MyLog.w(TAG, "playLyric is exist");
                final File fileName = SongResUtils.getGrabLyricFileByUrl(mMiniGameSongUrl);
                drawLyric(fileName);
            }
        } else {
            // TODO: 2019-05-29 不带歌词的,待补充
            mTvLyric.setText(mMiniGameInfoModel.getDisplayGameRule());
        }

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
                mTvLyric.setText("");
                if (isJSON2(o)) {
                    NewChorusLyricModel newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel.class);
                    mTvLyric.append(mMiniGameInfoModel.getDisplayGameRule());
                    for (int i = 0; i < newChorusLyricModel.getItems().size() && i < 2; i++) {
                        mTvLyric.append(newChorusLyricModel.getItems().get(i).getWords());
                        if (i == 0) {
                            mTvLyric.append("\n");
                        }
                    }
                } else {
                    mTvLyric.append(mMiniGameInfoModel.getDisplayGameRule());
                    mTvLyric.setText(o);
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

    private void fetchLyricTask() {
        if(TextUtils.isEmpty(mMiniGameSongUrl)){
            MyLog.w(TAG, "fetchLyricTask mMiniGameSongUrl = null" );
            return;
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        mDisposable = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File newName = new File(SongResUtils.createStandLyricFileName(mMiniGameSongUrl));
                boolean isSuccess = U.getHttpUtils().downloadFileSync(mMiniGameSongUrl, newName, true, null);

                if (isSuccess) {
                    emitter.onNext(newName);
                    emitter.onComplete();
                } else {
                    emitter.onError(new IgnoreException("下载失败" + TAG));
                }
            }
        }).subscribeOn(Schedulers.io())
                .compose(((BaseActivity) getContext()).bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RxRetryAssist(5, 1, false))
                .subscribe(file -> {
                    final File fileName = SongResUtils.getGrabLyricFileByUrl(mMiniGameSongUrl);
                    drawLyric(fileName);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mSingCountDownView.reset();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public void destroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }
}
