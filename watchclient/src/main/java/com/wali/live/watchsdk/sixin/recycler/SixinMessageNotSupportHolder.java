package com.wali.live.watchsdk.sixin.recycler;

import android.view.View;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.sixin.recycler.adapter.SixinMessageAdapter;

/**
 * Created by lan on 16-5-20.
 *
 * @notice 太多类型不支持，这里加个不支持的holder
 */
public class SixinMessageNotSupportHolder extends SixinMessageHolder {
    public SixinMessageNotSupportHolder(View itemView, SixinMessageAdapter adapter) {
        super(itemView, adapter);
    }

    @Override
    protected void bindView() {
        setTimestamp();

        setAvatar();
        setCertification();

        setBubbleOnType();
    }

    @Override
    protected void setBubbleOnType() {
        bindTextMessage(itemView.getResources().getString(R.string.message_not_supported));
    }
}