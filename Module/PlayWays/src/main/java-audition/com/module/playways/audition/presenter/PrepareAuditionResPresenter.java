package com.module.playways.audition.presenter;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.utils.HttpUtils;
import com.common.videocache.MediaCacheManager;
import com.component.lyrics.utils.SongResUtils;
import com.common.utils.U;
import com.module.playways.room.song.model.SongModel;
import com.component.lyrics.model.UrlRes;
import com.component.lyrics.utils.ZipUrlResourceManager;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.LinkedList;

public class PrepareAuditionResPresenter extends RxLifeCyclePresenter {
    public final String TAG = "PrepareAuditionResPresenter";
    HttpUtils.OnDownloadProgress mOnDownloadProgress;
    SongModel mSongModel;

    ZipUrlResourceManager mSongResourceZhang;

    public PrepareAuditionResPresenter(@NotNull HttpUtils.OnDownloadProgress onDownloadProgress, @NotNull SongModel songModel) {
        MyLog.d(TAG, "PrepareSongPresenter" + " mOnDownloadProgress=" + onDownloadProgress + " mSongModel=" + songModel);
        this.mOnDownloadProgress = onDownloadProgress;
        this.mSongModel = songModel;
    }

    public void prepareRes() {
        LinkedList<UrlRes> songResList = new LinkedList<>();
        String lyricUrl = mSongModel.getLyric();
        if (!TextUtils.isEmpty(lyricUrl)) {
            UrlRes lyric = new UrlRes(lyricUrl, SongResUtils.getLyricFileByUrl(lyricUrl));
            songResList.add(lyric);
        }

        //伴奏
        String accUrl = mSongModel.getAcc();
        if (!TextUtils.isEmpty(accUrl)) {
            // 有缓存时不需要下载
            String proxyUrl = MediaCacheManager.INSTANCE.getProxyUrl(accUrl, true);
            if (!proxyUrl.startsWith("file")) {
                UrlRes acc = new UrlRes(proxyUrl, null);
                songResList.add(acc);
            }
        }

        //原唱
//        String oriUrl = mSongModel.getOri();
//        if (!TextUtils.isEmpty(oriUrl)) {
//            UrlRes acc = new UrlRes(oriUrl, SongResUtils.getORIDir(), U.getFileUtils().getSuffixFromUrl(oriUrl,SongResUtils.SUFF_ORI));
//            songResList.add(acc);
//        }

        //评分文件
        String midiUrl = mSongModel.getMidi();
        if (!TextUtils.isEmpty(midiUrl)) {
            UrlRes midi = new UrlRes(midiUrl, SongResUtils.getMIDIFileByUrl(midiUrl));
            songResList.add(midi);
        }

        mSongResourceZhang = new ZipUrlResourceManager(songResList, mOnDownloadProgress);
        mSongResourceZhang.go();
    }

    @Override
    public void destroy() {
        super.destroy();
        cancelTask();
    }

    public void cancelTask(){
        mSongResourceZhang.cancelAllTask();
    }
}
