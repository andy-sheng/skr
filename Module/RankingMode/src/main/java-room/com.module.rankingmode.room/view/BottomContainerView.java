package com.module.rankingmode.room.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.rankingmode.R;
import com.module.rankingmode.room.event.InputBoardEvent;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.quickmsg.QuickMsgView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BottomContainerView extends RelativeLayout {

    Listener mBottomContainerListener;

    ExImageView mQuickBtn;
    ExImageView mShowInputContainerBtn;
    ExImageView mEmoji2Btn;
    ExImageView mEmoji1Btn;

    PopupWindow mQuickMsgPopWindow;
    private RoomData mRoomData;

    public BottomContainerView(Context context) {
        super(context);
        init();
    }

    public BottomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.bottom_container_view_layout, this);

        mQuickBtn = (ExImageView) this.findViewById(R.id.quick_btn);
        mShowInputContainerBtn = (ExImageView) this.findViewById(R.id.show_input_container_btn);
        mEmoji2Btn = (ExImageView) this.findViewById(R.id.emoji2_btn);
        mEmoji1Btn = (ExImageView) this.findViewById(R.id.emoji1_btn);

        mShowInputContainerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.showInputBtnClick();
                }
            }
        });

        mQuickBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int w = U.getDisplayUtils().dip2px(343);
                int h = U.getDisplayUtils().dip2px(146);
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
                    mQuickMsgPopWindow.showAtLocation(mQuickBtn, Gravity.START | Gravity.TOP, l[0], l[1] - h);
                }
            }
        });
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

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

    public interface Listener {
        void showInputBtnClick();
    }

}
