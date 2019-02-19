package com.module.playways;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.constans.GameModeType;
import com.engine.EngineManager;
import com.module.RouterConstants;
import com.module.playways.event.FinishPlayWayActivityEvent;
import com.module.playways.grab.songselect.SpecialSelectFragment;
import com.module.playways.rank.room.event.InputBoardEvent;
import com.module.rank.R;
import com.module.playways.rank.room.fragment.PkRoomFragment;
import com.module.playways.rank.song.fragment.SongSelectFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@Route(path = RouterConstants.ACTIVITY_PLAY_WAYS)
public class PlayWaysActivity extends BaseActivity {

    public static final String KEY_GAME_TYPE = "key_game_type";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG,"PlayWaysActivity onCreate" + " savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /**
         * 由准备页面未准备回退时，如果不在前台
         */
        if (!U.getActivityUtils().isAppForeground()) {
            MyLog.d(TAG,"PlayWaysActivity 在后台，不唤起");
            moveTaskToBack(true);
        }
    }

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.rankingmode_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        RelativeLayout mainActContainer = (RelativeLayout) findViewById(R.id.main_act_container);
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
                        .build());
            }
        } else if (gameType == GameModeType.GAME_MODE_GRAB) {
            mainActContainer.setBackground(null);
            // 一唱到底抢唱模式,
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, SpecialSelectFragment.class)
                    .setAddToBackStack(false)
                    .setHasAnimation(false)
                    .build());
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        EngineManager.getInstance().destroy("prepare");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FinishPlayWayActivityEvent event) {
        finish();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        // 自己处理有键盘时的整体布局
        return true;
    }
}
