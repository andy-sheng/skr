package com.wali.live.envelope;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.view.BackTitleBar;
import com.live.module.common.R;

/**
 * Created by yangli on 2017/7/18.
 *
 * @module 发红包
 */
public class SendEnvelopeFragment extends BaseFragment {

    private BackTitleBar mTitleBar;

    @Override
    public int getRequestCode() {
        return mRequestCode;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.send_envelope_fragment, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(R.string.send_redpacket);
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void openFragment(BaseActivity activity) {
        FragmentNaviUtils.addFragment(activity, R.id.main_act_container, SendEnvelopeFragment.class,
                null, true, true, true);
    }
}
