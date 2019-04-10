package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.kyleduo.switchbutton.SwitchButton;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.view.VoiceControlPanelView;
import com.module.rank.R;

public class GrabVoiceControlPanelView extends VoiceControlPanelView {
    public final static String TAG = "VoiceControlPanelView";

    LinearLayout mLlSwitchContainer;

    ExTextView mTvYuansheng;
    ExTextView mTvKtv;
    ExTextView mTvLiuxing;
    ExTextView mTvYaogun;
    ExTextView mTvKongling;

    SwitchButton mSbAcc;

    GrabRoomData mGrabRoomData;

    public GrabVoiceControlPanelView(Context context) {
        super(context);
    }

    public GrabVoiceControlPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayout() {
        return R.layout.grab_voice_control_panel_layout;
    }

    protected int getMarginLeft() {
        return U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(30 + 24) - U.getDisplayUtils().dip2px(44 * 5);
    }

    @Override
    public void init(Context context, AttributeSet attrs) {
        super.init(context, attrs);
        mLlSwitchContainer = (LinearLayout) findViewById(R.id.ll_switch_container);
        mSbAcc = (SwitchButton) findViewById(R.id.sb_acc);
        mTvYuansheng = (ExTextView) findViewById(R.id.tv_yuansheng);
        mTvKtv = (ExTextView) findViewById(R.id.tv_ktv);
        mTvLiuxing = (ExTextView) findViewById(R.id.tv_liuxing);
        mTvYaogun = (ExTextView) findViewById(R.id.tv_yaogun);
        mTvKongling = (ExTextView) findViewById(R.id.tv_kongling);

        int marginLeft = U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(30 + 24) - U.getDisplayUtils().dip2px(44 * 5);
        marginLeft = marginLeft / 6;

        setTextMarginLeft(mTvKtv, marginLeft);
        setTextMarginLeft(mTvLiuxing, marginLeft);
        setTextMarginLeft(mTvYaogun, marginLeft);
        setTextMarginLeft(mTvKongling, marginLeft);

        mSbAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
                if (infoModel != null && infoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                    U.getToastUtil().showShort("你的演唱阶段无法修改演唱模式");
                    mSbAcc.setChecked(!isChecked);
                    return;
                }
                if (mGrabRoomData != null) {
                    mGrabRoomData.setAccEnable(!isChecked);
                }
            }
        });
    }

    @Override
    public void bindData() {
        super.bindData();
        if (mGrabRoomData.isAccEnable()) {
            mSbAcc.setChecked(false);
        } else {
            mSbAcc.setChecked(true);
        }
    }

    //    public void setSingerId(long singUid) {
//        if (singUid == MyUserInfoManager.getInstance().getUid()) {
//            setAccSwitchBtnStatus(false);
//        } else {
//            setAccSwitchBtnStatus(true);
//        }
//    }
//
//    private void setAccSwitchBtnStatus(boolean visibale) {
//        if (visibale) {
//            mLlSwitchContainer.setVisibility(VISIBLE);
//        } else {
//            mLlSwitchContainer.setVisibility(GONE);
//        }
//    }

    public void setRoomData(GrabRoomData modelBaseRoomData) {
        mGrabRoomData = modelBaseRoomData;
    }

    private void setTextMarginLeft(View view, int marginLeft) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMargins(marginLeft, 0, 0, 0);
    }
}
