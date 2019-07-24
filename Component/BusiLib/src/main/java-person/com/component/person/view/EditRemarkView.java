package com.component.person.view;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.component.busilib.R;

public class EditRemarkView extends RelativeLayout {

    Activity mActivity;
    String mNickName;
    String mRemarkName;
    Listener mListener;

    View mPlaceBottomView;
    View mPlaceTopView;
    NoLeakEditText mRemarkNameEdt;
    ImageView mClearEditIv;
    ExTextView mCancelTv;
    ExTextView mSaveTv;

    public EditRemarkView(Activity activity, String nickName, String remarkName) {
        super(activity);
        this.mActivity = activity;
        this.mNickName = nickName;
        this.mRemarkName = remarkName;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.remark_edit_view_layout, this);

        mRemarkNameEdt = findViewById(R.id.remark_name_edt);
        mClearEditIv = findViewById(R.id.clear_edit_iv);
        mCancelTv = findViewById(R.id.cancel_tv);
        mSaveTv = findViewById(R.id.save_tv);
        mPlaceBottomView = findViewById(R.id.place_bottom_view);
        mPlaceTopView = findViewById(R.id.place_top_view);

        ViewGroup.LayoutParams layoutParams = mPlaceBottomView.getLayoutParams();
        layoutParams.height = U.getKeyBoardUtils().getKeyBoardHeight();
        mPlaceBottomView.setLayoutParams(layoutParams);

        if (!TextUtils.isEmpty(mRemarkName)) {
            mRemarkNameEdt.setText(mRemarkName);
        }
        if (!TextUtils.isEmpty(mNickName)) {
            // 怎么变，提示都显示其昵称
            mRemarkNameEdt.setHint(mNickName);
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
                if (U.getStringUtils().getStringLength(mRemarkName) > 14) {
                    U.getToastUtil().showShort("备注名长度不能超过14个字符哦～");
                } else {
                    if (mListener != null) {
                        mListener.onClickSave(mRemarkName);
                    }
                }
            }
        });

        mClearEditIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mRemarkNameEdt.setText("");
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
                U.getKeyBoardUtils().showSoftInputKeyBoard(mActivity);
            }
        }, 300);

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClickCancel();

        void onClickSave(String remarkName);
    }

}
