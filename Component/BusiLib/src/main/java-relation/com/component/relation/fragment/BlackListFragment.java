package com.component.relation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.R;
import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.callback.ErrorCallback;
import com.component.busilib.callback.LoadingCallback;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.RouterConstants;
import com.component.relation.adapter.RelationAdapter;

import java.util.List;

/**
 * 黑名单列表
 */
public class BlackListFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RecyclerView mRecyclerView;

    RelationAdapter mRelationAdapter;

    LoadService mLoadService;

    @Override
    public int initView() {
        return R.layout.relation_black_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) getRootView().findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mRecyclerView = (RecyclerView) getRootView().findViewById(R.id.recycler_view);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                U.getFragmentUtils().popFragment(BlackListFragment.this);
            }
        });

        mRelationAdapter = new RelationAdapter(UserInfoManager.RELATION_BLACKLIST, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                final UserInfoModel userInfoModel = (UserInfoModel) model;
                if (view.getId() == R.id.content) {
                    // 跳到他人的个人主页
                    Bundle bundle = new Bundle();
                    bundle.putInt("bundle_user_id", userInfoModel.getUserId());
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation();
                } else if (view.getId() == R.id.follow_tv) {
                    // 移除黑名单
                    UserInfoManager.getInstance().removeBlackList(userInfoModel.getUserId(), new UserInfoManager.ResponseCallBack() {
                        @Override
                        public void onServerSucess(Object o) {
                            if (mRelationAdapter.getData() != null) {
                                mRelationAdapter.getData().remove(userInfoModel);
                                mRelationAdapter.notifyDataSetChanged();
                            }

                            if (mRelationAdapter.getData() != null && mRelationAdapter.getData().size() > 0) {
                                mLoadService.showSuccess();
                            } else {
                                mLoadService.showCallback(EmptyCallback.class);
                            }
                        }

                        @Override
                        public void onServerFailed() {

                        }
                    });
                }
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRelationAdapter);

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LoadingCallback(R.drawable.blacklist_empty_icon, "数据真的在加载中..."))
                .addCallback(new EmptyCallback(R.drawable.blacklist_empty_icon, "暂无黑名单", "#993B4E79"))
                .addCallback(new ErrorCallback(R.drawable.blacklist_empty_icon, "请求出错了"))
                .setDefaultCallback(LoadingCallback.class)
                .build();
        mLoadService = mLoadSir.register(mRecyclerView, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                loadData();
            }
        });

        loadData();

        U.getSoundUtils().preLoad(getTAG(), R.raw.normal_back);
    }

    private void loadData() {
        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.getBlackList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<UserInfoModel> userInfoModels = JSONObject.parseArray(result.getData().getString("users"), UserInfoModel.class);
                    if (userInfoModels != null && userInfoModels.size() > 0) {
                        mLoadService.showSuccess();
                        mRelationAdapter.addData(userInfoModels);
                        mRelationAdapter.notifyDataSetChanged();
                    } else {
                        mLoadService.showCallback(EmptyCallback.class);
                    }
                } else {
                    mLoadService.showCallback(ErrorCallback.class);
                }

            }
        }, this);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(getTAG());
    }
}
