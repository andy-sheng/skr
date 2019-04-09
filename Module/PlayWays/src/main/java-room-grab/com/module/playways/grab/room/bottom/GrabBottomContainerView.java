package com.module.playways.grab.room.bottom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.rank.room.view.BottomContainerView;
import com.module.rank.R;

public class GrabBottomContainerView extends BottomContainerView {

    View mIvRoomManage;

    public GrabBottomContainerView(Context context) {
        super(context);
    }

    public GrabBottomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayout() {
        return R.layout.grab_bottom_container_view_layout;
    }

    protected void init() {
        super.init();
        mIvRoomManage = this.findViewById(R.id.iv_room_manage);
        mIvRoomManage.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.clickRoomManagerBtn();
                }
            }
        });
    }

    public void setRoomData(BaseRoomData roomData) {
        super.setRoomData(roomData);
        if (mRoomData instanceof GrabRoomData) {
            GrabRoomData grabRoomData = (GrabRoomData) mRoomData;
            if (grabRoomData.isOwner()) {
                //是房主
                adjustUi(true);
            } else {
                //不是一唱到底房主
                adjustUi(false);
            }
        }
    }

    void adjustUi(boolean grabOwner) {
        if (grabOwner) {
            mIvRoomManage.setVisibility(VISIBLE);
            LayoutParams lp = (LayoutParams) mEmoji2Btn.getLayoutParams();
            lp.addRule(RelativeLayout.LEFT_OF, mIvRoomManage.getId());
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            mEmoji2Btn.setLayoutParams(lp);
        } else {
            mIvRoomManage.setVisibility(GONE);
            LayoutParams lp = (LayoutParams) mEmoji2Btn.getLayoutParams();
            lp.addRule(RelativeLayout.LEFT_OF, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mEmoji2Btn.setLayoutParams(lp);
        }
    }
}
