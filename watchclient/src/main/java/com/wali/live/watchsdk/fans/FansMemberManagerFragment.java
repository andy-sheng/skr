package com.wali.live.watchsdk.fans;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.log.MyLog;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.presenter.FansMemberManagerPresenter;
import com.wali.live.watchsdk.fans.view.FansMemberManagerView;

import static com.wali.live.component.view.Utils.$component;

/**
 * Created by yangli on 17/11/20.
 */
public class FansMemberManagerFragment extends RxFragment {

    private static final String EXTRA_GROUP_INFO = "EXTRA_GROUP_INFO";

    private FansGroupDetailModel mGroupDetailModel;

    protected FansMemberManagerView mManagerView;
    protected FansMemberManagerPresenter mManagerPresenter;

    @Override
    public final int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_fans_member_manager, container, false);
    }

    @Override
    protected void bindView() {
        if (getArguments() != null) {
            mGroupDetailModel = (FansGroupDetailModel) getArguments().getSerializable(EXTRA_GROUP_INFO);
        }
        if (mGroupDetailModel == null) {
            finish();
            return;
        }
        mManagerView = $(R.id.member_manager_view);
        mManagerPresenter = new FansMemberManagerPresenter(mGroupDetailModel.getZuid());
        $component(mManagerView, mManagerPresenter);
        mManagerPresenter.startPresenter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManagerPresenter.stopPresenter();
        mManagerPresenter.destroy();
    }

    private void finish() {
        try {
            FragmentNaviUtils.removeFragment(this);
        } catch (Exception e) {
            MyLog.e(e);
        }
    }

    @Override
    public boolean onBackPressed() {
        finish();
        return true;
    }

    public static void openFragment(BaseActivity activity, FansGroupDetailModel groupDetailModel) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_GROUP_INFO, groupDetailModel);
        FragmentNaviUtils.addFragment(activity, FansMemberManagerFragment.class, bundle, R.id.main_act_container);
    }
}
