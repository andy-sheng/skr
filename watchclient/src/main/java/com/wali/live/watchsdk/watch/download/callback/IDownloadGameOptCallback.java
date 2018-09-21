package com.wali.live.watchsdk.watch.download.callback;

import com.mi.live.data.gamecenter.model.GameInfoModel;

/**
 * Created by zhujianning on 18-9-21.
 */

public interface IDownloadGameOptCallback {
    void onResultCallback(GameInfoModel model,int status);
}
