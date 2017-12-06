package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wali.live.watchsdk.R;

public class WatchMenuIconView extends LinearLayout {
    private static final String TAG = "MoreControlBtnView";

    private int mUnreadCnt = 0;
    private ImageView mMoreIv;
    private ImageView mUnreadNum;

    public WatchMenuIconView(Context context) {
        this(context, null);
    }

    public WatchMenuIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchMenuIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.watch_menu_icon_view, this);
        mMoreIv = (ImageView) this.findViewById(R.id.more_iv);
        mUnreadNum = (ImageView) this.findViewById(R.id.message_unread_num_icon);
        mMoreIv.setImageDrawable(getResources().getDrawable(R.drawable.live_icon_menu_btn));
    }

    public void changeIconStatus(boolean isOpen) {
        if (isOpen) {
            mMoreIv.setImageResource(R.drawable.live_icon_menu_opened_btn);
        } else {
            mMoreIv.setImageResource(R.drawable.live_icon_menu_btn);
        }
    }

    public int getMsgUnreadCnt() {
        return mUnreadCnt;
    }

    public void setMsgUnreadCnt(int msgUnreadCnt) {
        mUnreadCnt = msgUnreadCnt;
        if (mUnreadCnt > 0) {
            mUnreadNum.setVisibility(View.VISIBLE);
        } else {
            mUnreadNum.setVisibility(View.GONE);
        }
    }
}
