package com.wali.live.common.gift.adapter.viewHolder;

import android.support.v7.widget.RecyclerView;

import com.wali.live.common.gift.view.GiftDisPlayItemView;

/**
 * Created by chengsimin on 16/7/28.
 */
public class GiftItemViewHolder extends RecyclerView.ViewHolder{
    public GiftDisPlayItemView giftDisPlayItemView;

    public GiftItemViewHolder(GiftDisPlayItemView giftDisPlayItemView) {
        super(giftDisPlayItemView);
        this.giftDisPlayItemView = giftDisPlayItemView;
    }
}