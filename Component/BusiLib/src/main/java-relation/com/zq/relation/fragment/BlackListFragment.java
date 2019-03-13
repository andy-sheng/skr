package com.zq.relation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
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
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.IMsgService;
import com.zq.person.fragment.OtherPersonFragment;
import com.zq.relation.adapter.RelationAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

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
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
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
                    bundle.putSerializable(OtherPersonFragment.BUNDLE_USER_ID, userInfoModel.getUserId());
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder((BaseActivity) getContext(), OtherPersonFragment.class)
                            .setUseOldFragmentIfExist(false)
                            .setBundle(bundle)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .build());
                } else if (view.getId() == R.id.follow_tv) {
                    // 移除黑名单
                    IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
                    msgService.removeFromBlacklist(String.valueOf(userInfoModel.getUserId()), new ICallback() {
                        @Override
                        public void onSucess(Object obj) {
                            if (mRelationAdapter.getData() != null) {
                                mRelationAdapter.getData().remove(userInfoModel);
                                mRelationAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailed(Object obj, int errcode, String message) {

                        }
                    });
                }
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRelationAdapter);

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new LoadingCallback(R.drawable.wuhaoyou, "数据真的在加载中..."))
                .addCallback(new EmptyCallback(R.drawable.wuhaoyou, "黑名单为空"))
                .addCallback(new ErrorCallback(R.drawable.wuhaoyou, "请求出错了"))
                .setDefaultCallback(LoadingCallback.class)
                .build();
        mLoadService = mLoadSir.register(mRecyclerView, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                loadData();
            }
        });

        loadData();

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    private void loadData() {
        IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
        msgService.getBlacklist(new ICallback() {
            @Override
            public void onSucess(Object obj) {
                if (obj != null) {
                    String[] strings = (String[]) obj;
                    final List<Integer> useIDs = new ArrayList<>();
                    for (String string : strings) {
                        useIDs.add(Integer.valueOf(string));
                    }

                    UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("userIDs", JSON.toJSON(useIDs));

                    RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
                    ApiMethods.subscribe(userInfoServerApi.getUserInfos(body), new ApiObserver<ApiResult>() {
                        @Override
                        public void process(ApiResult result) {
                            if (result.getErrno() == 0) {
                                List<UserInfoModel> userInfoModels = JSON.parseArray(result.getData().getString("profiles"), UserInfoModel.class);
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
                    }, BlackListFragment.this);
                } else {
                    mLoadService.showCallback(EmptyCallback.class);
                }

            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {

            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }
}
