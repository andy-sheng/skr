package com.wali.live.watchsdk.longtext.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.longtext.model.interior.item.BaseFeedItemModel;

/**
 * Created by lan on 2017/9/20.
 */
public abstract class BaseFeedItemHolder<VM extends BaseFeedItemModel> extends BaseHolder<VM> {
    public BaseFeedItemHolder(View itemView) {
        super(itemView);
    }

    protected void bindText(TextView tv, String... texts) {
        if (tv == null) {
            return;
        }
        for (String text : texts) {
            if (!TextUtils.isEmpty(text)) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(text);
                return;
            }
        }
        tv.setVisibility(View.GONE);
    }
}
