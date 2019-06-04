package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.log.MyLog;
import com.module.playways.R;
import com.module.playways.room.gift.event.UpdateMeiliEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

// 展示魅力值的view
public class CharmsView extends RelativeLayout {

    public final static String TAG = "CharmsView";

    ImageView mCharmIv;
    TextView mCharmTv;

    int mUserID;
    int mCharmValue;

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

    public void bindData(int userID) {
        this.mUserID = userID;
        this.mCharmValue = 0;
        setVisibility(VISIBLE);
        mCharmTv.setTextColor(Color.parseColor("#3B4E79"));
        updateUi();
    }

    public void bindData(int userID, int color) {
        this.mUserID = userID;
        this.mCharmValue = 0;
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
        MyLog.d(TAG, "onEvent" + " event=" + event);
        MyLog.d(TAG, "onEvent" + " mUserID=" + mUserID);
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
