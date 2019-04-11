package com.zq.relation.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.UserInfoModel;
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
import com.component.busilib.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.person.fragment.OtherPersonFragment2;
import com.zq.relation.adapter.RelationAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SearchFriendFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mSearchResult;
    LinearLayoutManager mLinearLayoutManager;

    RelationAdapter mRelationAdapter;


    String lastSearchContent;  // 上次搜索内容
    String lastAutoSearchContent; // 自动搜索的内容
    int offset = 0;          //偏移量
    int DEFAULT_COUNT = 20;  // 每次拉去列表数目

    CompositeDisposable mCompositeDisposable;
    PublishSubject<String> mPublishSubject;
    DisposableObserver<ApiResult> mDisposableObserver;

    @Override
    public int initView() {
        return R.layout.search_friends_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mSearchResult = (RecyclerView) mRootView.findViewById(R.id.search_result);

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mSearchResult.setLayoutManager(mLinearLayoutManager);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                searchFriends(lastSearchContent, offset, DEFAULT_COUNT, true, true);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        mRelationAdapter = new RelationAdapter(UserInfoManager.NO_RELATION, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                UserInfoModel userInfoModel = (UserInfoModel) model;
                if (view.getId() == R.id.content) {
                    // 跳到他人的个人主页
                    U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(OtherPersonFragment2.BUNDLE_USER_ID, userInfoModel.getUserId());
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder((BaseActivity) getContext(), OtherPersonFragment2.class)
                            .setUseOldFragmentIfExist(false)
                            .setBundle(bundle)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .build());
                } else if (view.getId() == R.id.follow_tv) {
                    if (userInfoModel.isFriend() || userInfoModel.isFollow()) {
                        // 取消关系
                        UserInfoManager.getInstance().mateRelation(userInfoModel.getUserId(), UserInfoManager.RA_UNBUILD, userInfoModel.isFriend());
                    } else {
                        // 建立关系
                        UserInfoManager.getInstance().mateRelation(userInfoModel.getUserId(), UserInfoManager.RA_BUILD, userInfoModel.isFriend());
                    }
                }
            }
        });
        mSearchResult.setAdapter(mRelationAdapter);

        mTitlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                switch (action) {
                    case CommonTitleBar.ACTION_SEARCH_SUBMIT:
                        searchFriends(extra, 0, DEFAULT_COUNT, false, true);
                        break;
                    case CommonTitleBar.ACTION_SEARCH_DELETE:
                        mTitlebar.getCenterSearchEditText().setText("");
                        break;
                }
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                U.getFragmentUtils().popFragment(SearchFriendFragment.this);
            }
        });

        initPublishSubject();

        mTitlebar.getCenterSearchEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPublishSubject.onNext(editable.toString());
            }
        });

        mTitlebar.showSoftInputKeyboard(true);

        mTitlebar.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getKeyBoardUtils().showSoftInputKeyBoard(getContext());
            }
        }, 200);
    }

    private void initPublishSubject() {
        mPublishSubject = PublishSubject.create();
        mDisposableObserver = new DisposableObserver<ApiResult>() {
            @Override
            public void onNext(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<UserInfoModel> userInfoModels = JSON.parseArray(result.getData().getString("accounts"), UserInfoModel.class);
                    int offset = result.getData().getIntValue("offset");
                    lastSearchContent = lastAutoSearchContent;
                    refreshView(userInfoModels, offset, false, false);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        mPublishSubject.debounce(200, TimeUnit.MILLISECONDS).filter(new Predicate<String>() {
            @Override
            public boolean test(String s) throws Exception {
                return s.length() > 0;
            }
        }).switchMap(new Function<String, ObservableSource<ApiResult>>() {
            @Override
            public ObservableSource<ApiResult> apply(String string) throws Exception {
                UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
                lastAutoSearchContent = string;
                return userInfoServerApi.searchFriendsList(string, 0, DEFAULT_COUNT).subscribeOn(Schedulers.io());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(mDisposableObserver);
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.add(mDisposableObserver);
    }

    private void searchFriends(final String searchContent, int offset, int limit, final boolean isLoadMore, final boolean isHideKeyBoard) {
        if (TextUtils.isEmpty(searchContent)) {
            U.getToastUtil().showShort("输入内容不能为空");
            return;
        }
        UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(userInfoServerApi.searchFriendsList(searchContent, offset, limit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    lastSearchContent = searchContent;
                    List<UserInfoModel> userInfoModels = JSON.parseArray(result.getData().getString("accounts"), UserInfoModel.class);
                    int offset = result.getData().getIntValue("offset");
                    refreshView(userInfoModels, offset, isLoadMore, isHideKeyBoard);
                }
            }
        }, this);
    }

    private void refreshView(List<UserInfoModel> userInfoModels, int offset, boolean isLoadMore, boolean isHideKeyBoard) {
        this.offset = offset;
        if (userInfoModels == null) {
            if (isLoadMore) {
                // 没有更多了
                mRefreshLayout.setEnableLoadMore(false);
                mRefreshLayout.finishLoadMore();
            }
            return;
        } else {
            // 恢复下拉更多
            mRefreshLayout.setEnableLoadMore(true);
        }

        if (isHideKeyBoard) {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        }

        if (!isLoadMore) {
            mRelationAdapter.getData().clear();
        } else {
            mRefreshLayout.finishLoadMore();
        }
        mRelationAdapter.getData().addAll(userInfoModels);
        mRelationAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RelationChangeEvent event) {
        MyLog.d(TAG, "RelationChangeEvent" + " event type = " + event.type + " isFriend = " + event.isFriend);
        // 只更新此页面上的数据
        if (mRelationAdapter.getData() != null && mRelationAdapter.getData().size() != 0) {
            for (int i = 0; i < mRelationAdapter.getData().size(); i++) {
                if (mRelationAdapter.getData().get(i).getUserId() == event.useId) {
                    UserInfoModel model = mRelationAdapter.getData().get(i);
                    model.setFriend(event.isFriend);
                    model.setFollow(event.isFollow);
                    mRelationAdapter.getData().set(i, model);
                    mRelationAdapter.notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowNotifyEvent event) {
        MyLog.d(TAG, "FollowNotifyEvent" + " event=" + event);
        // 只更新此页面上的数据
        if (mRelationAdapter.getData() != null && mRelationAdapter.getData().size() != 0) {
            for (int i = 0; i < mRelationAdapter.getData().size(); i++) {
                if (mRelationAdapter.getData().get(i).getUserId() == event.mUserInfoModel.getUserId()) {
                    mRelationAdapter.getData().set(i, event.mUserInfoModel);
                    mRelationAdapter.notifyItemChanged(i);
                    return;
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
    }
}
