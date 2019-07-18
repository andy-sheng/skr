package com.module.playways.room.room.bottom;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import com.common.core.account.UserAccountManager;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.module.playways.room.room.quickmsg.QuickMsgView;
import com.module.playways.room.room.view.BottomContainerView;
import com.module.playways.R;
import com.zq.live.proto.Room.SpecialEmojiMsgType;

import java.util.HashMap;

public class RankBottomContainerView extends BottomContainerView {

    View mQuickBtn;
    PopupWindow mQuickMsgPopWindow;  //快捷词弹出面板、
    ExTextView mShowInputContainerBtn;

    public RankBottomContainerView(Context context) {
        super(context);
    }

    public RankBottomContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayout() {
        return R.layout.bottom_container_view_layout;
    }

    @Override
    protected void init() {
        super.init();
        mQuickBtn = this.findViewById(R.id.quick_btn);
        mShowInputContainerBtn = this.findViewById(R.id.show_input_container_btn);

        mShowInputContainerBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.showInputBtnClick();
                }
            }
        });

        mEmoji2Btn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 发送动态表情，爱心
                sendSpecialEmojiMsg(SpecialEmojiMsgType.SP_EMOJI_TYPE_LIKE, "送出爱心");
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
//                    mQuickMsgPopWindow.setFocusable(false);
                    // 去除动画
                    mQuickMsgPopWindow.setAnimationStyle(R.style.MyPopupWindow_anim_style);
                    mQuickMsgPopWindow.setBackgroundDrawable(new BitmapDrawable());
                    mQuickMsgPopWindow.setOutsideTouchable(true);
                    mQuickMsgPopWindow.setFocusable(true);
                    mQuickMsgPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            onQuickMsgDialogShow(false);
                        }
                    });
                }
                if (!mQuickMsgPopWindow.isShowing()) {
                    int l[] = new int[2];
                    mQuickBtn.getLocationInWindow(l);
                    mQuickMsgPopWindow.showAtLocation(mQuickBtn, Gravity.START | Gravity.TOP, l[0], l[1] - h - U.getDisplayUtils().dip2px(5));
                    onQuickMsgDialogShow(true);
                } else {
                    mQuickMsgPopWindow.dismiss();
                }
            }
        });
    }

    @Override
    public void dismissPopWindow() {
        super.dismissPopWindow();
        if (mQuickMsgPopWindow != null) {
            mQuickMsgPopWindow.dismiss();
        }
    }
}
