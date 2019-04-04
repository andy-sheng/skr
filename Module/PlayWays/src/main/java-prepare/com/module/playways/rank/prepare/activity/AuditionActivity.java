package com.module.playways.rank.prepare.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.rank.prepare.fragment.AuditionFragment;
import com.module.playways.rank.prepare.fragment.AuditionPrepareResFragment;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.song.SongSelectServerApi;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

@Route(path = RouterConstants.ACTIVITY_AUDITION_ROOM)
public class AuditionActivity extends BaseActivity {

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.audition_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        SongModel songModel = (SongModel) getIntent().getSerializableExtra("songModel");
        if (songModel != null) {
            reportAuditionMusic(songModel.getItemID());
            if (songModel.isAllResExist()) {
                PrepareData prepareData = new PrepareData();
                prepareData.setSongModel(songModel);
                prepareData.setBgMusic(songModel.getRankUserVoice());
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, AuditionFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(0, prepareData)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                            }
                        })
                        .build());
            } else {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, AuditionPrepareResFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(0, songModel)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                            }
                        })
                        .build());
            }
        } else {

        }
    }

    private void reportAuditionMusic(int itemID) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);

        HashMap<String, Object> map = new HashMap<>();
        map.put("playbookItemID", itemID);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(songSelectServerApi.reportAuditionSong(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.d(TAG, "上报歌曲成功");
                }
            }
        }, this);
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
