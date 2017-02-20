package com.wali.live.watchsdk.watch.presenter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;

import com.base.activity.RxActivity;
import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.event.SdkEventClass;
import com.mi.live.data.push.SendBarrageManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.keyboard.KeyboardUtils;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.view.BottomGameInputView;
import com.wali.live.watchsdk.watch.view.GameBarrageViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by chengsimin on 2017/1/14.
 */

public class GameModePresenter implements IBindActivityLIfeCycle {

    RxActivity mActivity;
    RoomBaseDataModel mRoomBaseDataModel;

    private ViewStub mGameBarrageViewStub;
    private ViewStub mGameBottomViewStub;
    View mCommentView;
    View mWatchTopView;
    View mNormalCommentBtn;
    View mBottomBtnViewGroup;
    View mCloseBtn;
    View mRotateBtn;

    BottomGameInputView mBottomGameInputView;
    GameBarrageViewGroup mGameBarrageViewGroup;

    TouchPresenter mTouchPresenter;

    boolean mBarrageNeedShow = true;
    boolean mGameInputShow = false;
    boolean mHasInflate = false;


    public GameModePresenter(RxActivity watchSdkActivity, RoomBaseDataModel mMyRoomData) {
        this.mActivity = watchSdkActivity;
        this.mRoomBaseDataModel = mMyRoomData;
    }

    public void setCommentView(View commentView) {
        this.mCommentView = commentView;
    }

    public void setWatchTopView(View watchTopView) {
        this.mWatchTopView = watchTopView;
    }

    public void setNormalCommentBtn(View commentBtn) {
        this.mNormalCommentBtn = commentBtn;
    }

    public void setCloseBtn(View btn) {
        this.mCloseBtn = btn;
    }

    public void setRotateBtn(View btn) {
        this.mRotateBtn = btn;
    }

    public void setBottomContainerView(View bottomContainerView) {
        this.mBottomBtnViewGroup = bottomContainerView;
    }


    public void setmTouchPresenter(TouchPresenter touchPresenter) {
        this.mTouchPresenter = touchPresenter;
    }

    public void setGameBarrageViewStub(ViewStub gameViewStub) {
        this.mGameBarrageViewStub = gameViewStub;
    }

    public void setGameBottomViewStub(ViewStub gameViewStub) {
        this.mGameBottomViewStub = gameViewStub;
    }

    @Override
    public void onActivityDestroy() {
        EventBus.getDefault().unregister(this);
        if (mBottomGameInputView != null) {
            EventBus.getDefault().unregister(mBottomGameInputView);
        }
        if (mGameBarrageViewGroup != null) {
            mGameBarrageViewGroup.onActivityDestroy();
        }
    }

    @Override
    public void onActivityCreate() {
        EventBus.getDefault().register(this);
    }

    void ensureInflate() {
        if (!mHasInflate) {
            mHasInflate = true;
            mGameBarrageViewGroup = (GameBarrageViewGroup) mGameBarrageViewStub.inflate();
            mGameBarrageViewGroup.onActivityCreate();

            if (mGameBottomViewStub != null) {
                mBottomGameInputView = (BottomGameInputView) mGameBottomViewStub.inflate();
                EventBus.getDefault().register(mBottomGameInputView);


                mBottomGameInputView.setBarrageBtnClickListener(new BottomGameInputView.BarrageBtnClickListener() {
                    @Override
                    public void onSwitch(boolean open) {
                        mBarrageNeedShow = open;
                        if (open) {
                            mGameBarrageViewGroup.setVisibility(View.VISIBLE);
                        } else {
                            mGameBarrageViewGroup.setVisibility(View.GONE);
                        }
                    }
                });


                mBottomGameInputView.setListener(new BottomGameInputView.IBottomGameInputView() {
                    @Override
                    public void onSendClick(String msg) {
                        if (CommonUtils.isFastDoubleClick(500)) {
                            return;
                        }
                        String body = SmileyParser.getInstance()
                                .convertString(msg, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();
                        if (!mRoomBaseDataModel.canSpeak()) {
                            ToastUtils.showToast(GlobalData.app(), R.string.can_not_speak);
                            return;
                        }
                        if (TextUtils.isEmpty(body.trim())) {
                            return;
                        }
                        BarrageMsg.PkMessageExt pkExt = null;

                        //检查发送频率限制
                        if (mRoomBaseDataModel.getmMsgRule() != null && mRoomBaseDataModel.getmMsgRule().getSpeakPeriod() == Integer.MAX_VALUE) {
                            return;
                        }
                        BarrageMsg barrageMsg = SendBarrageManager.createBarrage(BarrageMsgType.B_MSG_TYPE_TEXT, body, mRoomBaseDataModel.getRoomId(), mRoomBaseDataModel.getUid(), System.currentTimeMillis(), null);
                        SendBarrageManager
                                .sendBarrageMessageAsync(barrageMsg)
                                .subscribe();
                        SendBarrageManager.pretendPushBarrage(barrageMsg);
                    }

                    @Override
                    public void onGameInputAreaClick() {
                        mWatchTopView.setVisibility(View.GONE);
                        mGameInputShow = true;
                    }
                });
            }

            TouchPresenter.AnimationParams animationParamsGame = new TouchPresenter.AnimationParams();
            animationParamsGame.animationWays = TouchPresenter.ANIMATION_TAP_DISMISS;
            animationParamsGame.views = new View[]{
                    mWatchTopView,
                    mBottomBtnViewGroup,
                    mBottomGameInputView,
                    mCloseBtn,
                    mRotateBtn,
            };
            animationParamsGame.ext = new int[]{
                    TouchPresenter.DIRECTION_UP,
                    TouchPresenter.DIRECTION_RIGHT,
                    TouchPresenter.DIRECTION_DOWN,
                    TouchPresenter.DIRECTION_RIGHT,
                    TouchPresenter.DIRECTION_RIGHT,
            };
            mTouchPresenter.setNeedHideViewsLandscape(animationParamsGame);
        }
    }

    public void hideInputArea() {
        if (mGameInputShow) {
            KeyboardUtils.hideKeyboardImmediately(mActivity);
            mGameInputShow = false;
            mWatchTopView.setVisibility(View.VISIBLE);
        }
    }

    public boolean ismInputViewShow() {
        return mGameInputShow;
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        ensureInflate();
        if (event.isLandscape()) {
            orientLandscape();
        } else {
            orientPortrait();
        }
    }

    private void orientPortrait() {
        if (mGameBarrageViewGroup != null) {
            mGameBarrageViewGroup.setVisibility(View.GONE);
        }
        if (mCommentView != null) {
            mCommentView.setVisibility(View.VISIBLE);
        }
        if (mBottomGameInputView != null) {
            mBottomGameInputView.setVisibility(View.GONE);
        }
        if (mNormalCommentBtn != null) {
            mNormalCommentBtn.setVisibility(View.VISIBLE);
        }
    }

    private void orientLandscape() {
        if (mNormalCommentBtn != null) {
            mNormalCommentBtn.setVisibility(View.GONE);
        }

        // 为了不使得布局乱掉
        if (mCommentView != null) {
            mCommentView.setVisibility(View.INVISIBLE);
        }
        if (mBottomGameInputView != null) {
            mBottomGameInputView.setVisibility(View.VISIBLE);
        }
        if (mGameBarrageViewGroup != null) {
            if (!mBarrageNeedShow) {
                mGameBarrageViewGroup.setVisibility(View.GONE);
            } else {
                mGameBarrageViewGroup.setVisibility(View.VISIBLE);
            }
        }
    }


}
