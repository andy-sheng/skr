package com.module.playways.grab.room.bottom;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GrabRoomType;
import com.module.playways.BaseRoomData;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.dynamicmsg.DynamicMsgView;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.room.room.view.BottomContainerView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class GrabBottomContainerView extends BottomContainerView {

    ExImageView mIvRoomManage;

    ExTextView mQuickBtn;

    View mSpeakingDotAnimationView;

    PopupWindow mDynamicMsgPopWindow;    //动态表情弹出面板

    DynamicMsgView mDynamicMsgView;

    GrabRoomData mGrabRoomData;

    RelativeLayout mEmojiArea;

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
        mEmojiArea = this.findViewById(R.id.emoji_area);
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
        mQuickBtn = (ExTextView) super.mQuickBtn;

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
                    mDynamicMsgPopWindow.setAnimationStyle(R.style.MyPopupWindow_anim_style);
                    // 去除动画
//                      mDynamicMsgPopWindow.setAnimationStyle(R.style.anim_quickmsg_dialog);
                    mDynamicMsgPopWindow.setBackgroundDrawable(new BitmapDrawable());
                    mDynamicMsgPopWindow.setOutsideTouchable(true);
                }
                if (!mDynamicMsgPopWindow.isShowing()) {
                    int l[] = new int[2];
                    mQuickBtn.getLocationInWindow(l);
                    mDynamicMsgPopWindow.showAtLocation(mQuickBtn, Gravity.START | Gravity.TOP, l[0], l[1] - h - U.getDisplayUtils().dip2px(5));
                } else {
                    mDynamicMsgPopWindow.dismiss();
                }
            }
        });

        mEmoji2Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.showGiftPanel();
                }
            }
        });
    }

    public void setOpVisible(boolean visible) {
        mEmojiArea.setVisibility(visible ? VISIBLE : INVISIBLE);
        mEmoji2Btn.setEnabled(visible);
        mEmoji1Btn.setEnabled(visible);
        mIvRoomManage.setEnabled(visible);
    }

    protected void onQuickMsgDialogShow(boolean show) {
        if (show) {
            Drawable drawable = U.getDrawable(R.drawable.kuaijiehuifu_fang);
            drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
            mQuickBtn.setCompoundDrawables(null, null,
                    drawable, null);
        } else {
            Drawable drawable = U.getDrawable(R.drawable.kuaijiehuifu_shou);
            drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
            mQuickBtn.setCompoundDrawables(null, null,
                    drawable, null);
        }
    }

    public void setRoomData(BaseRoomData roomData) {
        super.setRoomData(roomData);
        if (mRoomData instanceof GrabRoomData) {
            mGrabRoomData = (GrabRoomData) mRoomData;
            if (mGrabRoomData.getOwnerId() != 0) {
                if (mGrabRoomData.isOwner()) {
                    //是房主
                    adjustUi(true, true);
                } else {
                    //不是一唱到底房主
                    adjustUi(false, true);
                }
            } else {
                adjustUi(false, false);
            }

            if (mGrabRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_GUIDE) {
                mEmoji2Btn.setVisibility(GONE);
            }
        }
    }

    void adjustUi(boolean grabOwner, boolean isOwnerRoom) {
        if (grabOwner) {
            mIvRoomManage.setVisibility(VISIBLE);
            mIvRoomManage.setImageResource(R.drawable.ycdd_fangzhu);
//            mQuickBtn.setImageResource(R.drawable.fz_anzhushuohua);
//            mQuickBtn.setEnabled(true);
//            mQuickBtn.setOnClickListener(null);
//
//            mShowInputContainerBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, U.getDimension(R.dimen.textsize_13_dp));
//
//            mQuickBtn.setOnTouchListener(new OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (!mQuickBtn.isEnabled()) {
//                        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
//                            U.getToastUtil().showShort("演唱阶段不能说话哦");
//                        }
//                        return true;
//                    }
//                    switch (event.getActionMasked()) {
//                        case MotionEvent.ACTION_DOWN: {
//                            mQuickBtn.setImageResource(R.drawable.fz_shuohuazhong);
//                            mSpeakingDotAnimationView.setVisibility(VISIBLE);
//                            mShowInputContainerBtn.setText("");
//                            EventBus.getDefault().post(new GrabSpeakingControlEvent(true));
//                        }
//                        break;
//                        case MotionEvent.ACTION_CANCEL:
//                        case MotionEvent.ACTION_UP: {
//                            mQuickBtn.setImageResource(R.drawable.fz_anzhushuohua);
//                            mSpeakingDotAnimationView.setVisibility(GONE);
//                            mShowInputContainerBtn.setText("夸赞是一种美德");
//                            EventBus.getDefault().post(new GrabSpeakingControlEvent(false));
//                        }
//                        break;
//                    }
//                    return true;
//                }
//            });
        } else {
            if (isOwnerRoom) {
                mIvRoomManage.setVisibility(VISIBLE);
                mIvRoomManage.setImageResource(R.drawable.ycdd_diange);
            } else {
                mIvRoomManage.setVisibility(GONE);
            }
            Drawable drawable = U.getDrawable(R.drawable.kuaijiehuifu_shou);
            drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
            mQuickBtn.setCompoundDrawables(null, null,
                    drawable, null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabRoundStatusChangeEvent event) {
        //MyLog.d("GrabBottomContainerView","onEvent" + " event=" + event);
//        GrabRoundInfoModel now = event.roundInfo;
//        if (now != null && now.isSingStatus() && mGrabRoomData.isOwner()) {
//            if (mGrabRoomData.isSpeaking() && !now.singBySelf()) {
//                U.getToastUtil().showShort("有人上麦了,暂时不能说话哦", 0, Gravity.CENTER);
//            }
//            mQuickBtn.setImageResource(R.drawable.fz_anzhushuohua_b);
//            mQuickBtn.setEnabled(false);
//            mSpeakingDotAnimationView.setVisibility(GONE);
//            mShowInputContainerBtn.setText("夸赞是一种美德");
//            EventBus.getDefault().post(new GrabSpeakingControlEvent(false));
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabRoundChangeEvent event) {
//        if (mGrabRoomData != null && mGrabRoomData.isOwner()) {
//            mQuickBtn.setEnabled(true);
//            if (mGrabRoomData.isSpeaking()) {
//                // 正在说话，就算了
//            } else {
//                mQuickBtn.setImageResource(R.drawable.fz_anzhushuohua);
//            }
//        }
    }

    @Override
    public void dismissPopWindow() {
        super.dismissPopWindow();
        if (mDynamicMsgPopWindow != null) {
            mDynamicMsgPopWindow.dismiss();
        }
    }
}
