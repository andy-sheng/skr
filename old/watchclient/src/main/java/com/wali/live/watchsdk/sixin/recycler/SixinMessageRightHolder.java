package com.wali.live.watchsdk.sixin.recycler;

import android.view.View;
import android.widget.ImageView;

import com.base.log.MyLog;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.sixin.cache.SendingMessageCache;
import com.wali.live.watchsdk.sixin.recycler.adapter.SixinMessageAdapter;
import com.wali.live.watchsdk.sixin.recycler.helper.HolderHelper;

/**
 * Created by lan on 16-5-20.
 */
public class SixinMessageRightHolder extends SixinMessageHolder {
    private static final int RESEND_MESSAGE_TIMEOUT = 15000 * 2;

    private ImageView mResendBtn;

    private Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.notifyDataSetChanged();
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HolderHelper.resendMessage(mViewModel.getMsgId());
        }
    };

    public SixinMessageRightHolder(View itemView, SixinMessageAdapter adapter) {
        super(itemView, adapter);
    }

    @Override
    protected void initView() {
        super.initView();
        mResendBtn = $(R.id.resend_btn);
    }

    @Override
    protected void bindView() {
        super.bindView();
        setResendStatus();
    }

    private void setResendStatus() {
        mResendBtn.setVisibility(View.GONE);
        mResendBtn.setOnClickListener(null);

        if (mViewModel.isInbound()) {
            return;
        }

        if (mViewModel.getOutboundStatus() >= SixinMessage.OUTBOUND_STATUS_RECEIVED) {
            mResendBtn.removeCallbacks(mRefreshRunnable);
            return;
        }

        Long sendTime = SendingMessageCache.get(mViewModel.getMsgId());
        MyLog.d("setResend sendTime=" + sendTime);
        if (sendTime != null && (System.currentTimeMillis() - sendTime < RESEND_MESSAGE_TIMEOUT)) {
            mResendBtn.removeCallbacks(mRefreshRunnable);
            mResendBtn.postDelayed(mRefreshRunnable, RESEND_MESSAGE_TIMEOUT);
            return;
        }

        if (sendTime == null || (System.currentTimeMillis() - sendTime >= RESEND_MESSAGE_TIMEOUT)) {
            mResendBtn.setVisibility(View.VISIBLE);
            mResendBtn.setOnClickListener(mClickListener);
            mResendBtn.removeCallbacks(mRefreshRunnable);
            return;
        }
    }
}