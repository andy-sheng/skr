package com.module.playways;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.constans.GameModeType;
import com.engine.EngineManager;
import com.module.RouterConstants;
import com.module.rank.R;
import com.module.playways.rank.room.fragment.PkRoomFragment;
import com.module.playways.rank.song.fragment.SongSelectFragment;

@Route(path = RouterConstants.ACTIVITY_PLAY_WAYS)
public class PlayWaysActivity extends BaseActivity {

    public static final String KEY_GAME_TYPE = "key_game_type";

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.rankingmode_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        boolean selectSong = getIntent().getBooleanExtra("selectSong", false);
        int gameType = getIntent().getIntExtra(KEY_GAME_TYPE, GameModeType.GAME_MODE_CLASSIC_RANK);
        if (gameType == GameModeType.GAME_MODE_CLASSIC_RANK || gameType == GameModeType.GAME_MODE_FUNNY) {
            if (selectSong) {
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_GAME_TYPE, gameType);
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, SongSelectFragment.class)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .setBundle(bundle)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                                showRoomFragment();
                            }
                        })
                        .build());
            } else {
                showRoomFragment();
            }
        }else if(gameType == GameModeType.GAME_MODE_GRAB){
            // 一唱到底抢唱模式,
            // TODO 只是测试，后面先跳到选歌页面
            ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                    .navigation();
        }
    }

    void showRoomFragment() {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, PkRoomFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build());
    }

    @Override
    protected void destroy() {
        super.destroy();
        EngineManager.getInstance().destroy("prepare");
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return false;
    }
}
