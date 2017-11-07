package com.wali.live.watchsdk.fans;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.view.BackTitleBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.recycler.FansGroupAdapter;

/**
 * Created by lan on 17-6-15.
 */
public class FansGroupListFragment extends BaseFragment implements View.OnClickListener {
    private BackTitleBar mTitleBar;

    private RecyclerView mFansGroupListRv;
    private FansGroupAdapter mFansGroupAdapter;

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

        mFansGroupListRv = $(R.id.recycler_view);
        mFansGroupListRv.setLayoutManager(new LinearLayoutManager(getContext()));

        mFansGroupAdapter = new FansGroupAdapter();
        mFansGroupListRv.setAdapter(mFansGroupAdapter);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            finish();
        }
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void open(BaseActivity activity) {
        FragmentNaviUtils.addFragmentToBackStack(activity, R.id.main_act_container, FansGroupListFragment.class,
                null, false, 0, 0);
    }
}
