package com.module.home.feedback;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.LogUploadUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class FeedbackFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    RadioGroup mButtonArea;
    RadioButton mErrorBack;
    RadioButton mFeedBack;
    NoLeakEditText mFeedbackContent;
    ExTextView mContentTextSize;
    ExTextView mSubmitTv;

    ProgressBar mUploadProgressBar;

    int before;  // 记录之前的位置

    @Override
    public int initView() {
        return R.layout.feedback_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar)mRootView.findViewById(R.id.titlebar);
        mButtonArea = (RadioGroup)mRootView.findViewById(R.id.button_area);
        mErrorBack = (RadioButton)mRootView.findViewById(R.id.error_back);
        mFeedBack = (RadioButton)mRootView.findViewById(R.id.feed_back);
        mFeedbackContent = (NoLeakEditText)mRootView.findViewById(R.id.feedback_content);
        mContentTextSize = (ExTextView)mRootView.findViewById(R.id.content_text_size);
        mSubmitTv = (ExTextView)mRootView.findViewById(R.id.submit_tv);
        mUploadProgressBar = (ProgressBar)mRootView.findViewById(R.id.upload_progress_bar);


        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        U.getFragmentUtils().popFragment(FeedbackFragment.this);
                    }
                });


        mFeedbackContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                before = i;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                mContentTextSize.setText("" + length + "/200");
                int selectionEnd = mFeedbackContent.getSelectionEnd();
                if (length > 200) {
                    editable.delete(before, selectionEnd);
                    mFeedbackContent.setText(editable.toString());
                    int selection = editable.length();
                    mFeedbackContent.setSelection(selection);
                }
            }
        });

        RxView.clicks(mSubmitTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        mUploadProgressBar.setVisibility(View.VISIBLE);
                        U.getLogUploadUtils().upload(MyUserInfoManager.getInstance().getUid());
                    }
                });

    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LogUploadUtils.UploadLogEvent event) {
        mUploadProgressBar.setVisibility(View.GONE);
        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(getContext())
                .setImage(event.mIsSuccess ? R.drawable.touxiangshezhichenggong_icon : R.drawable.touxiangshezhishibai_icon)
                .setText(event.mIsSuccess ? "反馈成功" : "反馈失败")
                .build());

        U.getFragmentUtils().popFragment(FeedbackFragment.this);
    }
}
