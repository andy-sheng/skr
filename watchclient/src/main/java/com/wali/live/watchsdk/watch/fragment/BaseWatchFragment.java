package com.wali.live.watchsdk.watch.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.base.activity.BaseActivity;
import com.base.event.SdkEventClass;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.BaseFragment;
import com.base.log.MyLog;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.GiftInfoForEnterRoom;
import com.mi.live.data.push.collection.InsertSortLinkedList;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.Params;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.common.gift.view.GiftRoomEffectView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.wali.live.component.BaseSdkController.MSG_NEW_VIDEO_URL;
import static com.wali.live.component.BaseSdkController.MSG_ON_LINK_MIC_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_LIVE_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_START;
import static com.wali.live.watchsdk.base.BaseComponentSdkActivity.EXTRA_ROOM_INFO;
import static com.wali.live.watchsdk.watch.WatchSdkActivity.EXTRA_ROOM_INFO_LIST;
import static com.wali.live.watchsdk.watch.WatchSdkActivity.EXTRA_ROOM_INFO_POSITION;
import static com.wali.live.watchsdk.watch.WatchSdkActivity.ROOM_DATA;

/**
 * Created by zhujianning on 18-8-7.
 */

public class BaseWatchFragment extends BaseEventBusFragment {

    protected GiftRoomEffectView mGiftRoomEffectView;
    protected GiftContinueViewGroup mGiftContinueViewGroup;
    protected GiftAnimationView mGiftAnimationView;

    protected WatchComponentController mController;

    //data
//    protected RoomBaseDataModel mMyRoomData;
//    protected RoomInfo mRoomInfo = null;
//    protected ArrayList<RoomInfo> mRoomInfoList;
//    protected int mRoomInfoPosition;

    //presenter
    private GiftMallPresenter mGiftMallPresenter;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected void bindView() {
        setWatchComponentController(((WatchSdkActivity) getActivity()).getController());
    }

    @Override
    protected boolean needForceActivityOrientation() {
        return false;
    }

    protected void initGiftRoomEffectView() {
        mGiftRoomEffectView = (GiftRoomEffectView) mRootView.findViewById(R.id.gift_room_effect_view);
        if(mGiftRoomEffectView != null) {
            mGiftRoomEffectView.onActivityCreate();
        }
    }

    protected void initGiftAnimationView() {
        mGiftAnimationView = (GiftAnimationView) mRootView.findViewById(R.id.gift_animation_player_view);
        if(mGiftAnimationView != null) {
            mGiftAnimationView.onActivityCreate();
        }
    }

    protected void initGiftContinueViewGroup() {
        mGiftContinueViewGroup = (GiftContinueViewGroup) mRootView.findViewById(R.id.gift_continue_vg);
        if(mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.onActivityCreate();
        }
    }

//    protected void initParam() {
//        Bundle bundle = getArguments();
//        if(bundle != null) {
//            mMyRoomData = (RoomBaseDataModel) bundle.getSerializable(ROOM_DATA);
//            mRoomInfo = bundle.getParcelable(EXTRA_ROOM_INFO);
//            mRoomInfoList = bundle.getParcelableArrayList(EXTRA_ROOM_INFO_LIST);
//            mRoomInfoPosition = bundle.getInt(EXTRA_ROOM_INFO_POSITION, 0);
//        }
//    }

    public void setWatchComponentController(WatchComponentController controller) {
        this.mController = controller;
    }

    public void pageUpEvent() {

    }

    public void PageDownEvent() {

    }

    public void playerReadyEvent() {

    }

    public void switchRoom() {
        //重置对应的view
        if(mGiftMallPresenter != null) {
            mGiftMallPresenter.reset();
        }

        if(mGiftAnimationView != null) {
            mGiftAnimationView.reset();
        }

        if(mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.reset();
        }

        if(mGiftRoomEffectView != null) {
            mGiftRoomEffectView.reset();
        }
    }

    public int getGiftSendTickets() {
        if(mGiftMallPresenter != null) {
            return mGiftMallPresenter.getSpendTicket();
        }

        return 0;
    }

    public boolean isGiftMallViewVisibility() {
        if(mGiftMallPresenter != null) {
            return mGiftMallPresenter.isGiftMallViewVisibility();
        }

        return false;
    }

    public void syncRoomEffect(GiftInfoForEnterRoom giftInfoForEnterRoom) {
        mGiftMallPresenter.setGiftInfoForEnterRoom(giftInfoForEnterRoom.getmGiftInfoForThisRoom());
        mGiftMallPresenter.setPktGiftId(giftInfoForEnterRoom.getPktGiftId());
    }

    protected void initPresenter() {
        mGiftMallPresenter = new GiftMallPresenter(getActivity(), mRootView.getContext(), ((WatchSdkActivity) getActivity()).getRoomBaseData(), mController);
        mGiftMallPresenter.onActivityCreate();
//        addBindActivityLifeCycle(mGiftMallPresenter, true);
        mGiftMallPresenter.setViewStub((ViewStub) mRootView.findViewById(R.id.gift_mall_view_viewstub));
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GiftEventClass.GiftMallEvent event) {
        switch (event.eventType) {
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST: {
                mGiftMallPresenter.hideGiftMallView();
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_SHOW_MALL_LIST: {
                mGiftMallPresenter.showGiftMallView();
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_CLICK_SELECT_GIFT: {
                mGiftMallPresenter.showGiftMallView();
                mGiftMallPresenter.selectGiftView((Integer) event.obj1);
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE: {
                goToRecharge();
            }
            break;
        }
    }

    public void orientLandscape() {
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_LANDSCAPE);
        }

        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(true);
        }
    }

    public void orientPortrait() {
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_PORTRAIT);
        }

        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(false);
        }
    }

    private void goToRecharge() {
        ((WatchSdkActivity)getActivity()).goToRecharge();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGiftMallPresenter != null) {
            mGiftMallPresenter.onActivityDestroy();
        }

        if (mGiftRoomEffectView != null) {
            mGiftRoomEffectView.onActivityDestroy();
        }

        if(mGiftAnimationView != null) {
            mGiftAnimationView.onActivityDestroy();
        }

        if(mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.onActivityDestroy();
        }
    }
}
