package com.module.msg.custom;

import android.content.Context;
import android.view.View;

import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.adapter.MessageListAdapter;

public class MyMessageListAdapter extends MessageListAdapter {
    public MyMessageListAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindView(View v, int position, UIMessage data) {
        super.bindView(v, position, data);
    }
}
