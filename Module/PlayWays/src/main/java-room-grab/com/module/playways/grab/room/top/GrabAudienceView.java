package com.module.playways.grab.room.top;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabWaitSeatUpdateEvent;
import com.zq.person.event.ShowPersonCardEvent;
import com.module.playways.grab.room.event.SomeOneJoinWaitSeatEvent;
import com.module.playways.grab.room.event.SomeOneLeaveWaitSeatEvent;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GrabAudienceView extends RelativeLayout {
    public final String TAG = "GrabAudienceView";
    public static final int MAX_COUNT = 3;
    List<VH> mBaseImageViewList = new ArrayList<>(MAX_COUNT);
    List<GrabPlayerInfoModel> mWaitInfoModelList = new ArrayList<>();

    ExTextView mTvCount;

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
        MyLog.d(TAG, "onEvent" + " event=" + event);
        mWaitInfoModelList.clear();
        if (event.list != null) {
            mWaitInfoModelList.addAll(event.list);
        }
        updateAllView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLeaveWaitSeatEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
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
        MyLog.d(TAG, "onEvent" + " event=" + event);
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
            if (grabRoundInfoModel != null) {
                List<GrabPlayerInfoModel> l = grabRoundInfoModel.getWaitUsers();
                mWaitInfoModelList.clear();
                mWaitInfoModelList.addAll(l);
                updateAllView();
            }
        } else {
            MyLog.d(TAG, "setGrabRoomData" + " grabRoomData error");
        }
    }

    public void init() {
        inflate(getContext(), R.layout.audience_view_layout, this);
//        removeAllViews();
//        for (int i = 0; i < MAX_COUNT; i++) {
//            BaseImageView baseImageView = new BaseImageView(getContext());
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(24), U.getDisplayUtils().dip2px(24));
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            layoutParams.rightMargin = U.getDisplayUtils().dip2px(15) * i;
//            baseImageView.setVisibility(GONE);
//            VH vp = new VH(baseImageView);
//            mBaseImageViewList.add(vp);
//            addView(baseImageView, layoutParams);
//        }

        BaseImageView firstIcon = findViewById(R.id.first_icon);
        BaseImageView secondIcon = findViewById(R.id.second_icon);
        BaseImageView thirdIcon = findViewById(R.id.third_icon);
        addViewToList(firstIcon);
        addViewToList(secondIcon);
        addViewToList(thirdIcon);
        mTvCount =  findViewById(R.id.tv_count);
        mTvCount.setVisibility(GONE);
    }

    private void addViewToList(BaseImageView baseImageView) {
        baseImageView.setVisibility(GONE);
        VH vp = new VH(baseImageView);
        mBaseImageViewList.add(vp);
    }

    public void updateAllView() {
        MyLog.d(TAG, "updateAllView");
        for (int i = 0; i < mBaseImageViewList.size(); i++) {
            mBaseImageViewList.get(i).mBaseImageView.setVisibility(GONE);
        }

        for (int i = 0; i < mWaitInfoModelList.size() && i < mBaseImageViewList.size(); i++) {
            //MyLog.d(TAG, "i=" + i);
            UserInfoModel userInfoModel = mWaitInfoModelList.get(i).getUserInfo();
            VH vp = mBaseImageViewList.get(i);
            vp.mGrabPlayerInfoModel = userInfoModel;
            vp.mBaseImageView.setVisibility(VISIBLE);
            AvatarUtils.loadAvatarByUrl(vp.mBaseImageView, AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(1))
                    .setBorderColor(U.getColor(R.color.white))
                    .build());
        }

        if (mWaitInfoModelList.size() > 3) {
            mTvCount.setVisibility(VISIBLE);
            mTvCount.setText(mWaitInfoModelList.size() + "");
        } else {
            mTvCount.setVisibility(GONE);
        }
    }

    public static class VH {
        BaseImageView mBaseImageView;
        UserInfoModel mGrabPlayerInfoModel;

        public VH(BaseImageView baseImageView) {
            mBaseImageView = baseImageView;
            mBaseImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mGrabPlayerInfoModel != null) {
                        ShowPersonCardEvent event = new ShowPersonCardEvent(mGrabPlayerInfoModel.getUserId());
                        EventBus.getDefault().post(event);
                    }
                }
            });
        }
    }
}
