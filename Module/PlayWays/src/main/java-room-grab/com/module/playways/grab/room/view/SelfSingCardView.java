package com.module.playways.grab.room.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.common.log.MyLog;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.model.LyricsLineInfo;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 你的主场景歌词
 */
public class SelfSingCardView extends RelativeLayout {
    public final static String TAG = "SelfSingCardView";
    SimpleDraweeView mSdvIcon;
    ScrollView mSvLyric;
    TextView mTvLyric;

    int mSpeed = 0;

    SongModel mSongModel;

    /**
     * 歌词在Y轴上的偏移量
     */
    private int mOffsetY = 0;

    Disposable mDisposable;

    Handler mHandler = new Handler();

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
        mSdvIcon = findViewById(R.id.sdv_icon);
        mSvLyric = findViewById(R.id.sv_lyric);
        mTvLyric = findViewById(R.id.tv_lyric);

        mSvLyric.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        resetOffsetY();
                        startScroll();
                        break;
                    default:
                        stopScroll();
                        break;
                }
                return false;
            }
        });
    }

    public void playLyric(SongModel songModel, boolean play) {
        MyLog.w(TAG, "开始播放歌词 songId=" + songModel.getItemID());
        if (songModel == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }
        mSongModel = songModel;

        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

        File file = SongResUtils.getZRCELyricFileByUrl(songModel.getLyric());

        if (file == null) {
            MyLog.w(TAG, "playLyric is not in local file");
            fetchLyricTask(songModel, play);
        } else {
            MyLog.w(TAG, "playLyric is exist");
            final String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
            parseLyrics(fileName, play);
        }
    }


    private void parseLyrics(String fileName, boolean play) {
        MyLog.w(TAG, "parseLyrics" + " fileName=" + fileName);
        mDisposable = LyricsManager.getLyricsManager(U.app()).loadLyricsObserable(fileName, fileName.hashCode() + "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(10)
//                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(lyricsReader -> {
                    drawLyric(fileName.hashCode() + "", lyricsReader, play);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    private void fetchLyricTask(SongModel songModel, boolean play) {
        MyLog.w(TAG, "fetchLyricTask" + " songModel=" + songModel);
        mDisposable = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File tempFile = new File(SongResUtils.createTempLyricFileName(songModel.getLyric()));

                boolean isSuccess = U.getHttpUtils().downloadFileSync(songModel.getLyric(), tempFile, null);

                File oldName = new File(SongResUtils.createTempLyricFileName(songModel.getLyric()));
                File newName = new File(SongResUtils.createLyricFileName(songModel.getLyric()));

                if (isSuccess) {
                    if (oldName.renameTo(newName)) {
                        MyLog.w(TAG, "已重命名");
                    } else {
                        MyLog.w(TAG, "Error");
                        emitter.onError(new Throwable("重命名错误"));
                    }
                } else {
                    emitter.onError(new Throwable("下载失败"));
                }

                emitter.onNext(newName);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(1000)
//                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(file -> {
                    final String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
                    parseLyrics(fileName, play);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    private void drawLyric(String fileNameHash, LyricsReader lyricsReader, boolean play) {
        if (lyricsReader != null) {
            lyricsReader.setHash(fileNameHash);
            TreeMap<Integer, LyricsLineInfo> mLrcLineInfos = lyricsReader.getLrcLineInfos();
            Iterator<Map.Entry<Integer, LyricsLineInfo>> it = mLrcLineInfos.entrySet().iterator();
            while (it.hasNext()) {
                mTvLyric.append(getLyricFromLyricsLineInfo(it.next().getValue()));
            }
        }

        startScroll();
    }

    private String getLyricFromLyricsLineInfo(LyricsLineInfo info) {
        String lyric = "";
        for (int i = 0; i < info.getSplitLyricsLineInfos().size(); i++) {
            lyric = lyric + info.getSplitLyricsLineInfos().get(i) + "\n";
        }

        return lyric;
    }

    private void resetOffsetY(){
//        int[] location = new int[2];
//        mTvLyric.getLocationInWindow(location);
//        mOffsetY = location[1];
    }

    private void stopScroll() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void startScroll() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSpeed == 0) {
                    int textHeight = mTvLyric.getHeight();
                    mSpeed = textHeight / mSongModel.getTotalMs();
                    resetOffsetY();
                }

                if (mSpeed != 0) {
                    mOffsetY += mSpeed * 30;
                    mSvLyric.scrollTo(0, mOffsetY);
                }

                startScroll();
            }
        }, 30);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }
}
