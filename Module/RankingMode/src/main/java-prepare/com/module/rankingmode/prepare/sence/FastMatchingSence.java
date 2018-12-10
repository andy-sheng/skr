package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.presenter.MatchPresenter;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;
import com.module.rankingmode.prepare.view.MatchingLayerView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class FastMatchingSence extends RelativeLayout implements ISence {
    MatchSenceController matchSenceController;

    MatchPresenter matchPresenter;

    Disposable disposable;

    MatchingLayerView mLargeMatchingLayerView;
    MatchingLayerView mSmallMatchingLayerView;

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

    private void init(){
        inflate(getContext(), R.layout.matching_sence_layout, this);
        BaseImageView ownerIcon = findViewById(R.id.owner_icon);

        matchPresenter = new MatchPresenter();

        mLargeMatchingLayerView = findViewById(R.id.large_matching_layer_view);
        mSmallMatchingLayerView = findViewById(R.id.small_matching_layer_view);

        AvatarUtils.loadAvatarByUrl(ownerIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setCircle(true)
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());
    }
    @Override
    public void toShow(RelativeLayout parentViewGroup, Bundle bundle) {
        matchPresenter.startMatch();
        //这里可能有动画啥的
        setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        parentViewGroup.addView(this);

        disposable = Observable.timer(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                matchSenceController.toNextSence(null);
            }
        });

        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("一大波skrer在来的路上...");
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("匹配中...");
    }

    @Override
    public void toHide(RelativeLayout parentViewGroup) {
        //可能有动画
        setVisibility(GONE);
        disposable.dispose();
    }

    @Override
    public void toRemoveFromStack(RelativeLayout parentViewGroup) {
        // todo 仅做test
        matchPresenter.cancelMatch(1);

        parentViewGroup.removeView(this);
        mSmallMatchingLayerView.release();
        mLargeMatchingLayerView.release();
        disposable.dispose();
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
}
