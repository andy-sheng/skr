package com.wali.live.watchsdk.editinfo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.FragmentListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.user.User;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditSignPresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditSignView;

import rx.Observable;

/**
 * Created by lan on 2017/8/15.
 */
public class EditSignFragment extends RxFragment implements View.OnClickListener, FragmentListener,
        IEditSignView {
    // Fragment单一的请求数据回调，直接使用REQUEST_CODE
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    private BackTitleBar mTitleBar;
    private TextView mRightButton;

    private EditText mSignEt;
    private TextView mCountHintTv;

    private int mSignMaxCount;
    private String mSign;

    private EditSignPresenter mPresenter;
    private User mMe;
    private boolean mInfoChanged;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.edit_sign_layout, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(getString(R.string.change_signature_text));
        mTitleBar.getBackBtn().setOnClickListener(this);

        mRightButton = mTitleBar.getRightTextBtn();
        mRightButton.setText(getString(R.string.save));
        mRightButton.setOnClickListener(this);

        mSignEt = $(R.id.sign_et);
        mSignEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateCountHint();
            }
        });
        mSignMaxCount = getResources().getInteger(R.integer.max_sign_char_count);
        mSignEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter((mSignMaxCount))});

        mCountHintTv = $(R.id.left_character_hint);
        mCountHintTv.setText(String.valueOf(mSignMaxCount) + getString(R.string.character_text));

        initView();
        initPresenter();
    }

    private void initView() {
        mMe = MyUserInfoManager.getInstance().getUser();
        mSign = mMe.getSign();
        if (!TextUtils.isEmpty(mSign)) {
            if (mSign.length() > mSignMaxCount) {
                mSign = mSign.substring(0, mSignMaxCount);
            }
            mSignEt.setText(mSign);
            mSignEt.setSelection(mSign.length());
        }
    }

    private void updateCountHint() {
        String sign = mSignEt.getText().toString();
        if (TextUtils.isEmpty(sign)) {
            mCountHintTv.setText(String.valueOf(mSignMaxCount) + GlobalData.app().getString(R.string.character_text));
            return;
        }

        int length = sign.length();
        mCountHintTv.setText(String.valueOf(mSignMaxCount - length) + GlobalData.app().getString(R.string.character_text));
    }

    private void initPresenter() {
        mPresenter = new EditSignPresenter(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            closeFragment();
        } else if (i == R.id.right_text_btn) {
            clickSaveBtn();
        }
    }

    private void clickSaveBtn() {
        String sign = mSignEt.getText().toString();

        if (TextUtils.isEmpty(sign)) {
            ToastUtils.showToast(R.string.signature_is_empty);
            return;
        }
        if (sign.equals(mSign)) {
            ToastUtils.showToast(R.string.input_same_signature);
            return;
        }

        mPresenter.uploadSign(sign);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return bindUntilEvent();
    }

    @Override
    public void editSuccess(String sign) {
        MyLog.d(TAG, "editSuccess sign=" + sign);
        mInfoChanged = true;
        ToastUtils.showToast(R.string.change_sign_success);

        mSign = sign;
        mMe.setSign(mSign);

        closeFragment();
    }

    @Override
    public void editFailure(int code) {
        MyLog.w(TAG, "editFailure code=" + code);
        if (code == ErrorCode.CODE_CONTAIN_SENSITIVE) {
            ToastUtils.showToast(R.string.change_failed_include_sensitive);
        } else {
            ToastUtils.showToast(R.string.change_sign_failed);
        }
    }

    private void closeFragment() {
        MyLog.w(TAG, "closeFragment infoChanged=" + mInfoChanged);
        if (mInfoChanged && mDataListener != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(EditInfoActivity.EXTRA_OUT_INFO_CHANGED, mInfoChanged);
            mDataListener.onFragmentResult(mRequestCode, Activity.RESULT_OK, bundle);
        }
        finish();
    }

    private void finish() {
        MyLog.w(TAG, "finish");
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    @Override
    public boolean onBackPressed() {
        MyLog.d(TAG, " onBackPressed ");
        closeFragment();
        return true;
    }

    public static void open(BaseActivity activity, FragmentDataListener listener, Bundle bundle) {
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, R.id.main_act_container, EditSignFragment.class,
                bundle, true, true, true);
        fragment.initDataResult(REQUEST_CODE, listener);
    }
}
