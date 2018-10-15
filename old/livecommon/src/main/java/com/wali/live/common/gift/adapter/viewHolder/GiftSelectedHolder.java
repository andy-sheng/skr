package com.wali.live.common.gift.adapter.viewHolder;

import android.support.v7.widget.RecyclerView;

import com.wali.live.common.gift.view.GiftSelectedView;


/**
 * Created by xzy on 17-6-29.
 */

public class GiftSelectedHolder extends RecyclerView.ViewHolder {

    public GiftSelectedView mGiftSelectedView;

    public GiftSelectedHolder(GiftSelectedView giftSelectedView) {
        super(giftSelectedView);
        mGiftSelectedView = giftSelectedView;
    }
}
