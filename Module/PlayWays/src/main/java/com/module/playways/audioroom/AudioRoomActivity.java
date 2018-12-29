package com.module.playways.audioroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.changba.songstudio.audioeffect.AudioEffectStyleEnum;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExButton;
import com.engine.EngineManager;
import com.engine.Params;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.playways.rank.prepare.view.VoiceControlPanelView;
import com.module.playways.rank.room.fragment.PkRoomFragment;
import com.module.playways.rank.room.view.TopContainerView;
import com.module.playways.rank.song.fragment.SongSelectFragment;
import com.module.rank.R;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.FloatLyricsView;
import com.zq.lyrics.widget.ManyLyricsView;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

import static com.umeng.socialize.utils.ContextUtil.getContext;

@Route(path = RouterConstants.ACTIVITY_AUDIOROOM)
public class AudioRoomActivity extends BaseActivity {

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.audio_room_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        boolean selectSong = getIntent().getBooleanExtra("selectSong", false);
        if (selectSong) {
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, SongSelectFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(true)
                    .build());
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
