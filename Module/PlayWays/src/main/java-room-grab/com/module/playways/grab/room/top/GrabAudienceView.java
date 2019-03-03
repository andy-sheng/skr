package com.module.playways.grab.room.top;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabWaitSeatUpdateEvent;
import com.module.playways.grab.room.event.SomeOneJoinWaitSeatEvent;
import com.module.playways.grab.room.event.SomeOneLeaveWaitSeatEvent;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GrabAudienceView extends RelativeLayout {
    public final static String TAG = "GrabAudienceView";
    List<BaseImageView> mBaseImageViewList = new ArrayList<>(6);
    List<GrabPlayerInfoModel> mWaitInfoModelList = new ArrayList<>();

    GrabRoomData mGrabRoomData;

    public GrabAudienceView(Context context) {
        this(context, null);
        init();
    }

    public GrabAudienceView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public GrabAudienceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabWaitSeatUpdateEvent event) {
        MyLog.d(TAG,"onEvent" + " event=" + event);
        mWaitInfoModelList.clear();
        if (event.list != null) {
            mWaitInfoModelList.addAll(event.list);
        }
        updateAllView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLeaveWaitSeatEvent event) {
        MyLog.d(TAG,"onEvent" + " event=" + event);
        Iterator<GrabPlayerInfoModel> iterator = mWaitInfoModelList.iterator();
        while (iterator.hasNext()) {
            GrabPlayerInfoModel grabPlayerInfoModel = iterator.next();
            if (grabPlayerInfoModel.getUserID() == event.getPlayerInfoModel().getUserID()) {
                iterator.remove();
            }
        }
        updateAllView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneJoinWaitSeatEvent event) {
        MyLog.d(TAG,"onEvent" + " event=" + event);
        for (int i = 0; i < mWaitInfoModelList.size(); i++) {
            if (event.getPlayerInfoModel().getUserID() == mWaitInfoModelList.get(i).getUserID()) {
                return;
            }
        }
        mWaitInfoModelList.add(event.getPlayerInfoModel());
        updateAllView();
    }

    public void setRoomData(GrabRoomData grabRoomData) {
        if (grabRoomData != null) {
            mGrabRoomData = grabRoomData;
            GrabRoundInfoModel grabRoundInfoModel = grabRoomData.getExpectRoundInfo();
            List<GrabPlayerInfoModel> l = grabRoundInfoModel.getWaitUsers();
            mWaitInfoModelList.clear();
            mWaitInfoModelList.addAll(l);
            updateAllView();
        } else {
            MyLog.d(TAG, "setGrabRoomData" + " grabRoomData error");
        }
    }

    public void init() {
        removeAllViews();
        for (int i = 0; i < 6; i++) {
            BaseImageView baseImageView = new BaseImageView(getContext());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(24), U.getDisplayUtils().dip2px(24));
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.rightMargin = U.getDisplayUtils().dip2px(15) * i;
            baseImageView.setVisibility(GONE);
            mBaseImageViewList.add(baseImageView);
            addView(baseImageView, layoutParams);
        }
    }

    public void updateAllView() {
        for (int i = 0; i < mBaseImageViewList.size(); i++) {
            mBaseImageViewList.get(i).setVisibility(GONE);
        }

        for (int i = 0; i < mWaitInfoModelList.size(); i++) {
            UserInfoModel userInfoModel = mWaitInfoModelList.get(i).getUserInfo();
            mBaseImageViewList.get(i).setVisibility(VISIBLE);
            AvatarUtils.loadAvatarByUrl(mBaseImageViewList.get(i), AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(1))
                    .setBorderColorBySex(userInfoModel.getIsMale())
                    .build());
        }
    }
}
