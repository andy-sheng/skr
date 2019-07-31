package com.component.lyrics;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.common.core.crash.IgnoreException;
import com.common.core.global.ResServerApi;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.rxretrofit.ApiManager;
import com.common.utils.FileUtils;
import com.common.utils.U;
import com.component.lyrics.utils.LyricsUtils;
import com.component.lyrics.utils.SongResUtils;

import java.io.File;
import java.io.IOException;
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
import retrofit2.Call;
import retrofit2.Response;

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

    ResServerApi mResServerApi = ApiManager.getInstance().createService(ResServerApi.class);

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
                boolean isSuccess = false;
                isSuccess = U.getHttpUtils().downloadFileSync(url, newName, true, null);
                if (isSuccess) {
                    emitter.onNext(newName);
                } else {
                    MyLog.d(TAG, "使用服务器代理下载");
                    Call<JSONObject> call = mResServerApi.getLyricByUrl(url);
                    try {
                        Response<JSONObject> response = call.execute();
                        JSONObject jsonObject = response.body();
                        if (jsonObject != null) {
                            MyLog.d(TAG, "body=" + jsonObject.toString());
                            String content = jsonObject.getString("body");
                            U.getIOUtils().writeFile(content, newName);
                            emitter.onNext(newName);
                        } else {
                            emitter.onError(new IgnoreException("代理下载，歌词为空"));
                        }
                    } catch (IOException e) {
                        if (MyLog.isDebugLogOpen()) {
                            U.getToastUtil().showShort("歌词文件下载失败 url=" + url);
                        }
                        emitter.onError(new IgnoreException("代理下载失败"));
                        return;
                    }
                }
                emitter.onComplete();
            }

        }).map(new Function<File, LyricsReader>() {
            @Override
            public LyricsReader apply(File file) throws Exception {
                LyricsReader lyricsReader = new LyricsReader();
                try {
                    lyricsReader.loadLrc(file);
                    if (MyLog.isDebugLogOpen()) {
                        if (lyricsReader.getLrcLineInfos().isEmpty()) {
                            U.getToastUtil().showLong("时间戳歌词文件解析后内容为空 url=" + url);
                        }
                    }
                } catch (Exception e) {
                    Log.e("LyricsManager", "" + e.toString());
                }
                return lyricsReader;
            }
        })
                .subscribeOn(Schedulers.io())
                .retryWhen(new RxRetryAssist(5, ""))
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
                    boolean isSuccess = false;
                    isSuccess = U.getHttpUtils().downloadFileSync(url, file, true, null);
                    if (isSuccess) {
                        String content = U.getIOUtils().readFile(file);
                        if (!TextUtils.isEmpty(content)) {
                            emitter.onNext(content);
                        } else {
                            emitter.onNext("歌词buffer读取失败");
                        }
                    } else {
                        MyLog.d(TAG, "使用服务器代理下载");
                        Call<JSONObject> call = mResServerApi.getLyricByUrl(url);
                        try {
                            Response<JSONObject> response = call.execute();
                            JSONObject jsonObject = response.body();
                            if (jsonObject != null) {
                                MyLog.d(TAG, "body=" + jsonObject.toString());
                                String content = jsonObject.getString("body");
                                U.getIOUtils().writeFile(content, file);
                                if (!TextUtils.isEmpty(content)) {
                                    emitter.onNext(content);
                                }
                            } else {
                                emitter.onError(new IgnoreException("代理下载，歌词为空"));
                            }
                        } catch (IOException e) {
                            if (MyLog.isDebugLogOpen()) {
                                U.getToastUtil().showShort("歌词文件下载失败 url=" + url);
                            }
                            emitter.onError(new IgnoreException("代理下载失败"));
                            return;
                        }
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
                .retryWhen(new RxRetryAssist(5, ""))
                .observeOn(AndroidSchedulers.mainThread());
    }
}
