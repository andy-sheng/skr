package com.wali.live.watchsdk.channel.holder;

import android.view.View;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by zhaomin on 17/11/2.
 * @module 和twocardholder 几乎一样 但高度矮
 */
public class TwoCardWideHolder extends TwoCardHolder {

    public TwoCardWideHolder(View itemView) {
        super(itemView);
        MyLog.d(TAG, " TwoCardWideHolder init ");
        mLabelColor = R.color.color_f8a500;
        mDefaultNameMaxWidth = DisplayUtils.dip2px(140);
    }


    @Override
    protected void initContentViewId() {
        super.initContentViewId();
        mSingleCardRadio = 0.5625f;
    }
}
