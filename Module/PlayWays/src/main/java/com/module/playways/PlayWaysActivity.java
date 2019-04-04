package com.module.playways;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.constans.GameModeType;
import com.engine.EngineManager;
import com.module.RouterConstants;
import com.module.playways.rank.song.fragment.SongSelectFragment;
import com.module.rank.R;

@Route(path = RouterConstants.ACTIVITY_PLAY_WAYS)
public class PlayWaysActivity extends BaseActivity {

    public static final String KEY_GAME_TYPE = "key_game_type";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG, "PlayWaysActivity onCreate" + " savedInstanceState=" + savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        MyLog.d(TAG, "onNewIntent" + " intent=" + intent);
        super.onNewIntent(intent);
        /**
         * 由准备页面未准备回退时，如果不在前台
         */
        if (!U.getActivityUtils().isAppForeground()
                // 如果应用刚回到前台500ms，也认为应用在后台。防止某些手机，比如华为Mate P20，
                // onActivityStarted 会比 onNewIntent 先调用，这里就是前台状态了
                || (System.currentTimeMillis() - U.getActivityUtils().getIsAppForegroundChangeTs() < 500)) {
            MyLog.d(TAG, "PlayWaysActivity 在后台，不唤起");
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onStart() {
        MyLog.d(TAG, "onStart");
        super.onStart();
    }

    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.rankingmode_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        RelativeLayout mainActContainer = (RelativeLayout) findViewById(R.id.main_act_container);
        boolean selectSong = getIntent().getBooleanExtra("selectSong", false);
        int gameType = getIntent().getIntExtra(KEY_GAME_TYPE, GameModeType.GAME_MODE_CLASSIC_RANK);
        if (gameType == GameModeType.GAME_MODE_CLASSIC_RANK) {
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
//            mainActContainer.setBackground(null);
//            // 一唱到底抢唱模式,
//            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabSelectFragment.class)
//                    .setAddToBackStack(false)
//                    .setHasAnimation(false)
//                    .build());
        } else {
            U.getToastUtil().showShort("该游戏模式已经下线 mode=" + gameType);
            finish();
        }

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

    @Override
    public boolean resizeLayoutSelfWhenKeybordShow() {
        // 自己处理有键盘时的整体布局
        return true;
    }
}
