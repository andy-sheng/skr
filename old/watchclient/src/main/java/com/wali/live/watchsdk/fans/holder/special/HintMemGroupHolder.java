package com.wali.live.watchsdk.fans.holder.special;

import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.item.CreateFansGroupModel;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;

/**
 * Created by lan on 2017/11/8.
 */
public class HintMemGroupHolder extends BaseHolder<CreateFansGroupModel> {
    private TextView mHintTv;

    public HintMemGroupHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mHintTv = $(R.id.hint_tv);
    }

    @Override
    protected void bindView() {
        mHintTv.setText(R.string.already_joined_group);
    }
}
