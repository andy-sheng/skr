package com.debugcore;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.R;

public class ShowMsgView extends RelativeLayout {

    TextView mContentTv;

    public ShowMsgView(Context context) {
        super(context);
        init();
    }

    public ShowMsgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShowMsgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.debug_msg_show_view,this);
        mContentTv = (TextView)this.findViewById(R.id.content_tv);
    }

    public void bindData(String str){
        mContentTv.setText(str);
    }
}
