package com.module.playways.rank.room.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.module.playways.RoomData;
import com.module.playways.rank.room.model.RoomDataUtils;
import com.module.rank.R;

public class MoreOpView extends RelativeLayout {

    LinearLayout mMenuContainer;
    RelativeLayout mQuitBtnContainer;
    ExTextView mQuitBtn;
    RelativeLayout mVoiceControlBtnContainer;
    ExTextView mVoiceControlBtn;
    View mDivideLine;
    PopupWindow mPopupWindow;

    Listener mListener;

    RoomData mRoomData;

    boolean mVoiceOpen = true;

    public MoreOpView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.more_op_view_layout, this);
        setBackgroundResource(R.drawable.tuichufangjian);

        mMenuContainer = (LinearLayout) this.findViewById(R.id.menu_container);
        mQuitBtnContainer = (RelativeLayout) this.findViewById(R.id.quit_btn_container);
        mQuitBtn = (ExTextView) this.findViewById(R.id.quit_btn);
        mVoiceControlBtnContainer = (RelativeLayout) this.findViewById(R.id.voice_control_btn_container);
        mVoiceControlBtn = (ExTextView) this.findViewById(R.id.voice_control_btn);
        mDivideLine = this.findViewById(R.id.divide_line);

        mQuitBtnContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClostBtnClick();
                }
                mPopupWindow.dismiss();
            }
        });

        mVoiceControlBtnContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mVoiceOpen = !mVoiceOpen;
                    mListener.onVoiceChange(mVoiceOpen);
                    if (mVoiceOpen) {
                        mVoiceControlBtn.setText("关闭声音");
                        Drawable drawableLeft = getResources().getDrawable(
                                R.drawable.soundoff);
                        mVoiceControlBtn.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                                null, null, null);
                    } else {
                        mVoiceControlBtn.setText("打开声音");
                        Drawable drawableLeft = getResources().getDrawable(
                                R.drawable.soundon);
                        mVoiceControlBtn.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,
                                null, null, null);
                    }
                }
                mPopupWindow.dismiss();
            }
        });
    }

    public void showAt(View view) {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(this, U.getDisplayUtils().dip2px(118), ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopupWindow.setOutsideTouchable(true);
        }
        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAsDropDown(view, -U.getDisplayUtils().dip2px(2), U.getDisplayUtils().dip2px(5));
        }
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            mVoiceControlBtnContainer.setVisibility(GONE);
            mDivideLine.setVisibility(GONE);
        } else {
            mVoiceControlBtnContainer.setVisibility(VISIBLE);
            mDivideLine.setVisibility(VISIBLE);
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

    public interface Listener {
        void onClostBtnClick();

        void onVoiceChange(boolean voiceOpen);
    }
}
