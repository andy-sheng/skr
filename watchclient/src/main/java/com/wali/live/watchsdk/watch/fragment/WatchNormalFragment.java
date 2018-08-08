package com.wali.live.watchsdk.watch.fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.base.activity.BaseActivity;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.flybarrage.view.FlyBarrageViewGroup;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchSdkView;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

public class WatchNormalFragment extends BaseWatchFragment {

    protected ImageView mCloseBtn;// 关闭按钮

    protected WatchSdkView mSdkView;

    // 高斯蒙层
    private BaseImageView mMaskIv;
    // 礼物特效动画
    private FlyBarrageViewGroup mFlyBarrageViewGroup;

    @Override
    public int getRequestCode() {
        return mRequestCode;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_watch_normal, container, false);
    }

    @Override
    protected void bindView() {
        super.bindView();

        // 封面模糊图
        mMaskIv = $(R.id.mask_iv);
        RoomInfo roomInfo = getWatchSdkInterface().getRoomInfo();
        RoomBaseDataModel roomBaseData = getWatchSdkInterface().getRoomBaseData();

        String url = roomInfo.getCoverUrl();
        if (TextUtils.isEmpty(url)) {
            url = AvatarUtils.getAvatarUrlByUidTs(roomInfo.getPlayerId(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, roomInfo.getAvatar());
        }
        AvatarUtils.loadAvatarByUrl(mMaskIv, url, false, true, R.drawable.rect_loading_bg_24292d);

        initGiftContinueViewGroup();
        initGiftRoomEffectView();
        initGiftAnimationView();

        //关闭按钮
        mCloseBtn = $(R.id.close_btn);
        RxView.clicks(mCloseBtn)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        finish();
                    }
                });
        mCloseBtn.setVisibility(View.VISIBLE);

        orientCloseBtn(getWatchSdkInterface().isDisplayLandscape());

        mSdkView = new WatchSdkView(getActivity(), (ViewGroup) mRootView.findViewById(R.id.main_act_container), mController);
        mSdkView.setupView((roomBaseData.getLiveType() == LiveManager.TYPE_LIVE_GAME) || (roomBaseData.getLiveType() == LiveManager.TYPE_LIVE_HUYA), roomBaseData.getLiveType() == LiveManager.TYPE_LIVE_HUYA);
        mSdkView.startView();

        mFlyBarrageViewGroup = (FlyBarrageViewGroup) mRootView.findViewById(R.id.fly_barrage_viewgroup);
        mFlyBarrageViewGroup.onActivityCreate();

        initPresenter();
    }

    private void finish() {
        getActivity().finish();
    }

    private void orientCloseBtn(boolean isLandscape) {
        if (mCloseBtn == null) {
            return;
        }
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)
                mCloseBtn.getLayoutParams();
        if (!isLandscape && BaseActivity.isProfileMode()) {
            layoutParams.topMargin = layoutParams.rightMargin + BaseActivity.getStatusBarHeight();
        } else {
            layoutParams.topMargin = layoutParams.rightMargin;
        }
        mCloseBtn.setLayoutParams(layoutParams);
    }

    @Override
    public void pageUpEvent() {
        super.pageUpEvent();
        if (mSdkView != null) {
            MyLog.d(TAG, "page down internal");
            mSdkView.switchToNextRoom();
        }
    }

    @Override
    public void PageDownEvent() {
        super.PageDownEvent();
        if (mSdkView != null) {
            MyLog.d(TAG, "page up internal");
            mSdkView.switchToLastRoom();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MyLog.d(TAG, "onDestroy");

        if (mSdkView != null) {
            mSdkView.stopView();
            mSdkView.release();
            mSdkView = null;
        }

        if(mFlyBarrageViewGroup != null) {
            mFlyBarrageViewGroup.onActivityDestroy();
        }
    }

    @Override
    public void playerReadyEvent() {
        super.playerReadyEvent();
        if (mMaskIv.getVisibility() == View.VISIBLE) {
            mMaskIv.setVisibility(View.GONE);
        }
        if (mSdkView != null) {
            mSdkView.postPrepare();
        }
    }

    @Override
    public void orientLandscape() {
        super.orientLandscape();
        orientCloseBtn(true);
    }

    @Override
    public void orientPortrait() {
        super.orientPortrait();
        orientCloseBtn(false);
    }

    @Override
    public void switchRoom() {
        super.switchRoom();
        mFlyBarrageViewGroup.reset();
        if (mSdkView != null) {
            mSdkView.reset();
            mSdkView.postSwitch(getWatchSdkInterface().getRoomBaseData().getLiveType() == LiveManager.TYPE_LIVE_GAME);
        }
    }
}
