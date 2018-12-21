package com.module.rankingmode.prepare.sence;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.GameReadyModel;
import com.module.rankingmode.prepare.model.JsonReadyInfo;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.presenter.MatchSucessPresenter;
import com.module.rankingmode.prepare.sence.controller.MatchSenceContainer;
import com.module.rankingmode.prepare.sence.controller.MatchSenceController;
import com.module.rankingmode.prepare.view.IMatchSucessView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FastMatchSuccessSence extends RelativeLayout implements ISence, IMatchSucessView {
    public static final String TAG = "FastMatchSuccessSence";


    MatchSenceController matchSenceController;

    BaseImageView mIvAArea;
    BaseImageView mIvBArea;
    BaseImageView mIvCArea;

    ExTextView mMatchStatusTv;

    MatchSucessPresenter mMatchSucessPresenter;

    volatile boolean isPrepared = false;

    PrepareData mPrepareData;

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
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .build());

        AvatarUtils.loadAvatarByUrl(mIvBArea,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .build());

        AvatarUtils.loadAvatarByUrl(mIvCArea,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .build());
    }

    @Override
    public void ready(boolean isPrepareState, List<JsonReadyInfo> list) {
        MyLog.d(TAG, "ready" + " isPrepareState=" + isPrepareState);
        isPrepared = isPrepareState;

        if(isPrepared){
            mMatchStatusTv.setText("已准备");
            mMatchStatusTv.setEnabled(false);
        }
    }

    @Override
    public void allPlayerIsReady(GameReadyModel jsonGameReadyInfo) {
        MyLog.d(TAG, "allPlayerIsReady" + " jsonGameReadyInfo=" + jsonGameReadyInfo);
        HandlerTaskTimer.newBuilder()
                .delay(2000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        matchSenceController.popSence();
                        ((BaseActivity)getContext()).finish();
                    }
                });
        mPrepareData.setGameReadyInfo(jsonGameReadyInfo);
        long localStartTs = System.currentTimeMillis()-jsonGameReadyInfo.getJsonGameStartInfo().getStartPassedMs();
        mPrepareData.setShiftTs((int) (localStartTs - jsonGameReadyInfo.getJsonGameStartInfo().getStartTimeMs()));
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKING_ROOM)
                .withSerializable("prepare_data", mPrepareData)
                .greenChannel().navigation();
    }

    @Override
    public void needReMatch() {
        MyLog.d(TAG, "needReMatch");
        matchSenceController.popSence();
        matchSenceController.toAssignSence(MatchSenceContainer.MatchSenceState.Matching, mPrepareData);
        U.getToastUtil().showShort("有人没有准备，需要重新匹配");
    }

    @Override
    public void toShow(RelativeLayout parentViewGroup, PrepareData data) {
        mPrepareData = data;
        if(getParent()==null) {
            //这里可能有动画啥的
            setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            parentViewGroup.addView(this);
        }
        matchSenceController.getCommonTitleBar().getCenterSubTextView().setText("已为你匹配到队伍");
        matchSenceController.getCommonTitleBar().getCenterTextView().setText("匹配成功");
        if (mMatchSucessPresenter != null) {
            mMatchSucessPresenter.destroy();
        }
        mMatchSucessPresenter = new MatchSucessPresenter(this, mPrepareData.getGameId(), mPrepareData);

        RxView.clicks(mMatchStatusTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getToastUtil().showShort("准备");
                    mMatchSucessPresenter.prepare(!isPrepared);
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
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mMatchSucessPresenter != null) {
            mMatchSucessPresenter.destroy();
        }
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
