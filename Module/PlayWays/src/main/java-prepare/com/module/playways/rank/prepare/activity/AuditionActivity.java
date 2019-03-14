package com.module.playways.rank.prepare.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.rank.prepare.fragment.AuditionFragment;
import com.module.playways.rank.prepare.fragment.AuditionPrepareResFragment;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;

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
            if (songModel.isAllResExist()) {
                PrepareData prepareData = new PrepareData();
                prepareData.setSongModel(songModel);
                prepareData.setBgMusic(songModel.getRankUserVoice());
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, AuditionFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
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
                        .setHasAnimation(true)
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

    @Override
    public boolean useEventBus() {
        return false;
    }
}
