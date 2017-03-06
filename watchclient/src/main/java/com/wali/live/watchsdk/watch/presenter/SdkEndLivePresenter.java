package com.wali.live.watchsdk.watch.presenter;

import android.app.Activity;
import android.os.Bundle;

import com.wali.live.watchsdk.endlive.EndLivePresenter;
import com.wali.live.watchsdk.endlive.IUserEndLiveView;
import com.wali.live.proto.RoomRecommend;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

/**
 * Created by chengsimin on 2016/12/2.
 */

public class SdkEndLivePresenter extends EndLivePresenter {
    public SdkEndLivePresenter(IUserEndLiveView view, Bundle bundle) {
        super(view, bundle);
    }

    @Override
    public void startWatchActivity(Activity activity, RoomRecommend.RecommendRoom roomData, int position) {
        RoomInfo info = RoomInfo.Builder.newInstance(roomData.getZuid(), roomData.getLiveId(), roomData.getDownStreamUrl()).build();
        WatchSdkActivity.openActivity(activity, info);
    }
}
