package com.module.playways.room.gift.loadsir;


import com.kingja.loadsir.callback.Callback;
import com.module.playways.R;

public class GiftEmptyCallback extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.gift_list_empty_layout;
    }
}
