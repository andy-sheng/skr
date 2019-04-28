package com.module.playways.voice.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.playways.BaseRoomData;
import com.module.playways.room.room.event.InputBoardEvent;
import com.module.playways.room.room.quickmsg.QuickMsgView;
import com.module.playways.R;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VoiceBottomContainerView extends RelativeLayout {

    static final int CLEAR_CONTINUE_FLAG = 11;

    Listener mBottomContainerListener;

    ExTextView mQuickBtn;
    ExTextView mShowInputContainerBtn;

    PopupWindow mQuickMsgPopWindow;
    private BaseRoomData mRoomData;

    SpecialEmojiMsgType mLastSendType = null;
    int mContinueCount = 1;
    long mContinueId = 0L;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == CLEAR_CONTINUE_FLAG) {
                mLastSendType = null;
                mContinueCount = 1;
            }
        }
    };

    public VoiceBottomContainerView(Context context) {
        super(context);
        init();
    }

    public VoiceBottomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.voice_bottom_container_view_layout, this);

        mQuickBtn = (ExTextView) this.findViewById(R.id.quick_btn);
        mShowInputContainerBtn = (ExTextView) this.findViewById(R.id.show_input_container_btn);

        mShowInputContainerBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.showInputBtnClick();
                }
            }
        });

        mQuickBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                int w = U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(32);
                int h = U.getDisplayUtils().dip2px(172);
                if (mQuickMsgPopWindow == null) {
                    QuickMsgView quickMsgView = new QuickMsgView(getContext());
                    quickMsgView.setRoomData(mRoomData);
                    quickMsgView.setListener(new QuickMsgView.Listener() {
                        @Override
                        public void onSendMsgOver() {
                            if (mQuickMsgPopWindow != null) {
                                mQuickMsgPopWindow.dismiss();
                            }
                        }
                    });
                    mQuickMsgPopWindow = new PopupWindow(quickMsgView, w, h);
                    mQuickMsgPopWindow.setFocusable(false);
                    // 去除动画
//                    mQuickMsgPopWindow.setAnimationStyle(R.style.anim_quickmsg_dialog);
                    mQuickMsgPopWindow.setBackgroundDrawable(new BitmapDrawable());
                    mQuickMsgPopWindow.setOutsideTouchable(true);
                }
                if (!mQuickMsgPopWindow.isShowing()) {
                    int l[] = new int[2];
                    mQuickBtn.getLocationInWindow(l);
                    mQuickMsgPopWindow.showAtLocation(mQuickBtn, Gravity.START | Gravity.TOP, l[0], l[1] - h - U.getDisplayUtils().dip2px(5));
                }
            }
        });
    }

    public void dismissPopWindow() {
        if (mQuickMsgPopWindow != null) {
            mQuickMsgPopWindow.dismiss();
        }
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
        MyLog.d("BottomContainerView", "onDetachedFromWindow");
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        dismissPopWindow();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(InputBoardEvent event) {
        if (event.show) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }

    public void setListener(Listener l) {
        mBottomContainerListener = l;
    }

    public void setRoomData(BaseRoomData roomData) {
        mRoomData = roomData;
    }

    public interface Listener {
        void showInputBtnClick();
    }

}
