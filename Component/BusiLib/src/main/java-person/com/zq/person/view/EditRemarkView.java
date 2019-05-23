package com.zq.person.view;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.NoLeakEditText;
import com.component.busilib.R;
import com.dialog.view.StrokeTextView;

public class EditRemarkView extends RelativeLayout {

    BaseFragment mFragment;
    String mRemarkName;
    Listener mListener;

    View mPlaceBottomView;
    View mPlaceTopView;
    NoLeakEditText mRemarkNameEdt;
    StrokeTextView mCancelTv;
    StrokeTextView mSaveTv;

    public EditRemarkView(BaseFragment fragment, String remarkName) {
        super(fragment.getContext());
        this.mFragment = fragment;
        this.mRemarkName = remarkName;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.remark_edit_view_layout, this);

        mRemarkNameEdt = (NoLeakEditText) findViewById(R.id.remark_name_edt);
        mCancelTv = (StrokeTextView) findViewById(R.id.cancel_tv);
        mSaveTv = (StrokeTextView) findViewById(R.id.save_tv);
        mPlaceBottomView = (View) findViewById(R.id.place_bottom_view);
        mPlaceTopView = (View) findViewById(R.id.place_top_view);

        ViewGroup.LayoutParams layoutParams = mPlaceBottomView.getLayoutParams();
        layoutParams.height = U.getKeyBoardUtils().getKeyBoardHeight();
        mPlaceBottomView.setLayoutParams(layoutParams);

        if (!TextUtils.isEmpty(mRemarkName)) {
            mRemarkNameEdt.setText(mRemarkName);
            mRemarkNameEdt.setHint(mRemarkName);
        }

        mCancelTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancel();
                }
            }
        });

        mSaveTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                String mRemarkName = mRemarkNameEdt.getText().toString().trim();
                if (mListener != null) {
                    mListener.onClickSave(mRemarkName);
                }
            }
        });

        mPlaceBottomView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancel();
                }
            }
        });
        mPlaceTopView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancel();
                }
            }
        });

        mRemarkNameEdt.postDelayed(new Runnable() {
            @Override
            public void run() {
                String editRemark = mRemarkNameEdt.getText().toString().trim();
                if (!TextUtils.isEmpty(editRemark)) {
                    mRemarkNameEdt.setSelection(editRemark.length());
                }
                mRemarkNameEdt.requestFocus();
                U.getKeyBoardUtils().showSoftInputKeyBoard(mFragment.getActivity());
            }
        }, 500);

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClickCancel();

        void onClickSave(String remarkName);
    }

}
