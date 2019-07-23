package com.module.playways.grab.room.guide.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.room.prepare.model.PrepareData;

public class GrabGuideResultFragment extends BaseFragment {

    GrabRoomData mRoomData;

    ImageView mBgIv;
    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mTvBack;
    ExTextView mTvAgain;

    @Override
    public int initView() {
        return R.layout.grab_guide_result_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mBgIv = (ImageView) mRootView.findViewById(R.id.bg_iv);
        mAvatarBg = (ImageView) mRootView.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mTvBack = (ExTextView) mRootView.findViewById(R.id.tv_back);
        mTvAgain = (ExTextView) mRootView.findViewById(R.id.tv_again);


        mTvBack.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mTvAgain.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                PrepareData prepareData = new PrepareData();
                prepareData.setGameType(GameModeType.GAME_MODE_GRAB);
                prepareData.setTagId(mRoomData.getTagId());
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_GRAB_MATCH_ROOM)
                        .withSerializable("prepare_data", prepareData)
                        .navigation();

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (GrabRoomData) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        return super.onBackPressed();
    }
}
