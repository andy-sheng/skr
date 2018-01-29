package com.wali.live.watchsdk.income;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;


/**
 * Created by liuyanyan on 16/4/12.
 */
public class WithDrawInfoFragment extends BaseFragment {
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    public static final String KEY_OPEN_TYPE = "key_open_type";

    public static final int TYPE_DETAIL_INFO = 1;
    public static final int TYPE_WITHDRAW_FAIL = 2;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.detail_info_fragment, container, false);
    }

    @Override
    protected void bindView() {
        Bundle bundle = getArguments();
        int type = bundle.getInt(KEY_OPEN_TYPE);
        if (type == TYPE_WITHDRAW_FAIL) {
            TextView titleTv = (TextView) mRootView.findViewById(R.id.title);
            TextView messageTv = (TextView) mRootView.findViewById(R.id.message);
            TextView info1 = (TextView) mRootView.findViewById(R.id.info_num1);
            TextView info2 = (TextView) mRootView.findViewById(R.id.info_num2);
            TextView info3 = (TextView) mRootView.findViewById(R.id.info_num3);

            titleTv.setText(R.string.withdraw_fail_title);
            messageTv.setText(R.string.withdraw_fail_message);
            info1.setText(R.string.withdraw_fail_info1);
            info2.setText(R.string.withdraw_fail_info2);
            info3.setVisibility(View.GONE);
        }
        mRootView.findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentNaviUtils.popFragmentFromStack(getActivity());
            }
        });
    }

    public static void openFragment(BaseSdkActivity fragmentActivity, int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_OPEN_TYPE, type);
        FragmentNaviUtils.addFragment(fragmentActivity, WithDrawInfoFragment.class, bundle);
    }
}
