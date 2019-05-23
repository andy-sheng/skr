package com.zq.relation.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoLocalApi;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.person.fragment.OtherPersonFragment3;
import com.zq.relation.adapter.RelationAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SearchFriendFragment extends BaseFragment {

    public final static String TAG = "SearchFriendFragment";

    public static String BUNDLE_SEARCH_MODE = "bundle_search_mode";

    private int mMode;

    RelativeLayout mSearchArea;
    TextView mCancleTv;
    EditText mSearchContent;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;

    PublishSubject<String> mPublishSubject;
    DisposableObserver<List<UserInfoModel>> mDisposableObserver;  // 关注和好友使用
    DisposableObserver<ApiResult> mFansDisposableObserver;     //粉丝使用

    RelationAdapter mRelationAdapter;

    @Override
    public int initView() {
        return R.layout.search_friends_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mSearchArea = (RelativeLayout) mRootView.findViewById(R.id.search_area);
        mCancleTv = (TextView) mRootView.findViewById(R.id.cancle_tv);
        mSearchContent = (EditText) mRootView.findViewById(R.id.search_content);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mSearchContent.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMode = bundle.getInt(BUNDLE_SEARCH_MODE);
        }

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRelationAdapter = new RelationAdapter(mMode, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                UserInfoModel userInfoModel = (UserInfoModel) model;
                if (view.getId() == R.id.content) {
                    // 跳到他人的个人主页
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(OtherPersonFragment3.BUNDLE_USER_ID, userInfoModel.getUserId());
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder((BaseActivity) getContext(), OtherPersonFragment3.class)
                            .setUseOldFragmentIfExist(false)
                            .setBundle(bundle)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .build());
                } else if (view.getId() == R.id.follow_tv) {
                    // 关注和好友都是有关系的人
                    if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                        if (!userInfoModel.isFriend()) {
                            UserInfoManager.getInstance().mateRelation(userInfoModel.getUserId(), UserInfoManager.RA_BUILD, userInfoModel.isFriend());
                        }
                    }
                }
            }
        });
        mRecyclerView.setAdapter(mRelationAdapter);

        if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            mRefreshLayout.setEnableLoadMore(true);
            mRefreshLayout.setEnableRefresh(false);
        } else {
            // 非粉丝，不让刷新和加载更多
            mRefreshLayout.setEnableLoadMore(false);
            mRefreshLayout.setEnableRefresh(false);
        }
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                // TODO: 2019/5/23 加载更多 只是粉丝有该选项
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        mCancleTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                U.getFragmentUtils().popFragment(SearchFriendFragment.this);
            }
        });

        initPublishSubject();
        mSearchContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mPublishSubject != null) {
                    mPublishSubject.onNext(editable.toString());
                }
            }
        });

        mSearchContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = mSearchContent.getText().toString().trim();
                    if (TextUtils.isEmpty(keyword)) {
                        U.getToastUtil().showShort("搜索内容为空");
                        return false;
                    }
                    if (mPublishSubject != null) {
                        mPublishSubject.onNext(keyword);
                    }
                    U.getKeyBoardUtils().hideSoftInput(mSearchContent);
                }
                return false;
            }
        });

        mSearchContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearchContent.requestFocus();
                U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity());
            }
        }, 200);
    }

    UserInfoManager.UserInfoListCallback userInfoListCallback = new UserInfoManager.UserInfoListCallback() {
        @Override
        public void onSuccess(UserInfoManager.FROM from, int offset, List<UserInfoModel> list) {
            showUserInfoList(list);
        }
    };

    private void showUserInfoList(List<UserInfoModel> list) {
        mRelationAdapter.setData(list);
    }

    private void initPublishSubject() {
        mPublishSubject = PublishSubject.create();
        if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            mFansDisposableObserver = new DisposableObserver<ApiResult>() {
                @Override
                public void onNext(ApiResult result) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            };
            // TODO: 2019/5/23 等服务器粉丝搜索接口补充
        } else {
            mDisposableObserver = new DisposableObserver<List<UserInfoModel>>() {
                @Override
                public void onNext(List<UserInfoModel> list) {
                    MyLog.d(TAG, "onNext" + " list=" + list);
                    // TODO: 2019/5/23 一次搞定，不需要做分页
                    showUserInfoList(list);
                }

                @Override
                public void onError(Throwable e) {
                    MyLog.e(e);
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
            }).switchMap(new Function<String, ObservableSource<List<UserInfoModel>>>() {
                @Override
                public ObservableSource<List<UserInfoModel>> apply(String string) {
                    List<UserInfoModel> r = null;
                    if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                        r = UserInfoLocalApi.searchFriends(string);
                    } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
                        r = UserInfoLocalApi.searchFollow(string);
                    } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                    } else {
                    }
                    return Observable.just(r);
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).
                    subscribe(mDisposableObserver);
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mFansDisposableObserver != null) {
            mFansDisposableObserver.dispose();
        }
    }
}
