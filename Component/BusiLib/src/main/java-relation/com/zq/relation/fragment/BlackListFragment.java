package com.zq.relation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.R;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.IMsgService;
import com.zq.live.proto.Common.ESex;
import com.zq.person.fragment.OtherPersonFragment;
import com.zq.relation.adapter.RelationAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单列表
 */
public class BlackListFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RecyclerView mRecyclerView;

    RelationAdapter mRelationAdapter;

    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 30; // 每次拉去最大值

    @Override
    public int initView() {
        return R.layout.relation_black_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);

        mRelationAdapter = new RelationAdapter(UserInfoManager.RELATION_BLACKLIST, new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                final UserInfoModel userInfoModel = (UserInfoModel) model;
                if (view.getId() == R.id.content) {
                    // 跳到他人的个人主页
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(OtherPersonFragment.BUNDLE_USER_MODEL, userInfoModel);
                    U.getFragmentUtils().addFragment(FragmentUtils
                            .newAddParamsBuilder((BaseActivity) getContext(), OtherPersonFragment.class)
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

        loadData(mOffset, DEFAULT_COUNT);
    }

    private void loadData(int offset, int default_count) {
        IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
        msgService.getBlacklist(new ICallback() {
            @Override
            public void onSucess(Object obj) {
                if (obj != null) {
                    String[] strings = (String[]) obj;

                    // TODO: 2019/2/17 仅做测试，等服务器接口
                    List<UserInfoModel> list = new ArrayList<>();
                    for (String string : strings) {
                        UserInfoModel userInfoModel = new UserInfoModel();
                        userInfoModel.setUserId(Integer.valueOf(string));
                        userInfoModel.setNickname("翠花");
                        userInfoModel.setAvatar(MyUserInfoManager.getInstance().getAvatar());
                        userInfoModel.setSex(ESex.SX_FEMALE.getValue());
                        list.add(userInfoModel);
                    }

                    mRelationAdapter.addData(list);
                    mRelationAdapter.notifyDataSetChanged();
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
}
