package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PlayerInfo;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.presenter.MatchingPresenter;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;
import com.module.rankingmode.prepare.view.IMatchingView;
import com.module.rankingmode.prepare.view.MatchingLayerView;


import java.util.List;
import java.util.concurrent.TimeUnit;


public class FastMatchingSence extends RelativeLayout implements ISence, IMatchingView {
    public static final String TAG = "FastMatchingSence";

    MatchSenceController matchSenceController;

    MatchingPresenter matchingPresenter;

    MatchingLayerView mLargeMatchingLayerView;
    MatchingLayerView mSmallMatchingLayerView;

    ExTextView mMatchStatusTv;

    PrepareData mPrepareData;

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
                    matchingPresenter.cancelMatch();
                    matchSenceController.popSence();
                });
    }

    @Override
    public void toShow(RelativeLayout parentViewGroup, PrepareData data) {
        mPrepareData = data;
        if(getParent()==null) {
            //这里可能有动画啥的
            setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parentViewGroup.addView(this);
        }

        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("一大波skrer在来的路上...");
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("匹配中...");

        matchingPresenter.startLoopMatchTask(mPrepareData.getSongModel().getItemID());
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
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
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
        MyLog.d(TAG, "matchSucess" + " gameId=" + gameId + " gameCreatMs=" + gameCreatMs + " list=" + list);
        // 匹配成功
        mPrepareData.setGameId(gameId);
        mPrepareData.setGameCreatMs(gameCreatMs);
        mPrepareData.setPlayerInfoList(list);
        matchSenceController.toNextSence(mPrepareData);
    }

    @Override
    public void showUserIconList() {

    }
}
