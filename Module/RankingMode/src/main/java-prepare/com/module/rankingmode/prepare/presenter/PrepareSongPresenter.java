package com.module.rankingmode.prepare.presenter;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.utils.HttpUtils;
import com.common.utils.U;
import com.module.rankingmode.song.model.SongModel;
import com.zq.lyrics.model.UrlRes;
import com.zq.lyrics.utils.ZipUrlResourceManager;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.LinkedList;

public class PrepareSongPresenter extends RxLifeCyclePresenter {
    HttpUtils.OnDownloadProgress onDownloadProgress;
    SongModel songModel;

    ZipUrlResourceManager songResourceZhang;

    public PrepareSongPresenter(@NotNull HttpUtils.OnDownloadProgress onDownloadProgress, @NotNull SongModel songModel) {
        MyLog.d(TAG, "PrepareSongPresenter" + " onDownloadProgress=" + onDownloadProgress + " songModel=" + songModel);
        this.onDownloadProgress = onDownloadProgress;
        this.songModel = songModel;
    }

    public void prepareRes() {
        LinkedList<UrlRes> songResList = new LinkedList<>();
        String lyricUrl = songModel.getLyric();
        if (!TextUtils.isEmpty(lyricUrl)) {
            UrlRes lyric = new UrlRes(lyricUrl, U.getAppInfoUtils().getMainDir().getAbsolutePath() + File.separator + "lyrics", "zrce");
            songResList.add(lyric);
        }

        //伴奏
        String accUrl = songModel.getAcc();
        if (!TextUtils.isEmpty(accUrl)) {
            UrlRes acc = new UrlRes(accUrl, U.getAppInfoUtils().getMainDir().getAbsolutePath() + File.separator + "acc", "mp3");
            songResList.add(acc);
        }

        //原唱
        String oriUrl = songModel.getOri();
        if (!TextUtils.isEmpty(oriUrl)) {
            UrlRes acc = new UrlRes(oriUrl, U.getAppInfoUtils().getMainDir().getAbsolutePath() + File.separator + "ori", "mp3");
            songResList.add(acc);
        }

        //评分文件
        String midiUrl = songModel.getMidi();
        if (!TextUtils.isEmpty(midiUrl)) {
            UrlRes midi = new UrlRes(midiUrl, U.getAppInfoUtils().getMainDir().getAbsolutePath() + File.separator + "midi", "mid");
            songResList.add(midi);
        }

        songResourceZhang = new ZipUrlResourceManager(songResList, onDownloadProgress);
        songResourceZhang.go();
    }

    public void cancelTask(){
        songResourceZhang.cancelAllTask();
    }
}
