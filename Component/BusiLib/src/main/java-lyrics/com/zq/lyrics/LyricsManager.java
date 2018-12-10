package com.zq.lyrics;

import android.content.Context;
import android.util.Log;

import com.common.log.MyLog;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
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

    private static void copyLrcFileInfo(){
        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> emitter) {
                BufferedSource bufferedSource = null;
                BufferedSink sink = null;
                try {
                    String originalName = "shamoluotuo.zrce";

                    File dirFile = new File(ResourceConstants.PATH_LYRICS);
                    if(!dirFile.exists()){
                        dirFile.mkdirs();
                    }

                    File file = new File(ResourceConstants.PATH_LYRICS + File.separator + originalName);

                    if(!file.exists()){
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

    /**
     * @param fileName
     * @param keyword
     * @param duration
     * @param hash
     * @return
     */
    public void loadLyricsUtil(final String fileName, final String keyword, final String duration, final String hash) {

        Observable.create(new ObservableOnSubscribe<Object>() {

            @Override
            public void subscribe(ObservableEmitter<Object> emitter) {
                MyLog.d(TAG, "loadLyricsUtil 1");
                if (!mLyricsUtils.containsKey(hash)) {
                    MyLog.d(TAG, "loadLyricsUtil 2");
                    File lrcFile = LyricsUtils.getLrcFile(fileName, ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, null));
                    MyLog.d(TAG, "loadLyricsUtil 3 " + lrcFile);
                    if (lrcFile != null) {
                        LyricsReader lyricsUtil = new LyricsReader();
                        try {

                            lyricsUtil.loadLrc(lrcFile);
                        } catch (Exception e) {
                            Log.e("LyricsManager", "" + e.toString());
                        }
                        mLyricsUtils.put(hash, lyricsUtil);
                    }
                }

                EventBus.getDefault().post(new LrcEvent.FinishLoadLrcEvent(hash));
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();


        //1.从缓存中获取
        //2.从本地文件中获取
        //3.从网络中获取
//        new AsyncTaskUtil() {
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//
//                AudioMessage audioMessage = new AudioMessage();
//                audioMessage.setHash(hash);
//                //发送加载完成广播
//                Intent loadedIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCLOADED);
//                loadedIntent.putExtra(AudioMessage.KEY, audioMessage);
//                loadedIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                mContext.sendBroadcast(loadedIntent);
//            }
//
//            @Override
//            protected Void doInBackground(String... strings) {
//
//                AudioMessage audioMessage = new AudioMessage();
//                audioMessage.setHash(hash);
//                //发送搜索中广播
//                Intent searchingIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCSEARCHING);
//                searchingIntent.putExtra(AudioMessage.KEY, audioMessage);
//                searchingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                mContext.sendBroadcast(searchingIntent);
//
//                if (mLyricsUtils.containsKey(hash)) {
//                    return null;
//                }
//                //
//                File lrcFile = LyricsUtils.getLrcFile(fileName, ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, null));
//                if (lrcFile != null) {
//                    LyricsReader lyricsUtil = new LyricsReader();
//                    try {
//
//                        lyricsUtil.loadLrc(lrcFile);
//                    } catch (Exception e) {
//                        Log.e("LyricsManager", "" + e.toString());
//                    }
//                    mLyricsUtils.put(hash, lyricsUtil);
//                } else {
//
//                    //发送下载中广播
//                    Intent downloadingIntent = new Intent(AudioBroadcastReceiver.ACTION_LRCDOWNLOADING);
//                    downloadingIntent.putExtra(AudioMessage.KEY, audioMessage);
//                    downloadingIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                    mContext.sendBroadcast(downloadingIntent);
//
//                    //下载歌词
//                    File saveLrcFile = new File(ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_LYRICS, fileName + ".krc"));
//                    byte[] base64ByteArray = DownloadLyricsUtil.downloadLyric(mHPApplication, mContext, keyword, duration, hash);
//                    if (base64ByteArray != null && base64ByteArray.length > 1024) {
//                        LyricsReader lyricsUtil = new LyricsReader();
//
//                        try {
//                            lyricsUtil.loadLrc(base64ByteArray, saveLrcFile, saveLrcFile.getName());
//                        } catch (Exception e) {
//                            Log.e("LyricsManager", "" + e.toString());
//                        }
//                        mLyricsUtils.put(hash, lyricsUtil);
//                    } else {
//                        LyricsReader lyricsUtil = new LyricsReader();
//                        mLyricsUtils.put(hash, lyricsUtil);
//                    }
//                }
//
//                return super.doInBackground(strings);
//            }
//        }.execute("");
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
