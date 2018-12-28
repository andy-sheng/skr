package com.module.playways.rank.prepare.presenter;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.mvp.PresenterEvent;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.utils.HttpUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.module.playways.rank.prepare.view.IPrepareResView;
import com.module.playways.rank.song.model.SongModel;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.model.LyricsLineInfo;
import com.zq.lyrics.model.UrlRes;
import com.zq.lyrics.utils.ZipUrlResourceManager;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.LinkedList;
import java.util.TreeMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PrepareSongPresenter extends RxLifeCyclePresenter {
    HttpUtils.OnDownloadProgress mOnDownloadProgress;
    SongModel mSongModel;

    ZipUrlResourceManager mSongResourceZhang;

    IPrepareResView mIPrepareResView;

    public PrepareSongPresenter(@NotNull HttpUtils.OnDownloadProgress onDownloadProgress, IPrepareResView iPrepareResView, @NotNull SongModel songModel) {
        MyLog.d(TAG, "PrepareSongPresenter" + " mOnDownloadProgress=" + onDownloadProgress + " mSongModel=" + songModel);
        this.mOnDownloadProgress = onDownloadProgress;
        this.mSongModel = songModel;
        this.mIPrepareResView = iPrepareResView;
    }

    public void prepareRes() {
        LinkedList<UrlRes> songResList = new LinkedList<>();
//        String lyricUrl = mSongModel.getLyric();
//        if (!TextUtils.isEmpty(lyricUrl)) {
//            UrlRes lyric = new UrlRes(lyricUrl, SongResUtils.getLyricDir(), SongResUtils.SUFF_ZRCE);
//            songResList.add(lyric);
//        }

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

        fetchLyric();
    }

    private void fetchLyric(){
        File lyricFile = SongResUtils.getZRCELyricFileByUrl(mSongModel.getLyric());
        if(lyricFile == null){
            LyricsManager.getLyricsManager(U.app())
                    .fetchLyricTask(mSongModel.getLyric())
                    .compose(bindUntilEvent(PresenterEvent.DESTROY))
                    .subscribe(new Consumer<File>() {
                @Override
                public void accept(File file) throws Exception {
                    showLyric();
                }
            });
        } else {
            showLyric();
        }
    }

    private void showLyric(){
        final String fileName = SongResUtils.getFileNameWithMD5(mSongModel.getLyric());
        LyricsManager.getLyricsManager(U.app()).loadLyricsObserable(fileName, fileName.hashCode() + "")
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Consumer<LyricsReader>() {
                    @Override
                    public void accept(LyricsReader lyricsReader) throws Exception {
                        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = lyricsReader.getLrcLineInfos();

                        for (int i = 0; i < lyricsLineInfos.size(); i++){
                            LyricsLineInfo lyricsLineInfo = lyricsLineInfos.get(i);
                            if(lyricsLineInfo.getStartTime() >= mSongModel.getBeginMs()){
                                String l = lyricsLineInfo.getLineLyrics();
                                l = l + "\n" + lyricsLineInfos.get(i + 1).getLineLyrics();
                                l = l + "\n" + lyricsLineInfos.get(i + 2).getLineLyrics();
                                mIPrepareResView.onLyricReady(l);
                                break;
                            }
                        }
                    }
                }, throwable -> MyLog.e(TAG, throwable));
    }

    public void cancelTask(){
        mSongResourceZhang.cancelAllTask();
    }
}
