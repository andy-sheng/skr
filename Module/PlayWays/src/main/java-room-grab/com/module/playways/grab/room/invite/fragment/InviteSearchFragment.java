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
import com.common.view.ex.NoLeakEditText;
import com.component.busilib.callback.EmptyCallback;
import com.component.busilib.model.SearchModel;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
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

    public final String TAG = "InviteSearchFragment";

    public static String INVITE_SEARCH_FROM = "invite_search_from";
    public static String INVITE_SEARCH_MODE = "invite_search_mode";
    public static String INVITE_ROOM_ID = "invite_room_id";
    public static String INVITE_TAG_ID = "invite_tag_id";

    private int mMode;
    private int mRoomID;
    private int mFrom;
    private int mTagID;

    RelativeLayout mSearchArea;
    TextView mCancleTv;
    NoLeakEditText mSearchContent;
    RecyclerView mRecyclerView;

    InviteFirendAdapter mInviteFirendAdapter;

    PublishSubject<SearchModel> mPublishSubject;

    InviteSearchPresenter mPresenter;

    private Boolean isAutoSearch = false;      // 标记是否是自动搜索
    private LoadService mLoadService;

    @Override
    public int initView() {
        return R.layout.invite_search_fragment;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mSearchArea = getRootView().findViewById(R.id.search_area);
        mCancleTv = getRootView().findViewById(R.id.cancle_tv);
        mSearchContent = getRootView().findViewById(R.id.search_content);
        mRecyclerView = getRootView().findViewById(R.id.recycler_view);

        mSearchContent.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMode = bundle.getInt(INVITE_SEARCH_MODE);
            mRoomID = bundle.getInt(INVITE_ROOM_ID);
            mFrom = bundle.getInt(INVITE_SEARCH_FROM);
            mTagID = bundle.getInt(INVITE_TAG_ID);
        }

        mPresenter = new InviteSearchPresenter(this);
        addPresent(mPresenter);
        mInviteFirendAdapter = new InviteFirendAdapter(new InviteFirendAdapter.OnInviteClickListener() {
            @Override
            public void onClick(UserInfoModel model, ExTextView view) {
                if (mFrom == InviteFriendFragment2.FROM_GRAB_ROOM) {
                    mPresenter.inviteFriend(mRoomID, mTagID, model, view);
                } else if (mFrom == InviteFriendFragment2.FROM_DOUBLE_ROOM) {
                    mPresenter.inviteDoubleFriend(mRoomID, model, view);
                } else if (mFrom == InviteFriendFragment2.FROM_MIC_ROOM) {
                    mPresenter.inviteMicFriend(mRoomID, model, view);
                }
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
                    mPublishSubject.onNext(new SearchModel(editable.toString(), true));
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
                        mPublishSubject.onNext(new SearchModel(keyword, false));
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

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new EmptyCallback(R.drawable.relation_follow_empty_icon, "空空如也", "#A2C7D9"))
                .build();
        mLoadService = mLoadSir.register(mRecyclerView, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {

            }
        });
    }


    private void initPublishSubject() {
        mPublishSubject = PublishSubject.create();
        if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            // 粉丝
            ApiMethods.subscribe(mPublishSubject.debounce(200, TimeUnit.MILLISECONDS).filter(new Predicate<SearchModel>() {
                @Override
                public boolean test(SearchModel s) {
                    return !TextUtils.isEmpty(s.getSearchContent());
                }
            }).switchMap(new Function<SearchModel, ObservableSource<ApiResult>>() {
                @Override
                public ObservableSource<ApiResult> apply(SearchModel model) {
                    isAutoSearch = model.isAutoSearch();
                    GrabRoomServerApi grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
                    return grabRoomServerApi.searchFans(model.getSearchContent());
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
            }, this);
        } else {
            // 好友
            ApiMethods.subscribe(mPublishSubject.debounce(200, TimeUnit.MILLISECONDS).filter(new Predicate<SearchModel>() {
                @Override
                public boolean test(SearchModel s) {
                    return !TextUtils.isEmpty(s.getSearchContent());
                }
            }).switchMap(new Function<SearchModel, ObservableSource<List<UserInfoModel>>>() {
                @Override
                public ObservableSource<List<UserInfoModel>> apply(SearchModel model) {
                    // TODO: 2019/5/23 区分好友和关注
                    isAutoSearch = model.isAutoSearch();
                    List<UserInfoModel> userInfoModels = UserInfoLocalApi.searchFollow(model.getSearchContent());
                    UserInfoManager.getInstance().fillUserOnlineStatus(userInfoModels, true, false);
                    return Observable.just(userInfoModels);
                }
            }), new ApiObserver<List<UserInfoModel>>() {
                @Override
                public void process(List<UserInfoModel> list) {
                    showUserInfoList(list);
                }
            }, this);
        }
    }


    @Override
    public void showUserInfoList(List<UserInfoModel> list) {
        // TODO: 2019/5/23 后续补充
        mInviteFirendAdapter.getDataList().clear();
        mInviteFirendAdapter.getDataList().addAll(list);
        mInviteFirendAdapter.notifyDataSetChanged();

        if (!isAutoSearch && mInviteFirendAdapter.getDataList().isEmpty()) {
            // 不是自动搜索才有空页面
            mLoadService.showCallback(EmptyCallback.class);
        }

        if (mInviteFirendAdapter.getDataList() != null && mInviteFirendAdapter.getDataList().size() > 0) {
            mLoadService.showSuccess();
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
