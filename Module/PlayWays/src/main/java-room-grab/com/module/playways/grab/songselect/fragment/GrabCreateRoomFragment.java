package com.module.playways.grab.songselect.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.component.busilib.constans.GrabRoomType;
import com.module.rank.R;

/**
 * 一唱到底，创建房间页面
 */
public class GrabCreateRoomFragment extends BaseFragment {

    public static final String KEY_ROOM_TYPE = "key_room_type";

    ExImageView mIvBack;
    ExRelativeLayout mFriendsRoom;
    ExRelativeLayout mSecretRoom;
    ExRelativeLayout mPublicRoom;

    @Override
    public int initView() {
        return R.layout.grab_create_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mFriendsRoom = (ExRelativeLayout) mRootView.findViewById(R.id.friends_room);
        mSecretRoom = (ExRelativeLayout) mRootView.findViewById(R.id.secret_room);
        mPublicRoom = (ExRelativeLayout) mRootView.findViewById(R.id.public_room);


        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(GrabCreateRoomFragment.this);
            }
        });

        mFriendsRoom.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_ROOM_TYPE, GrabRoomType.ROOM_TYPE_FRIEND);
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabCreateSpecialFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setBundle(bundle)
                        .build());
            }
        });

        mSecretRoom.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_ROOM_TYPE, GrabRoomType.ROOM_TYPE_SECRET);
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabCreateSpecialFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setBundle(bundle)
                        .build());
            }
        });

        mPublicRoom.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_ROOM_TYPE, GrabRoomType.ROOM_TYPE_PUBLIC);
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabCreateSpecialFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setBundle(bundle)
                        .build());
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
