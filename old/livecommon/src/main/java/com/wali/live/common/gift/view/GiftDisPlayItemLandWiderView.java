package com.wali.live.common.gift.view;

import android.content.Context;
import android.util.AttributeSet;

import com.live.module.common.R;


/**
 * Created by ybao on 17/7/6
 */

public class GiftDisPlayItemLandWiderView extends GiftDisPlayItemLandView{

    public GiftDisPlayItemLandWiderView(Context context) {
        super(context);
    }

    public GiftDisPlayItemLandWiderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GiftDisPlayItemLandWiderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init(Context context) {
        inflate(context, R.layout.gift_display_item_wider_view,this);
        bindView();
    }
}