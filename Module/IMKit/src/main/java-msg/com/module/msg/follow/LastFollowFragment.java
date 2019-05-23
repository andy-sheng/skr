package com.module.msg.follow;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.permission.SkrNotificationPermission;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.log.MyLog;
import com.common.notification.event.FollowNotifyEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.person.fragment.OtherPersonFragment3;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.rong.imkit.R;

public class LastFollowFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mContentRv;

    LastFollowAdapter mLastFollowAdapter;

    LoadService mLoadService;

    @Override
    public int initView() {
        return R.layout.last_follow_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mContentRv = (RecyclerView) mRootView.findViewById(R.id.content_rv);

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setRefreshHeader(new ClassicsHeader(getContext()));
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getLastRelations();
            }
        });

        mLastFollowAdapter = new LastFollowAdapter(new RecyclerOnItemClickListener<LastFollowModel>() {
            @Override
            public void onItemClicked(View view, int position, LastFollowModel model) {
                if (view.getId() == R.id.content) {
                    // TODO: 2019/4/24 跳到主页还是开始聊天？？？
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(OtherPersonFragment3.BUNDLE_USER_ID, model.getUserID());
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder((BaseActivity) getContext(), OtherPersonFragment3.class)
                            .setUseOldFragmentIfExist(false)
                            .setBundle(bundle)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .build());
                } else if (view.getId() == R.id.follow_tv) {
                    if (!model.isIsFollow() && !model.isIsFriend()) {
                        UserInfoManager.getInstance().mateRelation(model.getUserID(), UserInfoManager.RA_BUILD, model.isIsFriend());
                    }
                }
            }
        });

        mContentRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mContentRv.setAdapter(mLastFollowAdapter);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LastFollowEmptyCallback())
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                getLastRelations();
            }
        });
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        getLastRelations();
    }

    private void getLastRelations() {
        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.getLatestRelation(100), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<LastFollowModel> list = JSON.parseArray(result.getData().getString("users"), LastFollowModel.class);
                    showLastRelation(list);
                }
            }
        }, this);
    }

    private void showLastRelation(List<LastFollowModel> list) {
        mRefreshLayout.finishRefresh();
        if (list != null && list.size() > 0) {
            mLoadService.showSuccess();
            mLastFollowAdapter.setDataList(list);
        } else {
            mLoadService.showCallback(LastFollowEmptyCallback.class);
            MyLog.w(TAG, "showLastRelation" + " list=" + list);
        }

    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    protected boolean onBackPressed() {
        finish();
        return true;
    }

    @Override
    public void finish() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        /**
         * 如果没有通知栏权限，提示一次
         */
        if (U.getPermissionUtils().checkNotification(getContext())) {
            // 有权限
        } else {
            long lastShowTs = U.getPreferenceUtils().getSettingLong("show_go_notification_page", 0);
            if (System.currentTimeMillis() - lastShowTs > 24 * 3600 * 1000) {
                U.getPreferenceUtils().setSettingLong("show_go_notification_page", System.currentTimeMillis());
                SkrNotificationPermission skrNotificationPermission = new SkrNotificationPermission();
                skrNotificationPermission.ensurePermission(getActivity(), null, true);
            }
        }
    }

    /**
     * 别人关注的事件,所有的关系都是从我出发
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowNotifyEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        // TODO: 2019/4/24 可以再优化，暂时这么写
        getLastRelations();
    }

    /**
     * 自己主动关注或取关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        MyLog.d(TAG, "RelationChangeEvent" + " event type = " + event.type + " isFriend = " + event.isFriend);
        // TODO: 2019/4/24 可以再优化，暂时这么写
        getLastRelations();
    }
}
