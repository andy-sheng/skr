package com.component.busilib.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.flowlayout.FlowLayout;
import com.common.flowlayout.TagAdapter;
import com.common.flowlayout.TagFlowLayout;
import com.common.image.fresco.BaseImageView;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;


public class OtherPersonFragment extends BaseFragment {
    private String[] mVals = new String[]{"北京市/昌平", "22岁", "粉丝/345"};

    RelativeLayout mPersonMainContainner;
    BaseImageView mAvatarIv;
    ExImageView mBackIv;
    ExTextView mShareTv;
    ExTextView mNameTv;
    ExTextView mSignTv;
    TagFlowLayout mFlowlayout;
    ExTextView mFollowTv;
    ExTextView mMessageTv;

    @Override
    public int initView() {
        return R.layout.other_person_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mPersonMainContainner = (RelativeLayout)mRootView.findViewById(R.id.person_main_containner);
        mAvatarIv = (BaseImageView)mRootView.findViewById(R.id.avatar_iv);
        mBackIv = (ExImageView)mRootView.findViewById(R.id.back_iv);
        mShareTv = (ExTextView)mRootView.findViewById(R.id.share_tv);
        mNameTv = (ExTextView)mRootView.findViewById(R.id.name_tv);
        mSignTv = (ExTextView)mRootView.findViewById(R.id.sign_tv);
        mFlowlayout = (TagFlowLayout)mRootView.findViewById(R.id.flowlayout);
        mFollowTv = (ExTextView)mRootView.findViewById(R.id.follow_tv);
        mMessageTv = (ExTextView)mRootView.findViewById(R.id.message_tv);

        // TODO: 2018/12/26 可能会变，先写死
        mFlowlayout.setAdapter(new TagAdapter<String>(mVals) {
            @Override
            public View getView(FlowLayout parent, int position, String o) {
                ExTextView tv = (ExTextView) LayoutInflater.from(getContext()).inflate(R.layout.tag_textview,
                        mFlowlayout, false);
                tv.setText(o);
                return tv;
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
