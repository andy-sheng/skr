package com.component.busilib.callback;

import android.content.Context;
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

    public EmptyCallback(int resid, String text) {
        this.resid = resid;
        this.text = text;
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

        mCallbackImg.setBackgroundResource(resid);
        mCallbackTxt.setText(text);
    }
}
