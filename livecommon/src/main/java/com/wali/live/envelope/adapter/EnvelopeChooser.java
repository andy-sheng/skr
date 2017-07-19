package com.wali.live.envelope.adapter;

import android.support.annotation.NonNull;
import android.view.View;

import com.wali.live.component.presenter.adapter.SingleChooser;

import java.util.List;

/**
 * Created by wangmengjie on 2017/7/19.
 *
 * @module 红包选择辅助
 */
public class EnvelopeChooser extends SingleChooser {

    public EnvelopeChooser(IChooserListener listener) {
        super(listener);
    }

    public void setup(@NonNull List<View> views, View selectedView) {
        reset();
        for (View view : views) {
            view.setOnClickListener(this);
        }
        mSelectedView = selectedView;
        if (mSelectedView != null) {
            mSelectedView.setSelected(true);
        }
    }
}
