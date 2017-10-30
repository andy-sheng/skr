package com.wali.live.watchsdk.sixin.recycler;

import android.view.View;

import com.base.view.MLTextView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.sixin.constant.SixinConstants;
import com.wali.live.watchsdk.sixin.message.SixinMessageModel;

/**
 * Created by lan on 16-5-20.
 */
public class SixinMessageDefaultHolder extends BaseHolder<SixinMessageModel> {
    private MLTextView mTimestampTv;

    private SixinMessageAdapter mAdapter;

    public SixinMessageDefaultHolder(View itemView, SixinMessageAdapter adapter) {
        super(itemView);
        mAdapter = adapter;
    }

    @Override
    protected void initView() {
        mTimestampTv = $(R.id.timestamp_tv);
    }

    @Override
    protected void bindView() {
        setTimestamp();
    }

    private boolean setTimestamp() {
        if (mPosition - 1 >= 0) {
            SixinMessageModel lastModel = mAdapter.getItem(mPosition - 1);
            if (lastModel != null) {
                if (Math.abs(mViewModel.getReceiveTime() - lastModel.getReceiveTime()) < SixinConstants.MESSAGE_TIMESTAMP_INTERVAL) {
                    mTimestampTv.setVisibility(View.GONE);
                    mTimestampTv.setText("");
                    return false;
                }
            }
        }

        mTimestampTv.setVisibility(View.VISIBLE);
        mTimestampTv.setText(mViewModel.getFormatSentTime());
        return true;
    }
}