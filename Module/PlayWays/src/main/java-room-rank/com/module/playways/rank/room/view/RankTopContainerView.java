package com.module.playways.rank.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.common.view.ex.ExImageView;
import com.module.playways.RoomData;
import com.module.playways.rank.room.score.bar.EnergySlotView;
import com.module.rank.R;

public class RankTopContainerView extends RelativeLayout {
    ExImageView mMoreBtn;
    MoreOpView mMoreOpView;
    ExImageView mIvLed;
    ExImageView mIvLeft;
    ExImageView mIvCenter;
    ExImageView mIvRignt;
    EnergySlotView mEnergySlotView;

    TopContainerView.Listener mListener;
    RoomData mRoomData;

    enum LightState{
        BAO, MIE
    }

    public RankTopContainerView(Context context) {
        super(context);
        init();
    }

    public RankTopContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setListener(TopContainerView.Listener l) {
        mListener = l;
    }

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

    private void init() {
        inflate(getContext(), R.layout.rank_top_container_view, this);
        mMoreBtn = this.findViewById(R.id.more_btn);
        mIvLed = (ExImageView) findViewById(R.id.iv_led);
        mIvLeft = (ExImageView) findViewById(R.id.iv_left);
        mIvCenter = (ExImageView) findViewById(R.id.iv_center);
        mIvRignt = (ExImageView) findViewById(R.id.iv_rignt);
        mEnergySlotView = (EnergySlotView)findViewById(R.id.energy_slot_view);

        mMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreOpView == null) {
                    mMoreOpView = new MoreOpView(getContext());
                    mMoreOpView.setListener(new MoreOpView.Listener() {
                        @Override
                        public void onClostBtnClick() {
                            if (mListener != null) {
                                mListener.closeBtnClick();
                            }
                        }

                        @Override
                        public void onVoiceChange(boolean voiceOpen) {
                            // 打开或者关闭声音 只是不听别人的声音
                            if (mListener != null) {
                                mListener.onVoiceChange(voiceOpen);
                            }
                        }
                    });
                    mMoreOpView.setRoomData(mRoomData);
                }
                mMoreOpView.showAt(mMoreBtn);
            }
        });
    }

    /**
     * @param seq  轮次信息
     * @param lightState  爆灭情况
     */
    public void updateLight(int seq, LightState lightState){

    }

    public EnergySlotView getEnergySlotView(){
        return mEnergySlotView;
    }
}
