package com.module.playways.grab.room.top;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.airbnb.lottie.L;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.image.model.BaseImage;
import com.common.log.MyLog;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabWaitSeatUpdateEvent;
import com.module.playways.grab.room.event.SomeOneGrabEvent;
import com.module.playways.grab.room.event.SomeOneJoinWaitSeatEvent;
import com.module.playways.grab.room.event.SomeOneLeaveWaitSeatEvent;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GrabAudienceView extends RelativeLayout {
    public final static String TAG = "GrabAudienceView";
    List<BaseImageView> mBaseImageViewList = new ArrayList<>(6);
    List<GrabPlayerInfoModel> mPlayerInfoModelList = new ArrayList<>();

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
        mPlayerInfoModelList.clear();
        if (event.list != null) {
            mPlayerInfoModelList.addAll(event.list);
        }
        updateAllView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLeaveWaitSeatEvent event) {
        Iterator<GrabPlayerInfoModel> iterator = mPlayerInfoModelList.iterator();
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
        for (int i = 0; i < mPlayerInfoModelList.size(); i++) {
            if (event.getPlayerInfoModel().getUserID() == mPlayerInfoModelList.get(i).getUserID()) {
                return;
            }
        }
        mPlayerInfoModelList.add(event.getPlayerInfoModel());
        updateAllView();
    }

    public void setGrabRoomData(GrabRoomData grabRoomData) {
        if (grabRoomData != null) {
            mGrabRoomData = grabRoomData;
            GrabRoundInfoModel grabRoundInfoModel = grabRoomData.getRealRoundInfo();

            if(grabRoundInfoModel != null && grabRoundInfoModel.getWaitUsers() != null){
                mPlayerInfoModelList.addAll(grabRoundInfoModel.getWaitUsers());
                updateAllView();
            }
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

        for (int i = 0; i < mPlayerInfoModelList.size(); i++) {
            UserInfoModel userInfoModel = mPlayerInfoModelList.get(i).getUserInfo();
            mBaseImageViewList.get(i).setVisibility(VISIBLE);
            AvatarUtils.loadAvatarByUrl(mBaseImageViewList.get(i), AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(1))
                    .setBorderColorBySex(userInfoModel.getIsMale())
                    .build());
        }
    }
}
