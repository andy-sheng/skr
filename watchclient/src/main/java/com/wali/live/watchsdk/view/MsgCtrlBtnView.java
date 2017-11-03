package com.wali.live.watchsdk.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.wali.live.watchsdk.R;

/**
 * Created by yangli on 16-8-29.
 *
 * @module 私信消息按钮
 */
public class MsgCtrlBtnView extends FrameLayout {
    private static final String TAG = "MsgCtrlBtnView";

    private int mUnreadCnt = 0;

    private ImageView mNormalMsgBtn;
    private View mRichMsgBtn;
    private View AlertRedIcon;
    private TextView mUnreadNum;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    public MsgCtrlBtnView(Context context) {
        this(context, null);
    }

    public MsgCtrlBtnView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MsgCtrlBtnView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.msg_ctrl_btn_view, this);
        mNormalMsgBtn = $(R.id.normal_message_btn);
        mRichMsgBtn = $(R.id.rich_message_btn);
        AlertRedIcon = $(R.id.alert_red_icon);
        mUnreadNum = $(R.id.message_unread_num_icon);
        mNormalMsgBtn.setImageDrawable(getResources().getDrawable(R.drawable.live_icon_letter_btn));
    }

    public int getMsgUnreadCnt() {
        return mUnreadCnt;
    }

    public void setMsgUnreadCnt(int msgUnreadCnt) {
        MyLog.d(TAG, "setMsgUnreadCnt msgUnreadCnt=" + msgUnreadCnt);
        mUnreadCnt = msgUnreadCnt;
        if (mUnreadCnt > 0) {
            String numAsString = mUnreadCnt > 99 ? "..." : String.valueOf(mUnreadCnt);
            mUnreadNum.setVisibility(View.VISIBLE);
            AlertRedIcon.setVisibility(View.GONE);
            AlertRedIcon.setBackground(null);

            mUnreadNum.setText(numAsString);
            mUnreadNum.setBackground(GlobalData.app().getResources().getDrawable(R.drawable.little_red_dot_number));
        } else {
            mUnreadNum.setVisibility(View.GONE);
            AlertRedIcon.setVisibility(View.GONE);
            AlertRedIcon.setBackground(null);

            mUnreadNum.setText("");
            mUnreadNum.setBackground(null);
        }
    }

    public void setMorePanelModeOn() {
        mNormalMsgBtn.setVisibility(View.GONE);
        mRichMsgBtn.setVisibility(View.VISIBLE);
    }
}
