package com.module.playways.rank.prepare.presenter;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.utils.HttpUtils;
import com.common.utils.SongResUtils;
import com.module.playways.rank.song.model.SongModel;
import com.zq.lyrics.model.UrlRes;
import com.zq.lyrics.utils.ZipUrlResourceManager;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.LinkedList;

public class PrepareAuditionResPresenter extends RxLifeCyclePresenter {
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
            UrlRes lyric = new UrlRes(lyricUrl, SongResUtils.getLyricDir(), SongResUtils.SUFF_ZRCE);
            songResList.add(lyric);
        }

        //伴奏
        String accUrl = mSongModel.getAcc();
        if (!TextUtils.isEmpty(accUrl)) {
            UrlRes acc = new UrlRes(accUrl, SongResUtils.getACCDir(), SongResUtils.SUFF_ACC);
            songResList.add(acc);
        }

        //原唱
        String oriUrl = mSongModel.getOri();
        if (!TextUtils.isEmpty(oriUrl)) {
            UrlRes acc = new UrlRes(oriUrl, SongResUtils.getORIDir(), SongResUtils.SUFF_ORI);
            songResList.add(acc);
        }

        //评分文件
        String midiUrl = mSongModel.getMidi();
        if (!TextUtils.isEmpty(midiUrl)) {
            UrlRes midi = new UrlRes(midiUrl, SongResUtils.getMIDIDir(), SongResUtils.SUFF_MIDI);
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
