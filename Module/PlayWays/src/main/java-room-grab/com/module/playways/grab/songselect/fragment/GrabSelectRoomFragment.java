package com.module.playways.grab.songselect.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.songselect.adapter.FriendRoomAdapter;
import com.module.playways.grab.songselect.model.FriendRoomModel;
import com.module.playways.grab.songselect.view.SpecialSelectView;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.List;

public class GrabSelectRoomFragment extends BaseFragment {

    ExImageView mGameCreate;
    ExImageView mSelectBack;
    RelativeLayout mFriendsArea;
    ExTextView mFriendsTv;
    ExTextView mMoreTv;
    RecyclerView mFriendsRecycle;
    ExTextView mFastBeginTv;
    SpecialSelectView mSpecialView;

    FriendRoomAdapter mFriendRoomAdapter;

    @Override
    public int initView() {
        return R.layout.grab_create_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mGameCreate = (ExImageView) mRootView.findViewById(R.id.game_create);
        mSelectBack = (ExImageView) mRootView.findViewById(R.id.select_back);
        mFriendsArea = (RelativeLayout) mRootView.findViewById(R.id.friends_area);
        mFriendsTv = (ExTextView) mRootView.findViewById(R.id.friends_tv);
        mMoreTv = (ExTextView) mRootView.findViewById(R.id.more_tv);
        mFriendsRecycle = (RecyclerView) mRootView.findViewById(R.id.friends_recycle);
        mFastBeginTv = (ExTextView) mRootView.findViewById(R.id.fast_begin_tv);
        mSpecialView = (SpecialSelectView) mRootView.findViewById(R.id.special_view);


        mSelectBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mFriendsRecycle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mFriendRoomAdapter = new FriendRoomAdapter();
        mFriendsRecycle.setAdapter(mFriendRoomAdapter);

        // TODO: 2019/3/20 测试数据
        FriendRoomModel friendRoomModel = new FriendRoomModel(MyUserInfoManager.getInstance().getAvatar(), MyUserInfoManager.getInstance().getNickName());

        List<FriendRoomModel> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add(friendRoomModel);
        }

        mFriendRoomAdapter.setDataList(list);
        mFriendRoomAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
