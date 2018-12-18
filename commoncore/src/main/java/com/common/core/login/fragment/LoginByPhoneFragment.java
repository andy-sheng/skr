package com.common.core.login.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExButton;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;

/**
 * 手机方式登陆界面
 */
public class LoginByPhoneFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mMainContainer;
    ExTextView mLogoTv;
    NoLeakEditText mInputPhoneEt;
    ExTextView mPhoneHintTv;
    ExButton mNextBtn;


    @Override
    public int initView() {
        return R.layout.core_phone_login_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mMainContainer = (RelativeLayout) mRootView.findViewById(R.id.main_container);
        mLogoTv = (ExTextView) mRootView.findViewById(R.id.logo_tv);
        mInputPhoneEt = (NoLeakEditText) mRootView.findViewById(R.id.input_phone_et);
        mPhoneHintTv = (ExTextView) mRootView.findViewById(R.id.phone_hint_tv);
        mNextBtn = (ExButton) mRootView.findViewById(R.id.next_btn);

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }
                String phoneNumber = mInputPhoneEt.getText().toString().trim();
                if (checkPhoneNumber(phoneNumber)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(VerifyCodeFragment.EXTRA_PHONE_NUMBER, phoneNumber);
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder(getActivity(), VerifyCodeFragment.class)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .setBundle(bundle)
                            .build());
                }
            }
        });
    }

    /**
     * 检查手机号是否正确
     *
     * @return
     */
    private boolean checkPhoneNumber(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber) && TextUtils.isDigitsOnly(phoneNumber)
                && phoneNumber.length() == 11 && phoneNumber.startsWith("1")) {
            return true;
        }

        mPhoneHintTv.setVisibility(View.VISIBLE);
        mPhoneHintTv.setText("请输入正确的手机号");
        return false;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
