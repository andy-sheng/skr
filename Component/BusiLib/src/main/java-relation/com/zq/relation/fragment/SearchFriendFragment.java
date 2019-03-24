package com.zq.relation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.cache.BuddyCache;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.R;
import com.zq.person.fragment.OtherPersonFragment;
import com.zq.relation.adapter.RelationAdapter;
import com.zq.relation.callback.FansEmptyCallback;
import com.zq.relation.callback.FriendsEmptyCallback;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.common.core.userinfo.UserInfoManager.RELATION_FOLLOW;
import static com.common.core.userinfo.event.RelationChangeEvent.FOLLOW_TYPE;
import static com.common.core.userinfo.event.RelationChangeEvent.UNFOLLOW_TYPE;


public class SearchFriendFragment extends BaseFragment {

    CommonTitleBar mTitlebar;

    RecyclerView mSearchResult;
    LinearLayoutManager mLinearLayoutManager;

    RelationAdapter mRelationAdapter;

    int offset = 0;          //偏移量
    int DEFAULT_COUNT = 10;  // 每次拉去列表数目

    @Override
    public int initView() {
        return R.layout.search_friends_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mSearchResult = (RecyclerView) mRootView.findViewById(R.id.search_result);

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mSearchResult.setLayoutManager(mLinearLayoutManager);

        mRelationAdapter = new RelationAdapter(false, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                UserInfoModel userInfoModel = (UserInfoModel) model;
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
                        searchFriends(extra, offset, DEFAULT_COUNT);
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
        mTitlebar.showSoftInputKeyboard(true);
        mTitlebar.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getKeyBoardUtils().showSoftInputKeyBoard(getContext());
            }
        }, 200);
    }

    UserInfoManager.ResponseCallBack<ApiResult> mResponseCallBack = new UserInfoManager.ResponseCallBack<ApiResult>() {
        @Override
        public void onServerSucess(ApiResult result) {
            if (result.getErrno() == 0) {
                List<UserInfoModel> userInfoModels = JSON.parseArray(result.getData().getString("accounts"), UserInfoModel.class);
                int offset = result.getData().getIntValue("offset");
                refreshView(userInfoModels, offset);
            }
        }

        @Override
        public void onServerFailed() {

        }
    };

    private void searchFriends(String searchContent, int offset, int limit) {
        if (TextUtils.isEmpty(searchContent)) {
            U.getToastUtil().showShort("输入内容不能为空");
            return;
        }
        UserInfoManager.getInstance().searchFriendList(searchContent, offset, limit, mResponseCallBack);
    }

    private void refreshView(List<UserInfoModel> userInfoModels, int offset) {
        this.offset = offset;
        if (userInfoModels == null) {
            return;
        }

        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        mRelationAdapter.getData().clear();
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

        if (mRelationAdapter.getData() != null) {

            for (UserInfoModel userInfoModel : mRelationAdapter.getData()) {
                if (userInfoModel.getUserId() == event.useId) {
                    // TODO: 2019/3/21 更新关系

                }
            }
        }

    }
}
