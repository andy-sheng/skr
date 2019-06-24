package com.module.playways.grab.room.view.normal.view;

import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;

import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.U;
import com.common.view.ExViewStub;
import com.common.view.ex.ExTextView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.room.song.model.SongModel;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.model.LyricsLineInfo;

import java.util.Iterator;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 你的主场景歌词
 */
public class DoubleNormalSelfSingCardView extends ExViewStub {
    public final static String TAG = "DoubleNormalSelfSingCardView";

    SongModel mSongModel;

    ExTextView mLyricTv;

    public DoubleNormalSelfSingCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
    }

    @Override
    protected void init(View parentView) {
        {
            mLyricTv = parentView.findViewById(R.id.lyric_tv);
        }
        mParentView.setClickable(true);
        {
            ConstraintLayout.LayoutParams topLayoutParams = (ConstraintLayout.LayoutParams) parentView.getLayoutParams();
            topLayoutParams.topMargin = U.getDisplayUtils().dip2px(55f);
        }
    }

    @Override
    protected int layoutDesc() {
        return R.layout.double_normal_lyric_layout;
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        super.onViewAttachedToWindow(v);
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
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
            LyricsManager.getLyricsManager(U.app())
                    .fetchAndLoadLyrics(mSongModel.getLyric())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .retryWhen(new RxRetryAssist(5, ""))
                    .subscribe(new Consumer<LyricsReader>() {
                        @Override
                        public void accept(LyricsReader lyricsReader) throws Exception {
                            mLyricTv.setText("");
                            Iterator<LyricsLineInfo> iter = lyricsReader.getLrcLineInfos().values().iterator();
                            while (iter.hasNext()) {
                                LyricsLineInfo lyricsLineInfo = iter.next();
                                mLyricTv.append(lyricsLineInfo.getLineLyrics() + "\n");
                            }
                        }
                    });
        } else if (!TextUtils.isEmpty(mSongModel.getStandLrc())) {
            LyricsManager.getLyricsManager(U.app())
                    .loadGrabPlainLyric(mSongModel.getStandLrc())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            mLyricTv.setText(s);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            MyLog.e(TAG, "accept" + " throwable=" + throwable);
                        }
                    });
        } else {
            MyLog.e(TAG, "没有歌词呀，mSongModel is " + mSongModel);
        }
    }

    public void destroy() {

    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }

}
