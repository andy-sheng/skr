package com.common.core.cta;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.common.preference.PreferenceUtils;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.utils.FragmentUtils;
import com.common.utils.U;

/**
 * Created by yaojian on 16-3-26.
 */
public class CTANotifyFragment extends BaseFragment implements View.OnClickListener {

    public static final String PREF_KEY_NEED_SHOW_CTA = "pref_key_need_show_cta";

    private CTANotifyButtonClickListener mButtonClickListener = null;

    private CheckBox mNeverShowCb;          //只显示一次
    private TextView mCancelButton;         //取消按钮
    private TextView mConfirmButton;            //确定按钮

    @Override
    public int initView() {
        return R.layout.fragment_cta_notify;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mNeverShowCb = (CheckBox) mRootView.findViewById(R.id.never_show_cb);
        mNeverShowCb.setChecked(true);

        TextView messageTv = (TextView) mRootView.findViewById(R.id.message);
        Linkify.addLinks(messageTv, Linkify.ALL);
        messageTv.setMovementMethod(LinkMovementMethod.getInstance());
        mCancelButton = (TextView) mRootView.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        mConfirmButton = (TextView) mRootView.findViewById(R.id.agree_button);
        mConfirmButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cancel_button) {
            if (mButtonClickListener != null) {
                mButtonClickListener.onClickCancelButton();
            }

        } else if (i == R.id.agree_button) {
            //TODO: 为了适应 cta 弹窗策略，避免偷跑流量，调整数据初始化，待优化
            if (mButtonClickListener != null) {
                PreferenceUtils.setSettingBoolean(PREF_KEY_NEED_SHOW_CTA, !mNeverShowCb.isChecked());
//                InitManager.initForCoreProcess(GlobalData.app());
                mButtonClickListener.onClickConfirmButton();
            }
        }
    }

    @Override
    public int getRequestCode() {
        return 0;
    }

    public static void openFragment(BaseActivity activity, CTANotifyButtonClickListener listener) {
        CTANotifyFragment fragment = (CTANotifyFragment) U.getFragmentUtils().addFragment(
                FragmentUtils.newParamsBuilder(activity, CTANotifyFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
        fragment.setCTANotifyButtonClickListener(listener);
    }

    /**
     * 设置回调
     *
     * @param listener
     */
    public void setCTANotifyButtonClickListener(CTANotifyButtonClickListener listener) {
        if (listener != null) {
            mButtonClickListener = listener;
        }
    }


    /**
     * 按钮点击事件的回调
     */
    public interface CTANotifyButtonClickListener {
        /**
         * 点击取消按钮
         */
        void onClickCancelButton();

        /**
         * 点击确定按钮
         */
        void onClickConfirmButton();
    }
}
