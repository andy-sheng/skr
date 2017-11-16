package com.wali.live.watchsdk.fans;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.view.BackTitleBar;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.fans.presenter.FansGroupDetailPresenter;
import com.wali.live.watchsdk.fans.presenter.IFansGroupDetailView;
import com.wali.live.watchsdk.fans.utils.FansInfoUtils;
import com.wali.live.watchsdk.fans.view.FansTaskView;
import com.wali.live.watchsdk.fans.view.merge.FansDetailBasicView;

import java.util.List;

/**
 * Created by lan on 2017/11/9.
 */
public class MemGroupDetailFragment extends RxFragment implements View.OnClickListener, IFansGroupDetailView {
    private static final String EXTRA_ZUID = "extra_zuid";

    private BackTitleBar mTitleBar;

    private FansDetailBasicView mDetailBasicView;

    private ImageView mUnJoinGroupIv;
    private RelativeLayout mMyInfoArea;
    private TextView mMyMedalTv;
    private TextView mFansPetValueTv;
    private TextView mFansRankTv;

    private FansTaskView mTaskView;

    private FansGroupDetailPresenter mFansGroupDetailPresenter;

    private long mZuid;
    private FansGroupDetailModel mGroupDetailModel;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        initData(args);
    }

    private void initData(Bundle bundle) {
        if (bundle == null) {
            finish();
        }
        mZuid = bundle.getLong(EXTRA_ZUID);
        MyLog.d(TAG, "user id=" + mZuid);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_mem_group_detail, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.getBackBtn().setOnClickListener(this);

        mTitleBar.getRightImageBtn().setImageResource(R.drawable.web_icon_relay_bg);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mTitleBar.getRightImageBtn().getLayoutParams();
        lp.rightMargin = DisplayUtils.dip2px(10f);

        mDetailBasicView = $(R.id.detail_basic_view);

        mUnJoinGroupIv = $(R.id.unjoin_group_iv);
        mMyInfoArea = $(R.id.my_info_area);
        mMyMedalTv = $(R.id.my_medal_tv);
        mFansPetValueTv = $(R.id.vfan_value_tv);
        mFansRankTv = $(R.id.vfan_rank_tv);

        mTaskView = $(R.id.task_view);

        initPresenter();
    }

    private void initPresenter() {
        mFansGroupDetailPresenter = new FansGroupDetailPresenter(this);
        mFansGroupDetailPresenter.getFansGroupDetail(mZuid);
        mFansGroupDetailPresenter.getTopThreeMember(mZuid);
    }

    @Override
    public void setFansGroupDetail(FansGroupDetailModel model) {
        mGroupDetailModel = model;

        updateView();
    }

    private void updateView() {
        mTitleBar.setTitle(mGroupDetailModel.getGroupName());

        updateBasicArea();
        updateMyArea();
        updateTaskArea();
    }

    private void updateBasicArea() {
        mDetailBasicView.setGroupDetailModel(mGroupDetailModel);
    }

    private void updateMyArea() {
        if (mGroupDetailModel.getMemType() == VFansCommonProto.GroupMemType.NONE.getNumber()) {
            //没有入团的
            mMyInfoArea.setVisibility(View.GONE);
            mUnJoinGroupIv.setVisibility(View.VISIBLE);
        } else {
            //入团了的
            mMyInfoArea.setVisibility(View.VISIBLE);
            mUnJoinGroupIv.setVisibility(View.GONE);

            mMyMedalTv.setText(mGroupDetailModel.getMedalValue());
            mMyMedalTv.setBackgroundResource(FansInfoUtils.getGroupMemberLevelDrawable(mGroupDetailModel.getMyPetLevel()));
            mFansPetValueTv.setText(String.valueOf(mGroupDetailModel.getMyPetExp()));
            mFansRankTv.setText(mGroupDetailModel.getPetRanking() + "/" + mGroupDetailModel.getCurrentMember());
        }
    }

    private void updateTaskArea() {
        mTaskView.setGroupDetailModel(mGroupDetailModel);
    }

    @Override
    public void setTopThreeMember(List<FansMemberModel> memberList) {
        mDetailBasicView.setTopThreeMember(memberList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTaskView != null) {
            mTaskView.destroy();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() == null) {
            return false;
        }
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_iv) {
            finish();
        }
    }

    private void finish() {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void open(BaseActivity baseActivity, long zuid) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ZUID, zuid);
        FragmentNaviUtils.addFragmentToBackStack(baseActivity, R.id.main_act_container, MemGroupDetailFragment.class,
                bundle, true, R.anim.slide_right_in, R.anim.slide_right_out);

    }
}
