package com.module.playways.grab.room.bottom;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabSpeakingControlEvent;
import com.module.playways.grab.room.dynamicmsg.DynamicMsgView;
import com.module.playways.room.room.view.BottomContainerView;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;

public class GrabBottomContainerView extends BottomContainerView {

    View mIvRoomManage;

    ExImageView mQuickBtn;

    View mSpeakingDotAnimationView;

    PopupWindow mDynamicMsgPopWindow;    //动态表情弹出面板

    DynamicMsgView mDynamicMsgView;

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
        mSpeakingDotAnimationView = this.findViewById(R.id.speaking_dot_animation_view);
        mQuickBtn = (ExImageView) super.mQuickBtn;

        mEmoji1Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 动态表情按钮
                int w = U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(32);
                int h = U.getDisplayUtils().dip2px(72);
                if (mDynamicMsgView == null) {
                    mDynamicMsgView = new DynamicMsgView(getContext());
                    mDynamicMsgView.setData(mRoomData);
                    mDynamicMsgView.setListener(new DynamicMsgView.Listener() {
                        @Override
                        public void onSendMsgOver() {
                            if (mDynamicMsgPopWindow != null) {
                                mDynamicMsgPopWindow.dismiss();
                            }
                        }
                    });
                } else {
                    mDynamicMsgView.loadEmoji();
                }
                if (mDynamicMsgPopWindow == null) {
                    mDynamicMsgPopWindow = new PopupWindow(mDynamicMsgView, w, h);
                    mDynamicMsgPopWindow.setFocusable(true);
                    // 去除动画
//                      mDynamicMsgPopWindow.setAnimationStyle(R.style.anim_quickmsg_dialog);
                    mDynamicMsgPopWindow.setBackgroundDrawable(new BitmapDrawable());
                    mDynamicMsgPopWindow.setOutsideTouchable(true);
                }
                if (!mDynamicMsgPopWindow.isShowing()) {
                    int l[] = new int[2];
                    mQuickBtn.getLocationInWindow(l);
                    mDynamicMsgPopWindow.showAtLocation(mQuickBtn, Gravity.START | Gravity.TOP, l[0], l[1] - h - U.getDisplayUtils().dip2px(5));
                }else {
                    mDynamicMsgPopWindow.dismiss();
                }
            }
        });
    }

    protected void onQuickMsgDialogShow(boolean show) {
        if (show) {
            mQuickBtn.setImageResource(R.drawable.ycdd_kuaijie_anxia);
        } else {
            mQuickBtn.setImageResource(R.drawable.ycdd_kuaijie);
        }
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
            mQuickBtn.setImageResource(R.drawable.fz_anzhushuohua);
            mQuickBtn.setOnClickListener(null);
            mQuickBtn.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            mQuickBtn.setImageResource(R.drawable.fz_shuohuazhong);
                            mSpeakingDotAnimationView.setVisibility(VISIBLE);
                            mShowInputContainerBtn.setText("");
                            EventBus.getDefault().post(new GrabSpeakingControlEvent(true));
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            mQuickBtn.setImageResource(R.drawable.fz_anzhushuohua);
                            mSpeakingDotAnimationView.setVisibility(GONE);
                            mShowInputContainerBtn.setText("夸赞是一种美德");
                            EventBus.getDefault().post(new GrabSpeakingControlEvent(false));
                            break;
                    }
                    return true;
                }
            });
        } else {
            mIvRoomManage.setVisibility(GONE);
            LayoutParams lp = (LayoutParams) mEmoji2Btn.getLayoutParams();
            lp.addRule(RelativeLayout.LEFT_OF, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mEmoji2Btn.setLayoutParams(lp);
            mQuickBtn.setImageResource(R.drawable.ycdd_kuaijie);
        }
    }

    @Override
    public void dismissPopWindow() {
        super.dismissPopWindow();
        if (mDynamicMsgPopWindow != null) {
            mDynamicMsgPopWindow.dismiss();
        }
    }
}
