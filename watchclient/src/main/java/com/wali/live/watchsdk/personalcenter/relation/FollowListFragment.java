package com.wali.live.watchsdk.personalcenter.relation;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.view.BackTitleBar;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.data.UserListData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.relation.adapter.FollowFansAdapter;
import com.wali.live.watchsdk.personalcenter.relation.contact.FollowListContact;
import com.wali.live.watchsdk.personalcenter.relation.presenter.FollowListPresenter;
import com.wali.live.watchsdk.recipient.view.IndexableRecyclerView;
import com.wali.live.watchsdk.recipient.view.UserSectionIndexer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-21.
 *
 */

public class FollowListFragment extends BaseFragment{
    private static final String TAG = "FollowListFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();
    public static final String EXTRA_IN_USER_UUID = "extra_user_uuid";                              //用户id

    //presenter
    private FollowListPresenter mPresenter;

    //data
    private long mUuid;

    //ui
    private BackTitleBar mTitleBar;
    private IndexableRecyclerView mRecyclerView;
    private FollowFansAdapter mAdapter;
    private UserSectionIndexer mIndexer;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_relation_list, container, false);
    }

    @Override
    protected void bindView() {
        mRecyclerView = (IndexableRecyclerView) mRootView.findViewById(R.id.recycler_view);
        mTitleBar = (BackTitleBar) mRootView.findViewById(R.id.title_bar);
        mTitleBar.getBackBtn().setText(GlobalData.app().getResources().getString(R.string.follow));
        mAdapter = new FollowFansAdapter();
        mIndexer = new UserSectionIndexer();
        mRecyclerView.setSectionIndexer(mIndexer);
        mRecyclerView.showIndexBar();
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setAdapter(mAdapter);

        initParams();
        initListener();
        initPresenter();

        mPresenter.loadFollowList(mUuid);
    }

    private void initParams() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUuid = bundle.getLong(EXTRA_IN_USER_UUID, 0);
        }

        if(mUuid == 0) {
            MyLog.d(TAG, "uuid is 0");
            FragmentNaviUtils.popFragmentFromStack(getActivity());
        }
    }

    private void initListener() {
        RxView.clicks(mTitleBar.getBackBtn()).throttleFirst(300, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                FragmentNaviUtils.popFragmentFromStack(getActivity());
            }
        });
    }

    private void initPresenter() {
        mPresenter = new FollowListPresenter(mRelationListContact);
    }

    private FollowListContact.Iview mRelationListContact = new FollowListContact.Iview() {

        @Override
        public void loadFollowListSuccess(List<UserListData> datas) {
            if(datas != null && !datas.isEmpty()) {
                mAdapter.setDataSourse(datas);
                ArrayList<Object> objects = new ArrayList<>();
                objects.addAll(datas);
                mIndexer.setDataList(objects);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    public static void openFragment(Activity activity, int containerId, long uuid) {
        Bundle bundle = new Bundle();
        bundle.putLong(FollowListFragment.EXTRA_IN_USER_UUID, uuid);
        FragmentNaviUtils.addFragment((BaseSdkActivity) activity, containerId, FollowListFragment.class, bundle, true, true, true);
    }
}
