package com.wali.live.watchsdk.fans;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.adapter.FansGroupListAdapter;
import com.wali.live.watchsdk.fans.event.QuitGroupEvent;
import com.wali.live.watchsdk.fans.listener.FansGroupListListener;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.FansGroupListModel;
import com.wali.live.watchsdk.fans.pay.FansPayFragment;
import com.wali.live.watchsdk.fans.presenter.FansGroupListPresenter;
import com.wali.live.watchsdk.fans.presenter.IFansGroupListView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by lan on 17-6-15.
 */
public class FansGroupListFragment extends BaseEventBusFragment implements View.OnClickListener, IFansGroupListView, FansGroupListListener, FragmentDataListener {
    private BackTitleBar mTitleBar;

    private RecyclerView mFansGroupRv;
    private FansGroupListAdapter mAdapter;

    private FansGroupListPresenter mPresenter;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_fans_group_list, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.getBackBtn().setText(getContext().getString(R.string.vfan_me));
        mTitleBar.getBackBtn().setOnClickListener(this);

        mFansGroupRv = $(R.id.recycler_view);
        mFansGroupRv.setLayoutManager(new LinearLayoutManager(getContext()));

        mAdapter = new FansGroupListAdapter(this);
        mFansGroupRv.setAdapter(mAdapter);

        initPresenter();
    }

    private void initPresenter() {
        mPresenter = new FansGroupListPresenter(this);
        mPresenter.getFansGroupList(true);
    }

    @Override
    public void setFansGroupList(FansGroupListModel model) {
        if (model.isFirst()) {
            mAdapter.setDataList(model);
        } else {
            mAdapter.addDataList(model);
        }
    }

    @Override
    public void createGroup(String name) {
        mPresenter.createGroup(name);
    }

    @Override
    public void openPrivilege(FansGroupDetailModel detailModel) {
        FansPayFragment.open((BaseSdkActivity) getActivity(),
                detailModel, "", detailModel.getVipLevel() <= 0, false, this);
    }

    @Override
    public void notifyCreateGroupResult(boolean isSuccess) {
        KeyboardUtils.hideKeyboard(getActivity());
        if (isSuccess) {
            ToastUtils.showToast(getContext(), R.string.create_group_success);
            mPresenter.getFansGroupList(true);
        } else {
            ToastUtils.showToast(getContext(), R.string.create_group_faild);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QuitGroupEvent event) {
        if (event != null) {
            if (mPresenter != null) {
                mPresenter.getFansGroupList(true);
            }
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
        if (requestCode == FansPayFragment.REQUEST_CODE_PAY) {
            if (resultCode == Activity.RESULT_OK) {
                // 支付成功，重新拉一下数据
                mPresenter.getFansGroupList(true);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() == null) {
            return false;
        }
        finish();
        return true;
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void open(BaseActivity activity) {
        FragmentNaviUtils.addFragmentToBackStack(activity, R.id.main_act_container, FansGroupListFragment.class,
                null, true, R.anim.slide_right_in, R.anim.slide_right_out);
    }
}
