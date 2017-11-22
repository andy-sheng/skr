package com.wali.live.watchsdk.fans;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.utils.display.DisplayUtils;
import com.base.view.SlidingTabLayout;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.adapter.CommonTabPagerAdapter;
import com.wali.live.watchsdk.channel.view.IScrollListener;
import com.wali.live.watchsdk.channel.view.RepeatScrollView;
import com.wali.live.watchsdk.fans.dialog.ApplyJoinDialog;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.specific.RecentJobModel;
import com.wali.live.watchsdk.fans.pay.FansPayFragment;
import com.wali.live.watchsdk.fans.presenter.FansMemberPresenter;
import com.wali.live.watchsdk.fans.presenter.FansPagerPresenter;
import com.wali.live.watchsdk.fans.view.FansHomeView;
import com.wali.live.watchsdk.fans.view.FansMemberView;
import com.wali.live.watchsdk.fans.view.FansTaskView;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.wali.live.component.view.Utils.$component;

/**
 * Created by zyh on 2017/11/8.
 *
 * @module 粉丝团页面
 */
public class FansPagerFragment extends RxFragment implements View.OnClickListener, FansPagerPresenter.IView, FragmentDataListener {
    private static final String EXTRA_ANCHOR_ID = "extra_anchor_id";
    private static final String EXTRA_ROOMID = "extra_roomId";
    private static final String EXTRA_ANCHOR_NAME = "extra_anchor_name";
    private static final String EXTRA_MEMBER_TYPE = "extra_member_type";

    //粉丝团viewpager的page页面对应的position
    private final static int POSITION_FANS_HOME = 0;
    private final static int POSITION_FAN_TASK = 1;
    private final static int POSITION_FAN_MEMBER = 2;
    private final static int POSITION_FAN_GROUP = 3;

    private View mCoverView;
    private SlidingTabLayout mTabLayout;
    private CommonTabPagerAdapter mTabPagerAdapter;
    private ViewPager mViewPager;

    private View mJoinFansArea;
    private TextView mJoinFansBtn;

    private View mOpenPrivilegeArea;
    private RepeatScrollView mRepeatScrollView;
    private TextView mOpenPrivilegeBtn;
    private TextView mMessageTv1;
    private TextView mMessageTv2;

    //adapter加載的view
    private FansHomeView mFansHomeView;
    private FansTaskView mFansTaskView;
    private FansMemberView mFansMemberView;

    private String mAnchorName;
    private long mAnchorId;
    private String mRoomId;
    private int mMemberType;

    private FansPagerPresenter mPresenter;
    private FansGroupDetailModel mGroupDetailModel;

    private List<RecentJobModel> mRecentJobList;

    private boolean mHasJoinGroup;
    private ApplyJoinDialog mApplyJoinDialog;

    private boolean mShowApplyPrivilege = false;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_vfans, container, false);
    }

    @Override
    protected void bindView() {
        initData();
        initView();
        initPresenter();
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mAnchorName = bundle.getString(EXTRA_ANCHOR_NAME);
            mAnchorId = bundle.getLong(EXTRA_ANCHOR_ID);
            mRoomId = bundle.getString(EXTRA_ROOMID);
            mMemberType = bundle.getInt(EXTRA_MEMBER_TYPE);
        }
    }

    private void initView() {
        initTopContainer();
        initBottomContainer();
    }

    private void initPresenter() {
        mPresenter = new FansPagerPresenter(this);
        mPresenter.getGroupDetail(mAnchorId);
        mPresenter.getRecentJob(mAnchorId);
    }

    private void initTopContainer() {
        mCoverView = $(R.id.cover_view);
        $click(mCoverView, this);

        mTabLayout = $(R.id.vfan_tab);
        mTabLayout.setCustomTabView(R.layout.fans_tab_view, R.id.tab_tv);
        mTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_e5aa1e));
        mTabLayout.setDistributeMode(3);
        mTabLayout.setIndicatorWidth(DisplayUtils.dip2px(12));
        mTabLayout.setIndicatorBottomMargin(DisplayUtils.dip2px(6));
        mViewPager = $(R.id.vfans_pager);
        mTabPagerAdapter = new CommonTabPagerAdapter();
        // 设置数据
        mFansHomeView = new FansHomeView(getContext());
        mFansTaskView = new FansTaskView(getContext());
        mFansMemberView = new FansMemberView(getContext());
        $component(mFansMemberView, new FansMemberPresenter(mAnchorId));
        mTabPagerAdapter.addView(getString(R.string.vfans_homepage), mFansHomeView);
        mTabPagerAdapter.addView(getString(R.string.vfans_task), mFansTaskView);
        mTabPagerAdapter.addView(getString(R.string.vfans_member), mFansMemberView);
