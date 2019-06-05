package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.log.MyLog;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.module.playways.room.gift.event.UpdateMeiliEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

// 展示魅力值的view
public class CharmsView extends RelativeLayout {

    ImageView mCharmIv;
    TextView mCharmTv;

    int mUserID;
    int mCharmValue;
    GrabRoomData grabRoomData;

    public CharmsView(Context context) {
        super(context);
        init();
    }

    public CharmsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CharmsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_charms_view_layout, this);

        mCharmIv = (ImageView) findViewById(R.id.charm_iv);
        mCharmTv = (TextView) findViewById(R.id.charm_tv);
    }

    public void bindData(GrabRoomData grabRoomData, int userID) {
        bindData(grabRoomData, userID, Color.parseColor("#3B4E79"));
    }

    public void bindData(GrabRoomData grabRoomData, int userID, int color) {
        this.grabRoomData = grabRoomData;
        this.mUserID = userID;

        this.mCharmValue = 0;
        GrabRoundInfoModel roundInfoModel = grabRoomData.getRealRoundInfo();
        if (roundInfoModel != null && roundInfoModel.isPKRound()) {
            for (SPkRoundInfoModel sPkRoundInfoModel : roundInfoModel.getsPkRoundInfoModels()) {
                if (sPkRoundInfoModel.getUserID() == userID) {
                    this.mCharmValue = sPkRoundInfoModel.getMeiliTotal();
                }
            }
        }
        setVisibility(VISIBLE);
        mCharmTv.setTextColor(color);
        updateUi();
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
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateMeiliEvent event) {
        if (event.userID == mUserID) {
            if (event.value >= 0) {
                setVisibility(VISIBLE);
                mCharmValue = event.value;
                updateUi();
            } else {
                setVisibility(GONE);
            }
        }
    }

    private void updateUi() {
        mCharmTv.setText("魅力" + mCharmValue);
    }
}
