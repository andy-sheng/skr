package com.wali.live.watchsdk.personalcenter.relation;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.user.User;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.relation.adapter.FollowFansAdapter;
import com.wali.live.watchsdk.personalcenter.relation.contact.FollowListContact;
import com.wali.live.watchsdk.personalcenter.relation.contact.FollowOptContact;
import com.wali.live.watchsdk.personalcenter.relation.contact.IFollowOptListener;
import com.wali.live.watchsdk.personalcenter.relation.contact.IItemOnclickListener;
import com.wali.live.watchsdk.personalcenter.relation.presenter.FollowListPresenter;
import com.wali.live.watchsdk.personalcenter.relation.presenter.FollowOptPresenter;
import com.wali.live.watchsdk.sixin.PopComposeMessageFragment;
import com.wali.live.watchsdk.sixin.pojo.SixinTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-6-21.
 *
 */

public class FollowListHalfFragment extends BaseFragment{
    private static final String TAG = "FollowListHalfFragment";
    public static final int REQUEST_CODE = GlobalData.getRequestCode();
    public static final String EXTRA_IN_USER_UUID = "extra_user_uuid";                              //用户id

    //presenter
    private FollowListPresenter mPresenter;
    private FollowOptPresenter mFollowOptPresenter;

    //data
    private long mUuid;
    private List<UserListData> mDatas;

    //ui
    private RecyclerView mRecyclerView;
    private FollowFansAdapter mAdapter;
    private TextView mBackTv;
    private TextView mConfirmTv;
    private View mTopView;
    private TextView mTitleTv;

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
        mTitleTv = (TextView) mRootView.findViewById(R.id.title_tv);
        mTitleTv.setText(GlobalData.app().getResources().getString(R.string.follow));
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

        mPresenter.loadFollowList(mUuid);
    }

    private void initParams() {
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            mUuid = bundle.getLong(EXTRA_IN_USER_UUID, 0);
//        }
//
//        if(mUuid == 0) {
//            MyLog.d(TAG, "uuid is 0");
//            FragmentNaviUtils.popFragmentFromStack(getActivity());
//        }
        mUuid = MyUserInfoManager.getInstance().getUuid();
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

        mAdapter.setItemOnClickListener(new IItemOnclickListener() {
            @Override
            public void onItemClick(User user) {
                SixinTarget sixinTarget = new SixinTarget(user);
                if (user.isBothwayFollowing()) {
                    sixinTarget.setFocusState(SixinMessage.MSG_STATUE_BOTHFOUCS);
                } else if (user.isFocused()) {
                    sixinTarget.setFocusState(SixinMessage.MSG_STATUS_ONLY_ME_FOUCS);
                } else {
                    sixinTarget.setFocusState(SixinMessage.MSG_STATUS_UNFOUCS);
                }
                sixinTarget.setTargetType(0);
                PopComposeMessageFragment.open((BaseActivity) getActivity(), sixinTarget, true);
            }
        });
    }

    private void initPresenter() {
        mPresenter = new FollowListPresenter(mRelationListContact);
        mFollowOptPresenter = new FollowOptPresenter(mFollowOptContact);
    }

    private FollowListContact.Iview mRelationListContact = new FollowListContact.Iview() {

        @Override
        public void loadFollowListSuccess(List<UserListData> datas) {
            if(datas != null && !datas.isEmpty()) {
                mDatas = datas;
                mAdapter.setDataSourse(datas);
                ArrayList<Object> objects = new ArrayList<>();
                objects.addAll(datas);
            }
        }
    };

    private IFollowOptListener mIFollowOptListener = new IFollowOptListener() {

        @Override
        public void follow(long targetId) {
//            mFollowOptPresenter.follow(targetId);
        }

        @Override
        public void unFollow(long targetId) {
            mFollowOptPresenter.unFollow(targetId);
        }
    };

    private FollowOptContact.Iview mFollowOptContact = new FollowOptContact.Iview() {

        @Override
        public void followSuccess(long targetId) {
        }

        @Override
        public void unFollowSuccess(long targetId) {
            ToastUtils.showToast(R.string.unfollow_success);
            if(mDatas != null
                    && !mDatas.isEmpty()) {
                for(int i = mDatas.size() - 1; i >= 0; i--) {
                    UserListData data = mDatas.get(i);
                    if(data.userId == targetId) {
                        mDatas.remove(data);
                        break;
                    }
                }
                mAdapter.setDataSourse(mDatas);
//                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    public static void openFragment(BaseSdkActivity activity, int containerId) {
        FragmentNaviUtils.addFragment(activity, containerId, FollowListHalfFragment.class,
                new Bundle(), true, true, true);
    }
}
