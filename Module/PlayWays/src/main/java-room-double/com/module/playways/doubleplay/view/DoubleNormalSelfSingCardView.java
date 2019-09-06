package com.module.playways.doubleplay.view;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ScrollView;

import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.ExViewStub;
import com.common.view.ex.ExTextView;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.playways.R;
import com.module.playways.doubleplay.loadsir.LyricLoadErrorCallBack;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.room.song.model.SongModel;
import com.component.lyrics.LyricsManager;
import com.component.lyrics.model.LyricsLineInfo;

import java.util.Iterator;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 你的主场景歌词
 */
public class DoubleNormalSelfSingCardView extends ExViewStub {
    public final String TAG = "DoubleNormalSelfSingCardView";

    SongModel mSongModel;

    ExTextView mLyricTv;

    LoadService mLoadService;

    ScrollView mScrollView;

    public DoubleNormalSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
    }

    @Override
    protected void init(View parentView) {
        {
            mLyricTv = parentView.findViewById(R.id.lyric_tv);
            mScrollView = parentView.findViewById(R.id.scrollView);
        }
        getMParentView().setClickable(true);

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LyricLoadErrorCallBack())
                .build();
        mLoadService = mLoadSir.register(mScrollView, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                playLyric();
            }
        });
    }

    @Override
    protected int layoutDesc() {
        return R.layout.double_normal_lyric_stub_layout;
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        super.onViewAttachedToWindow(v);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
    }

    public void updateLockState() {

    }

    public void playLyric(SongModel songModel) {
        mSongModel = songModel;
        playLyric();
    }

    public void playLyric() {
        if (mSongModel == null) {
            MyLog.d(TAG, "mSongModel 是空的");
            return;
        }

        MyLog.d(TAG, "mSongModel is " + mSongModel);

        tryInflate();
        setVisibility(View.VISIBLE);

        mLyricTv.setText("歌词加载中...");

        if (!TextUtils.isEmpty(mSongModel.getLyric())) {
            LyricsManager.INSTANCE
                    .loadStandardLyric(mSongModel.getLyric())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .retryWhen(new RxRetryAssist(5, ""))
                    .subscribe(lyricsReader -> {
                        mLyricTv.setText("");
                        Iterator<LyricsLineInfo> iter = lyricsReader.getLrcLineInfos().values().iterator();
                        while (iter.hasNext()) {
                            LyricsLineInfo lyricsLineInfo = iter.next();
                            mLyricTv.append(lyricsLineInfo.getLineLyrics() + "\n");
                        }
                        mLoadService.showSuccess();
                    }, throwable -> {
                        mLoadService.showCallback(LyricLoadErrorCallBack.class);
                        MyLog.e(TAG, "accept 1" + " throwable=" + throwable);
                    });
        } else if (!TextUtils.isEmpty(mSongModel.getStandLrc())) {
            LyricsManager.INSTANCE
                    .loadGrabPlainLyric(mSongModel.getStandLrc())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            SpannableStringBuilder ssb = createLyricSpan(s, mSongModel);
                            if (ssb == null) {
                                mLyricTv.setText(s);
                            } else {
                                mLyricTv.setText(ssb);
                            }
                            mLoadService.showSuccess();
                        }
                    }, throwable -> {
                        mLoadService.showCallback(LyricLoadErrorCallBack.class);
                        MyLog.e(TAG, "accept 2" + " throwable=" + throwable);
                    });
        } else {
            MyLog.e(TAG, "没有歌词呀，mSongModel is " + mSongModel);
        }
    }

    protected SpannableStringBuilder createLyricSpan(String lyric, SongModel songModel) {
        if (songModel != null) {
            SpannableStringBuilder ssb = new SpanUtils()
                    .append(lyric)
                    .append("\n")
                    .append("上传者:" + songModel.getUploaderName()).setFontSize(12, true)
                    .create();
            return ssb;
        }
        return null;
    }

    public void destroy() {

    }
}
