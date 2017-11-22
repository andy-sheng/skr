package com.wali.live.watchsdk.fans.pay.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.wali.live.dao.Gift;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.pay.FansPayFragment;
import com.wali.live.watchsdk.fans.pay.adapter.FansPayAdapter;
import com.wali.live.watchsdk.fans.pay.model.FansPayModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

/**
 * Created by lan on 17/6/11.
 */
public class FansPayHolder extends BaseHolder<FansPayModel> {
    private TextView mPayValueTv;
    private TextView mPayTimeTv;

    private FansPayAdapter mPayAdapter;

    public FansPayHolder(View itemView, FansPayAdapter payAdapter) {
        super(itemView);
        mPayAdapter = payAdapter;
    }

    @Override
    protected void initView() {
        mPayValueTv = $(R.id.pay_value);
        mPayTimeTv = $(R.id.pay_time);

        initListener();
        adjustViewSize();
    }

    private void adjustViewSize() {
        int width = (DisplayUtils.getScreenWidth() - (1 + FansPayFragment.SPAN_COUNT) * DisplayUtils.dip2px(10)) / FansPayFragment.SPAN_COUNT;

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = width;
    }

    private void initListener() {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPayAdapter.setSelectedItem(mViewModel);
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

        itemView.setSelected(mViewModel.equals(mPayAdapter.getSelectedItem()));
    }
}
