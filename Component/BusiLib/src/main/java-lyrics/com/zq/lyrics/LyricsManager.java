package com.zq.lyrics;

import android.content.Context;
import android.util.Log;

import com.common.log.MyLog;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.model.LyricsInfo;
import com.zq.lyrics.utils.LyricsIOUtils;
import com.zq.lyrics.utils.LyricsUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static okhttp3.internal.Util.closeQuietly;

public class LyricsManager {
    public static final String TAG = "LyricsManager";
    /**
     *
     */
    private static Context mContext;
    /**
     *
     */
    private static Map<String, LyricsReader> mLyricsUtils = new HashMap<String, LyricsReader>();

    private static LyricsManager _LyricsManager;

    public LyricsManager(Context context) {
        mContext = context;
    }

    public static LyricsManager getLyricsManager(Context context) {
        if (_LyricsManager == null) {
            _LyricsManager = new LyricsManager(context);
            copyLrcFileInfo();
        }
        return _LyricsManager;
    }

    private static void copyLrcFileInfo() {
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> emitter) {
                BufferedSource bufferedSource = null;
                BufferedSink sink = null;
                try {
                    String originalName = "shamoluotuo.zrce";

                    File dirFile = new File(ResourceConstants.PATH_LYRICS);
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }

                    File file = new File(ResourceConstants.PATH_LYRICS + File.separator + originalName);

                    if (!file.exists()) {
                        InputStream is = U.app().getAssets().open(originalName);
                        bufferedSource = Okio.buffer(Okio.source(is));
                        file.createNewFile();
                        sink = Okio.buffer(Okio.sink(file));
                        sink.writeAll(bufferedSource);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeQuietly(bufferedSource);
                    closeQuietly(sink);
                    emitter.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    public Observable<LyricsReader> loadLyricsObserable(final String fileName, final String hash) {
        return Observable.create(new ObservableOnSubscribe<LyricsReader>() {

            @Override
            public void subscribe(ObservableEmitter<LyricsReader> emitter) {
                MyLog.d(TAG, "loadLyricsUtil 1");
                LyricsReader lyricsReader = null;
                if (!mLyricsUtils.containsKey(hash)) {
                    MyLog.d(TAG, "loadLyricsUtil 2");
                    File lrcFile = LyricsUtils.getLrcFile(fileName, SongResUtils.getLyricDir());
                    MyLog.d(TAG, "loadLyricsUtil 3 " + lrcFile);
                    if (lrcFile != null) {
                        lyricsReader = new LyricsReader();
                        try {
                            lyricsReader.loadLrc(lrcFile);
                            mLyricsUtils.put(hash, lyricsReader);
                        } catch (Exception e) {
                            Log.e("LyricsManager", "" + e.toString());
                            emitter.onError(e);
                            return;
                        }
                    }
                } else {
                    lyricsReader = mLyricsUtils.get(hash);
                }

                emitter.onNext(lyricsReader);
                emitter.onComplete();
            }
        });
    }

    public Observable<File> fetchLyricTask(final String url) {
        MyLog.d(TAG, "fetchLyricTask" + " url =" + url);
        return Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File tempFile = new File(SongResUtils.createTempLyricFileName(url));

                boolean isSuccess = U.getHttpUtils().downloadFileSync(url, tempFile, null);

                File oldName = new File(SongResUtils.createTempLyricFileName(url));
                File newName = new File(SongResUtils.createLyricFileName(url));
//                if (true) {
//                    MyLog.d(TAG, "fetchLyricTask 哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦");
//                    for (int i = 0; i < 308613; i++) {
//                        MyLog.d(TAG, "i=" + i);
//                    }
//                    emitter.onError(new Throwable("下载失败"));
//                    return;
//                }
                if (isSuccess) {
                    if (oldName != null && oldName.renameTo(newName)) {
                        System.out.println("已重命名");
                        emitter.onNext(newName);
                        emitter.onComplete();
                    } else {
                        System.out.println("Error");
                        emitter.onError(new Throwable("重命名错误"));
                    }
                } else {
                    emitter.onError(new Throwable("下载失败"));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param fileName
     * @param keyword
     * @param hash
     * @return
     */
    public void loadLyricsUtil(final String fileName, final String keyword, final String hash) {
        loadLyricsObserable(fileName, hash).subscribeOn(Schedulers.io())
                .retry(10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LyricsReader>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(LyricsReader lyricsReader) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, e);
                    }

                    @Override
                    public void onComplete() {
                        EventBus.getDefault().post(new LrcEvent.FinishLoadLrcEvent(hash));
                    }
                });
    }

    public LyricsReader getLyricsUtil(String hash) {
        return mLyricsUtils.get(hash);
    }


    /**
     * 保存歌词文件
     *
     * @param lrcFilePath lrc歌词路径
     * @param lyricsInfo  lrc歌词数据
     */
    private void saveLrcFile(final String lrcFilePath, final LyricsInfo lyricsInfo) {
        new Thread() {

            @Override
            public void run() {

                //保存修改的歌词文件
                try {
                    LyricsIOUtils.getLyricsFileWriter(lrcFilePath).writer(lyricsInfo, lrcFilePath);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        }.start();
    }
}
