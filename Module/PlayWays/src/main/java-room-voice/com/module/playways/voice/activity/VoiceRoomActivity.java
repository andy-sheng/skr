package com.module.playways.voice.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.module.RouterConstants;
import com.module.playways.RoomData;
import com.module.playways.voice.fragment.VoiceRoomFragment;
import com.module.rank.R;

@Route(path = RouterConstants.ACTIVITY_VOICEROOM)
public class VoiceRoomActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.voice_room_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        RoomData mRoomData = (RoomData) getIntent().getSerializableExtra("voice_room_data");

        U.getStatusBarUtil().setTransparentBar(this, false);
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, VoiceRoomFragment.class)
                .setAddToBackStack(false)
                .addDataBeforeAdd(0, mRoomData)
                .build()
        );
    }
}
