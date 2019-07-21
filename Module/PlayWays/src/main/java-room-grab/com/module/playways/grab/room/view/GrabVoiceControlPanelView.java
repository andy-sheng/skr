package com.module.playways.grab.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.common.utils.U;
import com.kyleduo.switchbutton.SwitchButton;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.view.VoiceControlPanelView;
import com.module.playways.R;

public class GrabVoiceControlPanelView extends VoiceControlPanelView {
    public final String TAG = "VoiceControlPanelView";

    LinearLayout mLlSwitchContainer;

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
        mLlSwitchContainer = findViewById(R.id.ll_switch_container);
        mSbAcc = findViewById(R.id.sb_acc);

        mSbAcc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GrabRoundInfoModel infoModel = mGrabRoomData.getRealRoundInfo();
                if (infoModel != null && infoModel.singBySelf()) {
                    U.getToastUtil().showShort("你的演唱阶段无法修改演唱模式");
                    if (mGrabRoomData != null) {
                        mSbAcc.setChecked(!mGrabRoomData.isAccEnable());
                    }
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

    public void setRoomData(GrabRoomData modelBaseRoomData) {
        mGrabRoomData = modelBaseRoomData;
    }
}
