package com.wali.live.common.gift.view;

import android.content.Context;
import android.util.AttributeSet;

import com.live.module.common.R;
import com.wali.live.common.gift.presenter.GiftMallPresenter;

import butterknife.ButterKnife;

/**
 * Created by zjn on 16-7-27.
 */
public class GiftDisPlayItemLandView extends GiftDisPlayItemView{

    public GiftDisPlayItemLandView(Context context) {
        super(context);
    }

    public GiftDisPlayItemLandView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GiftDisPlayItemLandView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void init(Context context) {
        inflate(context, R.layout.gift_display_item_land_view,this);
        bindView();
    }

    public void setDataSource(GiftMallPresenter.GiftWithCard infoWithCard){
        super.setDataSource(infoWithCard);
        hideGiftOriginalPriceArea();
    }


}
