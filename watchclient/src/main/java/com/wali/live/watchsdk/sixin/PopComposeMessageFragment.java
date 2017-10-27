package com.wali.live.watchsdk.sixin;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.view.BackTitleBar;
import com.wali.live.watchsdk.R;

/**
 * Created by lan on 2017/10/27.
 */
public class PopComposeMessageFragment extends RxFragment implements View.OnClickListener {
    private View mBgView;

    private BackTitleBar mTitleBar;
    private RecyclerView mMessageRv;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_pop_compose_message, container, false);
    }

    @Override
    protected void bindView() {
        mBgView = $(R.id.bg_view);
        $click(mBgView, this);

        mTitleBar = $(R.id.title_bar);
        $click(mTitleBar.getBackBtn(), this);

        mMessageRv = $(R.id.message_rv);
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() == null) {
            return false;
        }
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bg_view || id == R.id.back_iv) {
            finish();
        }
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void open(BaseActivity activity, boolean isNeedSaveToStack) {
        Bundle bundle = new Bundle();
        if (isNeedSaveToStack) {
            FragmentNaviUtils.addFragmentToBackStack(activity, R.id.main_act_container, PopComposeMessageFragment.class, bundle, false, 0, 0);
        } else {
            FragmentNaviUtils.addFragmentAndResetArgument(activity, R.id.main_act_container, PopComposeMessageFragment.class, bundle, true, false, 0, 0);
        }
    }
}
