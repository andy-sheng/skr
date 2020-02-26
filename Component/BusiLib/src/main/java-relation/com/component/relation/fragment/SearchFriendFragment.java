package com.component.relation.fragment;

import android.app.Activity;
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

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoLocalApi;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.utils.UserInfoDataUtils;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.rxretrofit.ControlType;
import com.common.rxretrofit.RequestControl;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.component.relation.adapter.RelationAdapter;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class SearchFriendFragment extends BaseFragment {

    public final String TAG = "SearchFriendFragment";

    public static String BUNDLE_SEARCH_MODE = "bundle_search_mode";

    private int mMode;

    RelativeLayout mSearchArea;
    TextView mCancleTv;
    EditText mSearchContent;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;

    PublishSubject<String> mPublishSubject;

    RelationAdapter mRelationAdapter;

    TipsDialogView tipsDialogView;

    public String mExtra = "";

    int mFrom = 0;  //默认为0，1为从赠送礼物来的,2为申请变成某种关系

    @Override
    public int initView() {
        return R.layout.search_friends_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mSearchArea = (RelativeLayout) getRootView().findViewById(R.id.search_area);
        mCancleTv = (TextView) getRootView().findViewById(R.id.cancle_tv);
        mSearchContent = (EditText) getRootView().findViewById(R.id.search_content);
        mRefreshLayout = (SmartRefreshLayout) getRootView().findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) getRootView().findViewById(R.id.recycler_view);

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
                    bundle.putInt("bundle_user_id", userInfoModel.getUserId());
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation();
                    U.getKeyBoardUtils().hideSoftInput(mSearchContent);
                } else if (view.getId() == R.id.follow_tv) {
                    // 关注和好友都是有关系的人
                    if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                        if (!userInfoModel.isFriend()) {
                            UserInfoManager.getInstance().mateRelation(userInfoModel.getUserId(), UserInfoManager.RA_BUILD, userInfoModel.isFriend());
                        }
                    }
                } else if (view.getId() == R.id.send_tv) {
                    if (mFrom == 1) {
                        showGiveDialog(userInfoModel);
                    } else if (mFrom == 2) {
                        checkRelation(userInfoModel);
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
            ApiMethods.subscribe(mPublishSubject.debounce(300, TimeUnit.MILLISECONDS).filter(new Predicate<String>() {
                @Override
                public boolean test(String s) throws Exception {
                    return s.length() > 0;
                }
            }).switchMap(new Function<String, ObservableSource<ApiResult>>() {
                @Override
                public ObservableSource<ApiResult> apply(String key) {
                    UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
                    return userInfoServerApi.searchFans(key);
                }
            }).retry(100), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult obj) {
                    List<JSONObject> list = JSON.parseArray(obj.getData().getString("fans"), JSONObject.class);
                    List<UserInfoModel> resultList = UserInfoDataUtils.parseRoomUserInfo(list);
                    if (resultList != null) {
                        showUserInfoList(resultList);
                    }
                }
            }, this);
        } else {
            ApiMethods.subscribe(mPublishSubject.debounce(300, TimeUnit.MILLISECONDS).filter(new Predicate<String>() {
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
                        UserInfoManager.getInstance().fillUserOnlineStatus(r, false, false, 0, 0);
                    } else if (mMode == UserInfoManager.RELATION.FOLLOW.getValue()) {
                        r = UserInfoLocalApi.searchFollow(string);
                        UserInfoManager.getInstance().fillUserOnlineStatus(r, false, true, 0, 0);
                    }
                    return Observable.just(r);
                }
            }).retry(100), new ApiObserver<List<UserInfoModel>>() {
                @Override
                public void process(List<UserInfoModel> list) {
                    MyLog.d(TAG, "onNext" + " list=" + list);
                    // TODO: 2019/5/23 一次搞定，不需要做分页
                    showUserInfoList(list);
                }
            }, this);
        }
    }

    private void checkRelation(UserInfoModel userInfoModel) {
        if (U.getStringUtils().isJSON(mExtra)) {
            UserInfoServerApi userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
            JSONObject jsonObject = JSONObject.parseObject(mExtra);
            HashMap map = new HashMap();
            map.put("goodsID", jsonObject.getIntValue("goodsID"));
            map.put("otherUserID", userInfoModel.getUserId());
            RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
            ApiMethods.subscribe(userInfoServerApi.checkCardRelation(body), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        String msg = result.getData().getString("noticeMsg");
                        showInviteCardDialog(userInfoModel, msg);
                    } else {
                        //ErrAlreadyHasRelation           = 8428114; //对方已是你的闺蜜，不能再发送邀请哦～
                        //ErrApplyAfter24Hour             = 8428115; //24小时之后才能再次发送关系申请哦～
                        //ErrAlreadyHasOtherRelation      = 8428116; //对方已是你的闺蜜，对方接受邀请将自动解除你们原来的关系哦～
                        if (8428114 == result.getErrno()) {
                            U.getToastUtil().showShort(result.getErrmsg());
                        } else if (8428115 == result.getErrno()) {
                            U.getToastUtil().showShort(result.getErrmsg());
                        } else if (8428116 == result.getErrno()) {
                            showInviteCardDialog(userInfoModel, result.getErrmsg());
                        }
                    }
                }
            }, new RequestControl("checkCardRelation", ControlType.CancelLast));
        }
    }

    /**
     * 邀请好友发生关系
     *
     * @param userInfoModel
     */
    private void showInviteCardDialog(UserInfoModel userInfoModel, String msg) {
        if (tipsDialogView != null) {
            tipsDialogView.dismiss(false);
        }

        IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();

        tipsDialogView = new TipsDialogView.Builder((Activity) getContext())
                .setMessageTip(msg)
                .setCancelBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tipsDialogView != null) {
                            tipsDialogView.dismiss(false);
                        }
                    }
                })
                .setCancelTip("取消")
                .setConfirmBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((Activity) getContext()).finish();
                        channelService.inviteToRelationCardFinish(userInfoModel.getUserId());
                    }
                })
                .setConfirmTip("邀请")
                .build();
        tipsDialogView.showByDialog();
    }

    private void showGiveDialog(UserInfoModel userInfoModel) {
        if (tipsDialogView != null) {
            tipsDialogView.dismiss(false);
        }

        IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();

        tipsDialogView = new TipsDialogView.Builder((Activity) getContext())
                .setMessageTip("是否赠送给" + userInfoModel.getNickname() + channelService.getSelectedMallName() + "?")
                .setCancelBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tipsDialogView != null) {
                            tipsDialogView.dismiss(false);
                        }
                    }
                })
                .setCancelTip("取消")
                .setConfirmBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        channelService.selectGiveMallUserFinish(userInfoModel.getUserId());
                        ((Activity) getContext()).finish();
                    }
                })
                .setConfirmTip("赠送")
                .build();
        tipsDialogView.showByDialog();
    }

    @Override
    public void setData(int type, @org.jetbrains.annotations.Nullable Object data) {
        if (type == 1) {
            mFrom = (Integer) data;
        } else if (type == 2) {
            mExtra = (String) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mPublishSubject != null) {
            mPublishSubject.onComplete();
        }
    }
}
