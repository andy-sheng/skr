package com.module.playways.grab.room.invite;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoLocalApi;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.module.playways.R;
import com.module.playways.grab.room.model.GrabFriendModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;

public class InviteSearchFragment extends BaseFragment {

    public final static String TAG = "InviteSearchFragment";

    public static String INVITE_SEARCH_MODE = "invite_search_mode";
    public static String INVITE_ROOM_ID = "invite_room_id";

    private int mMode;
    private int mRoomID;

    RelativeLayout mSearchArea;
    TextView mCancleTv;
    EditText mSearchContent;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;

    InviteFirendAdapter mInviteFirendAdapter;

    CompositeDisposable mCompositeDisposable;
    PublishSubject<String> mPublishSubject;
    DisposableObserver<List<UserInfoModel>> mDisposableObserver;  // 关注和好友使用
    DisposableObserver<ApiResult> mFansDisposableObserver;        // 粉丝使用

    InviteSearchPresenter mPresenter;

    @Override
    public int initView() {
        return R.layout.invite_search_fragment;
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
            mMode = bundle.getInt(INVITE_SEARCH_MODE);
            mRoomID = bundle.getInt(INVITE_ROOM_ID);
        }

        mPresenter = new InviteSearchPresenter();
        mInviteFirendAdapter = new InviteFirendAdapter(new InviteFirendAdapter.OnInviteClickListener() {
            @Override
            public void onClick(GrabFriendModel model) {
                // TODO: 2019/5/23 邀请
                mPresenter.inviteFriend(mRoomID, model);
            }

            @Override
            public void onClickSearch() {
                // TODO: 2019/5/23  donothing
            }
        }, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mInviteFirendAdapter);

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
                U.getFragmentUtils().popFragment(InviteSearchFragment.this);
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
                    searchFriends(mSearchContent.getText().toString().trim());
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

    private void searchFriends(String trim) {

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
                public ObservableSource<List<UserInfoModel>> apply(String string) throws Exception {
                    // TODO: 2019/5/23 区分好友和关注
                    List<UserInfoModel> userInfoModels = UserInfoLocalApi.searchFollow(string);
                    return Observable.just(userInfoModels);
                }
            }).observeOn(AndroidSchedulers.mainThread()).
                    subscribe(mDisposableObserver);
            mCompositeDisposable = new CompositeDisposable();
            mCompositeDisposable.add(mDisposableObserver);
        }

    }

    private void showUserInfoList(List<UserInfoModel> list) {
        // TODO: 2019/5/23 后续补充
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
