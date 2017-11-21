package com.wali.live.watchsdk.fans.pay.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.wali.live.dao.Gift;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.pay.model.FansPayModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

/**
 * Created by lan on 17/6/11.
 */
public class FansPayHolder extends BaseHolder<FansPayModel> {
    private TextView mPayValueTv;
    private TextView mPayTimeTv;

    public FansPayHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mPayValueTv = $(R.id.pay_value);
        mPayTimeTv = $(R.id.pay_time);

        initListener();
    }

    private void initListener() {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    protected void bindView() {
        Gift gift = mViewModel.getGift();

        mPayValueTv.setText(String.valueOf(gift.getPrice()));

        String name = gift.getInternationalName();
        if (TextUtils.isEmpty(name)) {
            name = gift.getName();
        }
        mPayTimeTv.setText(name);
    }
}
