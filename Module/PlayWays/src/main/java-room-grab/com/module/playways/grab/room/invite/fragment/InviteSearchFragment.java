package com.module.playways.grab.room.invite.fragment;

import android.os.Bundle;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoLocalApi;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.utils.UserInfoDataUtils;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.dialog.view.StrokeTextView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.invite.adapter.InviteFirendAdapter;
import com.module.playways.grab.room.invite.presenter.InviteSearchPresenter;
import com.module.playways.grab.room.invite.view.IInviteSearchView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;

public class InviteSearchFragment extends BaseFragment implements IInviteSearchView {

    public final static String TAG = "InviteSearchFragment";

    public static String INVITE_SEARCH_MODE = "invite_search_mode";
    public static String INVITE_ROOM_ID = "invite_room_id";

    private int mMode;
    private int mRoomID;

    RelativeLayout mSearchArea;
    TextView mCancleTv;
    EditText mSearchContent;
    RecyclerView mRecyclerView;

    InviteFirendAdapter mInviteFirendAdapter;

    PublishSubject<String> mPublishSubject;

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
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mSearchContent.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMode = bundle.getInt(INVITE_SEARCH_MODE);
            mRoomID = bundle.getInt(INVITE_ROOM_ID);
        }

        mPresenter = new InviteSearchPresenter(this);
        addPresent(mPresenter);
        mInviteFirendAdapter = new InviteFirendAdapter(new InviteFirendAdapter.OnInviteClickListener() {
            @Override
            public void onClick(UserInfoModel model, ExTextView view) {
                mPresenter.inviteFriend(mRoomID, model, view);
            }

            @Override
            public void onClickSearch() {

            }
        }, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mInviteFirendAdapter);

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


    private void initPublishSubject() {
        mPublishSubject = PublishSubject.create();
        if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            // 粉丝
            ApiMethods.subscribe(mPublishSubject.debounce(200, TimeUnit.MILLISECONDS).filter(new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    return s.length() > 0;
                }
            }).switchMap(new Function<String, ObservableSource<ApiResult>>() {
                @Override
                public ObservableSource<ApiResult> apply(String s) {
                    GrabRoomServerApi grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
                    return grabRoomServerApi.searchFans(s);
                }
            }), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        List<JSONObject> list = JSON.parseArray(result.getData().getString("fans"), JSONObject.class);
                        List<UserInfoModel> userInfoModels = UserInfoDataUtils.parseRoomUserInfo(list);
                        showUserInfoList(userInfoModels);
                    }
                }
            },this);
        } else {
            // 好友
            ApiMethods.subscribe(mPublishSubject.debounce(200, TimeUnit.MILLISECONDS).filter(new Predicate<String>() {
                @Override
                public boolean test(String s) {
                    return s.length() > 0;
                }
            }).switchMap(new Function<String, ObservableSource<List<UserInfoModel>>>() {
                @Override
                public ObservableSource<List<UserInfoModel>> apply(String string) {
                    // TODO: 2019/5/23 区分好友和关注
                    List<UserInfoModel> userInfoModels = UserInfoLocalApi.searchFollow(string);
                    UserInfoManager.getInstance().fillUserOnlineStatus(userInfoModels, true);
                    return Observable.just(userInfoModels);
                }
            }), new ApiObserver<List<UserInfoModel>>() {
                @Override
                public void process(List<UserInfoModel> list) {
                    showUserInfoList(list);
                }
            },this);
        }
    }


    @Override
    public void showUserInfoList(List<UserInfoModel> list) {
        // TODO: 2019/5/23 后续补充
        if (list != null && list.size() > 0) {
            mInviteFirendAdapter.getDataList().clear();
            mInviteFirendAdapter.getDataList().addAll(list);
            mInviteFirendAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void updateInvited(ExTextView view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            view.setClickable(false);
            view.setAlpha(0.5f);
            view.setText("已邀请");
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
