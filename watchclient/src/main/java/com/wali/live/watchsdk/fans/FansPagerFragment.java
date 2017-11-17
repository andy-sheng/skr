package com.wali.live.watchsdk.fans;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.utils.display.DisplayUtils;
import com.base.view.SlidingTabLayout;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.adapter.CommonTabPagerAdapter;
import com.wali.live.watchsdk.channel.view.RepeatScrollView;
import com.wali.live.watchsdk.fans.dialog.ApplyJoinDialog;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.presenter.FansMemberPresenter;
import com.wali.live.watchsdk.fans.presenter.FansPagerPresenter;
import com.wali.live.watchsdk.fans.view.FansHomeView;
import com.wali.live.watchsdk.fans.view.FansMemberView;
import com.wali.live.watchsdk.fans.view.FansTaskView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.wali.live.component.view.Utils.$component;

/**
 * Created by zyh on 2017/11/8.
 *
 * @module 粉丝团页面
 */
public class FansPagerFragment extends RxFragment implements View.OnClickListener, FansPagerPresenter.IView {
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

    private View mApplyJoinArea;
    private TextView mApplyJoinBtn;

    private View mPrivilegeArea;
    private RepeatScrollView mRepeatScrollView;
    private TextView mPrivilegeOpenBtn;
    private TextView mScrollTv;

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

    private boolean mHasJoinGroup;
    private ApplyJoinDialog mApplyJoinDialog;

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
        mPresenter.getGroupDetailFromServer(mAnchorId);
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
                        break;
                    case POSITION_FAN_TASK:
                        break;
                    case POSITION_FAN_MEMBER:
                        break;
                    case POSITION_FAN_GROUP:
                        break;
                }
            }
        });
    }

    private void initBottomContainer() {
        mApplyJoinArea = $(R.id.apply_join_vfan_area);
        mApplyJoinBtn = $(R.id.join_vfans_btn);
        $click(mApplyJoinBtn, this);

        mPrivilegeArea = $(R.id.apply_privilege_area);
        mRepeatScrollView = $(R.id.repeat_scroll_view);
        mPrivilegeOpenBtn = $(R.id.open_privilege_btn);
        $click(mPrivilegeOpenBtn, this);

        mRepeatScrollView.init(R.layout.vfans_repeat_scroll_view, DisplayUtils.dip2px(66.67f));
        mScrollTv = (TextView) mRepeatScrollView.getChildView(0).findViewById(R.id.text_message);
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
            applyJoin();
        } else if (i == R.id.open_privilege_btn) {
        }
    }

    private void applyJoin() {
        if (mApplyJoinDialog == null) {
            mApplyJoinDialog = new ApplyJoinDialog(getActivity());
        }
        mApplyJoinDialog.show(mAnchorId, mRoomId, null);
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
                mApplyJoinArea.setVisibility(GONE);
            } else {
                mApplyJoinArea.setVisibility(VISIBLE);
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
