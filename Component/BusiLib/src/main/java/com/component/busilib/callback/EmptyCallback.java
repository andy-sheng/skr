package com.component.busilib.callback;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.component.busilib.R;
import com.kingja.loadsir.callback.Callback;

public class EmptyCallback extends Callback {

    ImageView mCallbackImg;
    TextView mCallbackTxt;

    int resid;
    String text;
    String textColor;

    public EmptyCallback(int resid, String text, String textColor) {
        this.resid = resid;
        this.text = text;
        this.textColor = textColor;
    }

    @Override
    protected int onCreateView() {
        return R.layout.loadsir_callback_layout;
    }

    @Override
    protected void onViewCreate(Context context, View view) {
        super.onViewCreate(context, view);
        mCallbackImg = (ImageView) view.findViewById(R.id.callback_img);
        mCallbackTxt = (TextView) view.findViewById(R.id.callback_txt);

        if (!TextUtils.isEmpty(textColor)) {
            mCallbackTxt.setTextColor(Color.parseColor(textColor));
        }
        mCallbackImg.setBackgroundResource(resid);
        mCallbackTxt.setText(text);
    }
}
