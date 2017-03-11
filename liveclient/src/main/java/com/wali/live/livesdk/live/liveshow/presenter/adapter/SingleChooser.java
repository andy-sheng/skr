package com.wali.live.livesdk.live.liveshow.presenter.adapter;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import static android.view.View.NO_ID;

/**
 * Created by yangli on 2017/3/11.
 *
 * @module 多选一辅助器
 */
public class SingleChooser implements View.OnClickListener {
    private static final String TAG = "SingleChooser";

    private IChooserListener mListener;

    private ViewGroup mViewGroup;
    private View mSelectedView;

    private <T extends View> T $(@IdRes int resId) {
        return (T) mViewGroup.findViewById(resId);
    }

    @Override
    public void onClick(View v) {
        if (mSelectedView != null && mSelectedView == v) {
            return;
        }
        if (mSelectedView != null) {
            mSelectedView.setSelected(false);
        }
        mSelectedView = v;
        mSelectedView.setSelected(true);
        if (mListener != null) {
            mListener.onItemSelected(mSelectedView);
        }
    }

    public SingleChooser(IChooserListener listener) {
        mListener = listener;
    }

    public void setup(@NonNull ViewGroup viewGroup, @IdRes int selectedId) {
        if (mViewGroup != null && mViewGroup != viewGroup) {
            reset();
        }
        mViewGroup = viewGroup;

        int childCount = mViewGroup.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View view = mViewGroup.getChildAt(i);
            int id = view.getId();
            if (id == NO_ID) {
                continue;
            }
            if (id == selectedId) {
                mSelectedView = view;
                mSelectedView.setSelected(true);
            }
            view.setOnClickListener(this);
        }
    }

    public void reset() {
        if (mViewGroup == null) {
            return;
        }
        int childCount = mViewGroup.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View view = mViewGroup.getChildAt(i);
            view.setOnClickListener(null);
        }
        mViewGroup = null;
        mSelectedView = null;
    }


    public interface IChooserListener {

        void onItemSelected(View view);

    }
}
