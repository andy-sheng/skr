package com.component.lyrics;

import android.content.Context;
import android.util.Log;

import com.common.core.crash.IgnoreException;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.U;
import com.component.lyrics.utils.LyricsUtils;
import com.component.lyrics.utils.SongResUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSource;
import okio.Okio;

public class LyricsManager {
    public static final String TAG = "LyricsManager";

    private static LyricsManager _LyricsManager;

    private LyricsManager(Context context) {
    }

    public static LyricsManager getLyricsManager(Context context) {
        if (_LyricsManager == null) {
            _LyricsManager = new LyricsManager(context);
        }
        return _LyricsManager;
    }

    /**
     * 加载标准歌词 url
     *
     * @param url
     * @return
     */
    public Observable<LyricsReader> loadStandardLyric(final String url) {
        return Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) throws Exception {
                File newName = new File(SongResUtils.createLyricFileName(url));
                if (newName.exists() && newName.isFile()) {
                    emitter.onNext(newName);
                    emitter.onComplete();
                    return;
                }
                boolean isSuccess = U.getHttpUtils().downloadFileSync(url, newName, true, null);
                if (isSuccess) {
                    emitter.onNext(newName);
                    emitter.onComplete();
                } else {
                    if (MyLog.isDebugLogOpen()) {
                        U.getToastUtil().showShort("歌词文件下载失败 url=" + url);
                    }
                    emitter.onError(new Throwable("fetchLyricTask"));
                }
            }
        }).map(new Function<File, LyricsReader>() {
            @Override
            public LyricsReader apply(File file) throws Exception {
                LyricsReader lyricsReader = new LyricsReader();
                try {
                    lyricsReader.loadLrc(file);
                    if (MyLog.isDebugLogOpen()) {
                        if (lyricsReader.getLrcLineInfos().isEmpty()) {
                            U.getToastUtil().showShort("歌词文件解析后内容为空 url=" + url);
                        }
                    }
                } catch (Exception e) {
                    Log.e("LyricsManager", "" + e.toString());
                }
                return lyricsReader;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 加载一唱到底普通文本歌词
     *
     * @param url
     * @return
     */
    public Observable<String> loadGrabPlainLyric(final String url) {
        MyLog.w(TAG, "loadGrabPlainLyric" + " url=" + url);
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                File file = SongResUtils.getGrabLyricFileByUrl(url);
                if (file == null || !file.exists()) {
                    boolean isSuccess = U.getHttpUtils().downloadFileSync(url, file, true, null);
                    if (isSuccess) {
                        BufferedSource source = null;
                        try {
                            source = Okio.buffer(Okio.source(file));
                            emitter.onNext(source.readUtf8());
                        } catch (Exception e) {
                            MyLog.e(TAG, e);
                            emitter.onNext("歌词buffer读取失败");
                        }
                    } else {
                        MyLog.w(TAG, "loadGrabPlainLyric 下载失败, url is " + url);
                        emitter.onNext("歌词下载失败");
                        emitter.onError(new IgnoreException("loadGrabPlainLyric"));
                        return;
                    }
                } else {
                    MyLog.w(TAG, "playLyric is exist");
                    BufferedSource source = null;
                    try {
                        source = Okio.buffer(Okio.source(file));
                        emitter.onNext(source.readUtf8());
                    } catch (Exception e) {
                        MyLog.e(TAG, e);
                        emitter.onNext("歌词buffer读取失败");
                    }
                }
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .retryWhen(new RxRetryAssist(3, ""))
                .observeOn(AndroidSchedulers.mainThread());
    }
}
