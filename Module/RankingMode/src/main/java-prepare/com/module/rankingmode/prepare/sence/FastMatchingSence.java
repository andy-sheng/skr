package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.prepare.presenter.MatchingPresenter;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;
import com.module.rankingmode.prepare.view.IMatchingView;
import com.module.rankingmode.prepare.view.MatchingLayerView;
import com.module.rankingmode.song.model.SongModel;

import java.util.List;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import static com.module.rankingmode.prepare.sence.FastMatchSuccessSence.BUNDLE_KEY_GAME_CREATE_MS;
import static com.module.rankingmode.prepare.sence.FastMatchSuccessSence.BUNDLE_KEY_GAME_ID;
import static com.module.rankingmode.prepare.sence.FastMatchSuccessSence.BUNDLE_KEY_GAME_PLAYER;
import static com.module.rankingmode.prepare.sence.FastMatchSuccessSence.BUNDLE_KEY_GAME_PLAYERS;
import static com.module.rankingmode.prepare.sence.FastMatchSuccessSence.BUNDLE_KEY_GAME_SONG;

public class FastMatchingSence extends RelativeLayout implements ISence, IMatchingView {

    MatchSenceController matchSenceController;

    MatchingPresenter matchingPresenter;

    MatchingLayerView mLargeMatchingLayerView;
    MatchingLayerView mSmallMatchingLayerView;

    ExTextView mMatchStatusTv;

    Bundle nextBundle = new Bundle();

    SongModel songModel;

    public FastMatchingSence(Context context) {
        this(context, null);
    }

    public FastMatchingSence(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastMatchingSence(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.matching_sence_layout, this);
        BaseImageView ownerIcon = findViewById(R.id.owner_icon);

        mMatchStatusTv = findViewById(R.id.match_status_tv);

        matchingPresenter = new MatchingPresenter(this);

        mLargeMatchingLayerView = findViewById(R.id.large_matching_layer_view);
        mSmallMatchingLayerView = findViewById(R.id.small_matching_layer_view);

        AvatarUtils.loadAvatarByUrl(ownerIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setCircle(true)
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());

        matchingPresenter.getLoadingUserListIcon();

        RxView.clicks(mMatchStatusTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    matchingPresenter.cancelMatch(1);
                    matchSenceController.popSence();
                });
    }

    @Override
    public void toShow(RelativeLayout parentViewGroup, Bundle bundle) {
        matchingPresenter.startLoopMatchTask();
        //这里可能有动画啥的
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parentViewGroup.addView(this);

        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("一大波skrer在来的路上...");
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("匹配中...");

        songModel = (SongModel) bundle.getSerializable("song_model");
    }

    @Override
    public void toHide(RelativeLayout parentViewGroup) {
        //可能有动画
        setVisibility(GONE);
    }

    @Override
    public void toRemoveFromStack(RelativeLayout parentViewGroup) {
        // todo 只有场景往上切是取消匹配
        parentViewGroup.removeView(this);
        mSmallMatchingLayerView.release();
        mLargeMatchingLayerView.release();

        if (matchingPresenter != null) {
            matchingPresenter.destroy();
        }
    }

    //每个场景有一个是不是可以往下跳的判断
    @Override
    public boolean isPrepareToNextSence() {
        return true;
    }

    @Override
    public void onResumeSence(RelativeLayout parentViewGroup) {
        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("一大波skrer在来的路上...");
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("匹配中...");
        setVisibility(VISIBLE);
    }

    @Override
    public boolean removeWhenPush() {
        return true;
    }

    @Override
    public void setSenceController(MatchSenceController matchSenceController) {
        this.matchSenceController = matchSenceController;
    }

    @Override
    public void matchSucess(int gameId, long gameCreatMs, List<PlayerInfo> list) {
        // 匹配成功
        nextBundle.putInt(BUNDLE_KEY_GAME_ID, gameId);
        nextBundle.putLong(BUNDLE_KEY_GAME_CREATE_MS, gameCreatMs);
        nextBundle.putSerializable(BUNDLE_KEY_GAME_SONG, songModel);
        // TODO: 2018/12/12 记得可以不改成bundle传数据
//        nextBundle.putSerializable(BUNDLE_KEY_GAME_PLAYERS, (Object)list);
        matchSenceController.toNextSence(nextBundle);
    }

    @Override
    public void showUserIconList() {

    }
}