//        mTabPagerAdapter.addView(getString(R.string.vfans_group), new FansHomeView(getContext()));

        mTabPagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mTabPagerAdapter);
        mTabLayout.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case POSITION_FANS_HOME:
                        updateApplyPrivilegeArea(true);
                        break;
                    case POSITION_FAN_TASK:
                        updateApplyPrivilegeArea(true);
                        break;
                    case POSITION_FAN_MEMBER:
                        updateApplyPrivilegeArea(false);
                        break;
                    case POSITION_FAN_GROUP:
                        updateApplyPrivilegeArea(false);
                        break;
                }
            }
        });
    }

    private void initBottomContainer() {
        mJoinFansArea = $(R.id.apply_join_vfan_area);
        mJoinFansBtn = $(R.id.join_vfans_btn);
        $click(mJoinFansBtn, this);

        mOpenPrivilegeArea = $(R.id.open_privilege_area);
        mRepeatScrollView = $(R.id.repeat_scroll_view);
        mOpenPrivilegeBtn = $(R.id.open_privilege_btn);
        $click(mOpenPrivilegeBtn, this);

        mRepeatScrollView.init(R.layout.vfans_repeat_scroll_view, DisplayUtils.dip2px(66.67f));
        mMessageTv1 = $(mRepeatScrollView.getChildView(0), R.id.message_tv);
        mMessageTv2 = $(mRepeatScrollView.getChildView(1), R.id.message_tv);
    }

    private void finish() {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    @Override
    public boolean onBackPressed() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cover_view) {
            finish();
        } else if (i == R.id.join_vfans_btn) {
            joinFans();
        } else if (i == R.id.open_privilege_btn) {
            openPrivilege();
        }
    }

    private void joinFans() {
        if (mApplyJoinDialog == null) {
            mApplyJoinDialog = new ApplyJoinDialog(getActivity());
        }
        mApplyJoinDialog.show(mAnchorId, mRoomId, null);
    }

    private void openPrivilege() {
        FragmentNaviUtils.hideFragment(this, getActivity());
        FansPayFragment.open((BaseSdkActivity) getActivity(),
                mGroupDetailModel, mRoomId, mGroupDetailModel.getVipLevel() <= 0, true, this);
    }

    private void updateApplyPrivilegeArea(boolean isShow) {
        if (mShowApplyPrivilege != isShow) {
            mShowApplyPrivilege = isShow;

            if (mShowApplyPrivilege) {
                showApplyPrivilege();
            } else {
                hideApplyPrivilege();
            }
        }
    }

    private void showApplyPrivilege() {
        if (mHasJoinGroup && mAnchorId != UserAccountManager.getInstance().getUuidAsLong()) {
            if (mGroupDetailModel.getVipLevel() == 0) {
                mOpenPrivilegeBtn.setText(R.string.vfans_open_privilege);
            } else if (mGroupDetailModel.getVipLevel() > 0 && mGroupDetailModel.getVipExpire() > System.currentTimeMillis() / 1000) {
                mOpenPrivilegeBtn.setText(R.string.vfans_renew_pay);
            }

            mOpenPrivilegeArea.setVisibility(VISIBLE);

            if (mRecentJobList != null) {
                if (mRecentJobList.size() == 1) {
                    mRepeatScrollView.enterSingleMode();
                } else {
                    mRepeatScrollView.enterScrollMode();
                }
            }
        }
    }

    private void hideApplyPrivilege() {
        mOpenPrivilegeArea.setVisibility(GONE);

        mRepeatScrollView.stopTimer();
        mRepeatScrollView.stopAnimator();
    }

    @Override
    public void setGroupDetail(FansGroupDetailModel groupDetailModel) {
        mGroupDetailModel = groupDetailModel;

        mFansHomeView.setData(mAnchorName, mGroupDetailModel);
        mFansTaskView.setGroupDetailModel(mGroupDetailModel);
        mFansMemberView.updateGroupDetail(mGroupDetailModel);

        updateApplyArea();
    }

    private void updateApplyArea() {
        if (mAnchorId != UserAccountManager.getInstance().getUuidAsLong()) {
            mHasJoinGroup = mGroupDetailModel.getMemType() != VFansCommonProto.GroupMemType.NONE_VALUE;

            if (mHasJoinGroup) {
                mJoinFansArea.setVisibility(GONE);

                int index = mViewPager.getCurrentItem();
                if (index == POSITION_FANS_HOME || index == POSITION_FAN_TASK) {
                    updateApplyPrivilegeArea(true);
                }
            } else {
                mJoinFansArea.setVisibility(VISIBLE);
                updateApplyPrivilegeArea(false);
            }
        }
    }

    @Override
    public void setRecentJobList(List<RecentJobModel> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        mRecentJobList = list;
        mRepeatScrollView.setListener(new IScrollListener() {
            @Override
            public void onFirstIndexed() {
                bindItem(mRecentJobList.get(0), mMessageTv1);
            }

            @Override
            public void onIndexChanged(int index) {
                bindItem(mRecentJobList.get(index % mRecentJobList.size()), mMessageTv1);
                bindItem(mRecentJobList.get((index + 1) % mRecentJobList.size()), mMessageTv2);
            }
        });

        if (mShowApplyPrivilege) {
            showApplyPrivilege();
        }
    }

    protected void bindItem(RecentJobModel model, TextView tv) {
        String nickname = model.getNickname();
        if (nickname.length() > 5) {
            nickname = nickname.substring(0, 5) + "...";
        }
        int taskType = model.getGroupJobType();
        StringBuilder result = new StringBuilder().append(nickname).append(GlobalData.app().getString(R.string.vfans_task_ok));
        switch (taskType) {
            case VFansCommonProto.GroupJobType.GROUP_CHAT_VALUE:
                result.append(GlobalData.app().getString(R.string.vfans_daily_tasks_finish_send_barrage));
                break;
            case VFansCommonProto.GroupJobType.SEND_GIFT_VALUE:
                result.append(GlobalData.app().getString(R.string.vfans_daily_tasks_finish_send_gift));
                break;
            case VFansCommonProto.GroupJobType.SHARE_LIVE_VALUE:
                result.append(GlobalData.app().getString(R.string.vfans_daily_tasks_finish_share_live));
                break;
            case VFansCommonProto.GroupJobType.VIEW_LIVE_VALUE:
                result.append(GlobalData.app().getString(R.string.vfans_daily_tasks_finish_watch_live));
                break;
        }
        result.append(",").append(GlobalData.app().getString(R.string.vfans_task_ok_exp_plus)).append(model.getJobExp());
        tv.setText(result);
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
        if (requestCode == FansPayFragment.REQUEST_CODE_PAY) {
            FragmentNaviUtils.showFragment(this, getActivity());
            if (resultCode == Activity.RESULT_OK) {
                // 支付成功，重新拉一下数据
                mPresenter.getGroupDetail(mAnchorId);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mFansHomeView != null) {
            mFansHomeView.destroy();
        }
    }

    public static void openFragment(BaseSdkActivity activity, String anchorName,
                                    long zuid, String roomId, int memberType) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ANCHOR_NAME, anchorName);
        bundle.putLong(EXTRA_ANCHOR_ID, zuid);
        bundle.putString(EXTRA_ROOMID, roomId);
        bundle.putInt(EXTRA_MEMBER_TYPE, memberType);
        FragmentNaviUtils.openFragment(activity, FansPagerFragment.class, bundle, R.id.main_act_container,
                true, R.anim.slide_bottom_in, R.anim.slide_bottom_out);
    }
}
