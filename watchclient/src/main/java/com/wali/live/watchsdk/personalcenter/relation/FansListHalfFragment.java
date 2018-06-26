package com.wali.live.watchsdk.personalcenter.relation;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.data.UserListData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.relation.adapter.FollowFansAdapter;
import com.wali.live.watchsdk.personalcenter.relation.contact.FansListContact;
import com.wali.live.watchsdk.personalcenter.relation.contact.FollowOptContact;
import com.wali.live.watchsdk.personalcenter.relation.contact.IFollowOptListener;
import com.wali.live.watchsdk.personalcenter.relation.presenter.FansListPresenter;
import com.wali.live.watchsdk.personalcenter.relation.presenter.FollowOptPresenter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FansListHalfFragment extends BaseFragment {
    private static final String TAG = "FansListHalfFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();
    public static final String EXTRA_IN_USER_UUID = "extra_user_uuid";//用户id

    //data
    private long mUuid;

    //ui
    private RecyclerView mRecyclerView;
    private FollowFansAdapter mAdapter;
    private TextView mBackTv;
    private TextView mConfirmTv;
    private View mTopView;

    //presenter
    private FansListPresenter mPresenter;
    private FollowOptPresenter mFollowOptPresenter;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_relation_list_half, container, false);
    }

    @Override
    protected void bindView() {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mBackTv = (TextView) mRootView.findViewById(R.id.back_tv);
        mConfirmTv = (TextView) mRootView.findViewById(R.id.confirm_tv);
        mTopView = mRootView.findViewById(R.id.place_holder_view);

        mAdapter = new FollowFansAdapter();
        mAdapter.setFollowOptListener(mIFollowOptListener);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setAdapter(mAdapter);

        initParams();
        initListener();
        initPresenter();

        mPresenter.loadFansList(mUuid, 0);
    }

    private void initParams() {
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            mUuid = bundle.getLong(EXTRA_IN_USER_UUID, 0);
//        }
        mUuid = MyUserInfoManager.getInstance().getUuid();

        if(mUuid == 0) {
            MyLog.d(TAG, "uuid is 0");
            FragmentNaviUtils.popFragmentFromStack(getActivity());
        }
    }

    private void initListener() {
        RxView.clicks(mBackTv).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popFragmentFromStack(getActivity());
                    }
                });

        RxView.clicks(mTopView).throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        FragmentNaviUtils.popAllFragmentFromStack(getActivity());
                    }
                });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                int visibleItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getChildCount();
                int totalItemCount = ((LinearLayoutManager) recyclerView.getLayoutManager()).getItemCount();
                if (firstVisibleItem + visibleItemCount > totalItemCount - 2) {
                    if (mPresenter.isHasMore()) {
                        mPresenter.loadFansList(mUuid, mAdapter.getItemCount());
                    }
                }
            }
        });
    }

    private void initPresenter() {
        mPresenter = new FansListPresenter(mFansListContact);
        mFollowOptPresenter = new FollowOptPresenter(mFollowOptContact);

    }

    private FansListContact.Iview mFansListContact = new FansListContact.Iview() {

        @Override
        public void loadFansListSuccess(List<UserListData> dataList) {
            if(dataList != null && !dataList.isEmpty()) {
                mAdapter.addDataSourse(dataList);
            }
        }
    };

    private IFollowOptListener mIFollowOptListener = new IFollowOptListener() {

        @Override
        public void follow(long targetId) {
            mFollowOptPresenter.follow(targetId);
        }

        @Override
        public void unFollow(long targetId) {
            mFollowOptPresenter.unFollow(targetId);
        }
    };

    private FollowOptContact.Iview mFollowOptContact = new FollowOptContact.Iview() {

        @Override
        public void followSuccess(long targetId) {
            if(mAdapter != null
                    && mAdapter.getDatas() != null
                    && !mAdapter.getDatas().isEmpty()) {
                for(UserListData data : mAdapter.getDatas()) {
                    if(data.userId == targetId) {
                        data.isBothway = true;
                        break;
                    }
                }

                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void unFollowSuccess() {
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
        mFollowOptPresenter.destroy();
    }

    public static void openFragment(BaseSdkActivity activity, int containerId) {
        FragmentNaviUtils.addFragment(activity, containerId, FansListHalfFragment.class,
                new Bundle(), true, true, true);
    }
}
