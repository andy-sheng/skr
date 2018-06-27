package com.wali.live.watchsdk.personalcenter.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.event.KeyboardEvent;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.user.User;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.editinfo.EditInfoActivity;
import com.wali.live.watchsdk.editinfo.fragment.presenter.EditSignPresenter;
import com.wali.live.watchsdk.editinfo.fragment.presenter.IEditSignView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-25.
 */

public class EditSignHalfFragment extends RxFragment implements IEditSignView {

    private static final String TAG = "EditSignHalfFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    //ui
    private TextView mBackTv;
    private TextView mConfirmTv;
    private EditText mSignEt;
    private TextView mHintTv;
    private View mTopView;
    private View mBottomSplit;

    //presenter
    private EditSignPresenter mPresenter;

    //data
    private User mUser;
    private int mSignMaxCount;
    private String mSign;
    private boolean mInfoChanged;

    private TextWatcher mTextWatcher = new TextWatcher() {
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
    };

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.frag_edit_sign_half, container, false);
    }

    @Override
    protected void bindView() {
        mBackTv = (TextView) mRootView.findViewById(R.id.back_tv);
        mConfirmTv = (TextView) mRootView.findViewById(R.id.confirm_tv);
        mSignEt = (EditText) mRootView.findViewById(R.id.sign_et);
        mHintTv = (TextView) mRootView.findViewById(R.id.left_character_hint);
        mTopView = mRootView.findViewById(R.id.place_holder_view);
        mBottomSplit = mRootView.findViewById(R.id.bottom_split);

        mUser = MyUserInfoManager.getInstance().getUser();
        mSignMaxCount = getResources().getInteger(R.integer.max_sign_char_count);
//        mHintTv.setText(String.valueOf(mSignMaxCount) + getString(R.string.character_text));
        mSign = mUser.getSign();

        if (!TextUtils.isEmpty(mSign)) {
            if (mSign.length() > mSignMaxCount) {
                mSign = mSign.substring(0, mSignMaxCount);
            }
            mSignEt.setText(mSign);
            mSignEt.setSelection(mSign.length());
        }

        initPresenter();
        initListener();

        updateCountHint();
    }

    private void updateCountHint() {
        String sign = mSignEt.getText().toString();
        if (TextUtils.isEmpty(sign)) {
            mHintTv.setText(String.valueOf(mSignMaxCount) + GlobalData.app().getResources().getString(R.string.character_text));
            return;
        }

        int length = sign.length();
        mHintTv.setText(String.valueOf(mSignMaxCount - length) + GlobalData.app().getResources().getString(R.string.character_text));
    }

    private void initListener() {
        RxView.clicks(mBackTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        closeFragment();
                    }
                });

        RxView.clicks(mConfirmTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        clickSaveBtn();
                    }
                });
        RxView.clicks(mTopView).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popAllFragmentFromStack(getActivity());
                    }
                });

        mSignEt.addTextChangedListener(mTextWatcher);
    }

    private void initPresenter() {
        mPresenter = new EditSignPresenter(this);
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

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = false, priority = 1)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.d(TAG, "KeyboardEvent");
        int start = DisplayUtils.dip2px(0f);
        int height = KeyboardUtils.getKeyboardHeight(getActivity());
        int y = KeyboardUtils.getScreenHeight(getActivity()) - mSignEt.getBottom() - mTopView.getHeight();
        int tran = height - y;
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBottomSplit.getLayoutParams();
                params.height = tran;
                mBottomSplit.setLayoutParams(params);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                RelativeLayout.LayoutParams paramsl = (RelativeLayout.LayoutParams) mBottomSplit.getLayoutParams();
                paramsl.height = start;
                mBottomSplit.setLayoutParams(paramsl);
                break;
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
        mTextWatcher = null;
    }

    @Override
    public void editSuccess(String sign) {
        MyLog.d(TAG, "editSuccess sign=" + sign);
        mInfoChanged = true;
        ToastUtils.showToast(R.string.change_sign_success);

        mSign = sign;
        mUser.setSign(mSign);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public static void openFragment(BaseSdkActivity activity, int containerId, FragmentDataListener listener) {
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, containerId, EditSignHalfFragment.class,
                new Bundle(), true, true, true);
        fragment.initDataResult(REQUEST_CODE, listener);
    }
}
