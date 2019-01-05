package com.module.playways.rank.room.presenter;

import android.text.TextUtils;

import com.common.mvp.RxLifeCyclePresenter;
import com.common.utils.HttpUtils;
import com.common.utils.SongResUtils;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.zq.lyrics.model.UrlRes;
import com.zq.lyrics.utils.ZipUrlResourceManager;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.LinkedList;
import java.util.List;

public class DownLoadScoreFilePresenter extends RxLifeCyclePresenter {
    ZipUrlResourceManager mZipUrlResourceManager;
    HttpUtils.OnDownloadProgress mOnDownloadProgress;

    List<PlayerInfoModel>  mPlayerInfoModels;

    public DownLoadScoreFilePresenter(@NotNull HttpUtils.OnDownloadProgress onDownloadProgress, List<PlayerInfoModel> playerInfoList){

        mOnDownloadProgress = onDownloadProgress;
        mPlayerInfoModels = playerInfoList;
    }

    public void prepareRes() {
        if(mPlayerInfoModels==null){
            return;
        }
        LinkedList<UrlRes> songResList = new LinkedList<>();
        for (PlayerInfoModel playerInfo : mPlayerInfoModels) {
            if(playerInfo.isSkrer()){
                String midiUrl = playerInfo.getResourceInfoList().get(0).getMidiURL();
                if (!TextUtils.isEmpty(midiUrl)) {
                    UrlRes midi = new UrlRes(midiUrl, SongResUtils.getMIDIDir(), SongResUtils.SUFF_MIDI);
                    songResList.add(midi);
                }
            }
        }

        mZipUrlResourceManager = new ZipUrlResourceManager(songResList, mOnDownloadProgress);
        mZipUrlResourceManager.go();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mZipUrlResourceManager != null) {
            mZipUrlResourceManager.cancelAllTask();
        }
    }
}
