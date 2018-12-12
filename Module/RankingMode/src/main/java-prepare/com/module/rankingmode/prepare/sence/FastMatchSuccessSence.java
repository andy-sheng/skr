package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.JsonGameReadyInfo;
import com.module.rankingmode.prepare.presenter.MatchSucessPresenter;
import com.module.rankingmode.prepare.sence.controller.MatchSenceContainer;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;
import com.module.rankingmode.prepare.view.IMatchSucessView;
import com.module.rankingmode.song.model.SongModel;

import java.util.concurrent.TimeUnit;

public class FastMatchSuccessSence extends RelativeLayout implements ISence, IMatchSucessView {

    public final static String BUNDLE_KEY_GAME_ID = "current_game_id";
    public final static String BUNDLE_KEY_GAME_CREATE_MS = "game_create_ms";
    public final static String BUNDLE_KEY_GAME_READY_INFO = "game_ready_info";
    public final static String BUNDLE_KEY_GAME_SONG = "game_song_info";
    public final static String BUNDLE_KEY_GAME_PLAYERS = "game_player";

    int currentGameId;
    long gameCreateMs;

    MatchSenceController matchSenceController;

    BaseImageView mIvAArea;
    BaseImageView mIvBArea;
    BaseImageView mIvCArea;

    ExTextView mMatchStatusTv;

    MatchSucessPresenter matchSucessPresenter;

    volatile boolean isPrepared = false;

    SongModel songModel;

    public FastMatchSuccessSence(Context context) {
        this(context, null);
    }

    public FastMatchSuccessSence(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastMatchSuccessSence(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.match_success_layout, this);

        mIvAArea = findViewById(R.id.iv_a_area);
        mIvBArea = findViewById(R.id.iv_b_area);
        mIvCArea = findViewById(R.id.iv_c_area);

        mMatchStatusTv = findViewById(R.id.match_status_tv);

        AvatarUtils.loadAvatarByUrl(mIvAArea,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setCircle(true)
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());

        AvatarUtils.loadAvatarByUrl(mIvBArea,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setCircle(true)
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());

        AvatarUtils.loadAvatarByUrl(mIvCArea,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setCircle(true)
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());
    }

    @Override
    public void ready(boolean isPrepareState) {
        isPrepared = isPrepareState;

        if(isPrepared){
            mMatchStatusTv.setText("已准备");
            mMatchStatusTv.setEnabled(false);
        }
    }

    @Override
    public void allPlayerIsReady(JsonGameReadyInfo jsonGameReadyInfo) {
        matchSenceController.popSence();
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKING_ROOM)
                .withSerializable("song_model", songModel)
                .withInt(BUNDLE_KEY_GAME_ID, currentGameId)
                .withLong(BUNDLE_KEY_GAME_CREATE_MS, gameCreateMs)
                .withSerializable(BUNDLE_KEY_GAME_READY_INFO, jsonGameReadyInfo)
                .greenChannel().navigation();
    }

    @Override
    public void needReMatch() {
        matchSenceController.popSence();
        Bundle bundle = new Bundle();
        bundle.putSerializable("song_model", songModel);
        matchSenceController.toAssignSence(MatchSenceContainer.MatchSenceState.Matching, bundle);
        U.getToastUtil().showShort("有人没有准备，需要重新匹配");
    }

    @Override
    public void toShow(RelativeLayout parentViewGroup, Bundle bundle) {
        //这里可能有动画啥的
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parentViewGroup.addView(this);
        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("已为你匹配到队伍");
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("匹配成功");

        currentGameId = bundle.getInt(BUNDLE_KEY_GAME_ID);
        gameCreateMs = bundle.getLong(BUNDLE_KEY_GAME_CREATE_MS);
        songModel = (SongModel) bundle.getSerializable("song_model");

        matchSucessPresenter = new MatchSucessPresenter(this, currentGameId);

        RxView.clicks(mMatchStatusTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getToastUtil().showShort("准备");
                    matchSucessPresenter.prepare(!isPrepared);
                });
    }

    @Override
    public void toHide(RelativeLayout parentViewGroup) {
        //可能有动画
        setVisibility(GONE);
    }

    @Override
    public void toRemoveFromStack(RelativeLayout parentViewGroup) {
        parentViewGroup.removeView(this);
        matchSucessPresenter.destroy();
    }

    //每个场景有一个是不是可以往下跳的判断
    @Override
    public boolean isPrepareToNextSence() {
        return true;
    }

    @Override
    public void onResumeSence(RelativeLayout parentViewGroup) {
        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("已为你匹配到队伍");
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("匹配成功");
        setVisibility(VISIBLE);
    }

    @Override
    public boolean removeWhenPush() {
        return false;
    }

    @Override
    public void setSenceController(MatchSenceController matchSenceController) {
        this.matchSenceController = matchSenceController;
    }
}
