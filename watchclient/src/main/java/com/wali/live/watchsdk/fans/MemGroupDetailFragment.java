package com.wali.live.watchsdk.fans;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.wali.live.common.barrage.view.utils.FansInfoUtils;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.model.member.FansMemberModel;
import com.wali.live.watchsdk.fans.presenter.FansGroupDetailPresenter;
import com.wali.live.watchsdk.fans.presenter.IFansGroupDetailView;
import com.wali.live.watchsdk.fans.push.event.FansMemberUpdateEvent;
import com.wali.live.watchsdk.fans.setting.FansMedalSettingFragment;
import com.wali.live.watchsdk.fans.view.FansTaskView;
import com.wali.live.watchsdk.fans.view.merge.FansDetailBasicView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by lan on 2017/11/9.
 */
public class MemGroupDetailFragment extends BaseEventBusFragment implements View.OnClickListener, IFansGroupDetailView {
    private static final String EXTRA_ZUID = "extra_zuid";

    private BackTitleBar mTitleBar;

    private FansDetailBasicView mDetailBasicView;

    private ImageView mUnJoinGroupIv;
    private RelativeLayout mMyInfoArea;
    private TextView mMyMedalTv;
    private TextView mFansPetValueTv;
    private TextView mFansRankTv;

    private TextView mAccelerateStatusTv;
    private TextView mColourBarrageStatusTv;
    private TextView mFlyBarrageStatus;
    private TextView mForbiddenStatus;

    private FansTaskView mTaskView;

    private FansGroupDetailPresenter mDetailPresenter;

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
            return;
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

        mDetailBasicView = $(R.id.detail_basic_view);

        mUnJoinGroupIv = $(R.id.unjoin_group_iv);
        mMyInfoArea = $(R.id.my_info_area);
        mMyMedalTv = $(R.id.my_medal_tv);
        mFansPetValueTv = $(R.id.vfan_value_tv);
        mFansRankTv = $(R.id.vfan_rank_tv);

        mAccelerateStatusTv = $(R.id.accelerate_status);
        mColourBarrageStatusTv = $(R.id.colour_barrage_status);
        mFlyBarrageStatus = $(R.id.fly_barrage_status);
        mForbiddenStatus = $(R.id.forbidden_status);

        $click($(R.id.first_privilege_area), this);
        $click($(R.id.colour_barrage_area), this);
        $click($(R.id.fly_barrage_privilege_area), this);
        $click($(R.id.forbidden_privilege_area), this);

        mTaskView = $(R.id.task_view);

        initPresenter();
    }

    private void initPresenter() {
        mDetailPresenter = new FansGroupDetailPresenter(this);
        mDetailPresenter.getFansGroupDetail(mZuid);
        mDetailPresenter.getTopThreeMember(mZuid);
    }

    @Override
    public void setFansGroupDetail(FansGroupDetailModel model) {
        mGroupDetailModel = model;

        updateView();
    }

    private void updateView() {
        updateTitleArea();

        updateBasicArea();
        updateMyArea();
        updatePrivilegeArea();
        updateTaskArea();
    }

    private void updateTitleArea() {
        mTitleBar.setTitle(mGroupDetailModel.getGroupName());

        mTitleBar.getRightImageBtn().setOnClickListener(this);
        mTitleBar.getRightImageBtn().setImageResource(R.drawable.web_icon_relay_bg);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mTitleBar.getRightImageBtn().getLayoutParams();
        lp.rightMargin = DisplayUtils.dip2px(10f);
    }

    private void updateBasicArea() {
        mDetailBasicView.setGroupDetailModel(mGroupDetailModel);
    }

    private void updateMyArea() {
        if (mGroupDetailModel.getMemType() == VFansCommonProto.GroupMemType.NONE_VALUE) {
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

    private void updatePrivilegeArea() {
        if (FansInfoUtils.hasUpgradeAccelerationPrivilege(mGroupDetailModel.getMyPetLevel(), mGroupDetailModel.getVipLevel(), mGroupDetailModel.getVipExpire())) {
            Drawable drawable = GlobalData.app().getResources().getDrawable(R.drawable.live_pet_group_have_turned);
            drawable.setBounds(0, 0, DisplayUtils.dip2px(9.33f), DisplayUtils.dip2px(9.33f));
            mAccelerateStatusTv.setCompoundDrawables(drawable, null, null, null);
            mAccelerateStatusTv.setText(R.string.vfans_privilege_has_open);
        }
        if (FansInfoUtils.hasColorBarragePrivilege(mGroupDetailModel.getMyPetLevel(), mGroupDetailModel.getVipLevel(), mGroupDetailModel.getVipExpire())) {
            Drawable drawable = GlobalData.app().getResources().getDrawable(R.drawable.live_pet_group_have_turned);
            drawable.setBounds(0, 0, DisplayUtils.dip2px(9.33f), DisplayUtils.dip2px(9.33f));
            mColourBarrageStatusTv.setCompoundDrawables(drawable, null, null, null);
            mColourBarrageStatusTv.setText(R.string.vfans_privilege_has_open);
        }
        if (FansInfoUtils.hasFlyBarragePrivilege(mGroupDetailModel.getMyPetLevel(), mGroupDetailModel.getVipLevel(), mGroupDetailModel.getVipExpire())) {
            Drawable drawable = GlobalData.app().getResources().getDrawable(R.drawable.live_pet_group_have_turned);
            drawable.setBounds(0, 0, DisplayUtils.dip2px(9.33f), DisplayUtils.dip2px(9.33f));
            mFlyBarrageStatus.setCompoundDrawables(drawable, null, null, null);
            mFlyBarrageStatus.setText(R.string.vfans_privilege_has_open);
        }
        if (FansInfoUtils.hasBanPrivilege(mGroupDetailModel.getMyPetLevel(), mGroupDetailModel.getVipLevel(), mGroupDetailModel.getVipExpire())) {
            Drawable drawable = GlobalData.app().getResources().getDrawable(R.drawable.live_pet_group_have_turned);
            drawable.setBounds(0, 0, DisplayUtils.dip2px(9.33f), DisplayUtils.dip2px(9.33f));
            mForbiddenStatus.setCompoundDrawables(drawable, null, null, null);
            mForbiddenStatus.setText(R.string.vfans_privilege_has_open);
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
    public void notifyQuitGroupSuccess() {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FansMemberUpdateEvent event) {
        if (event != null) {
            if (mDetailPresenter != null) {
                mDetailPresenter.getFansGroupDetail(mZuid);
                mDetailPresenter.getTopThreeMember(mZuid);
            }
        }
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
        } else if (id == R.id.right_image_btn) {
            showMoreDialog();
        } else if (id == R.id.first_privilege_area) {
            FansPrivilegeFragment.openFragment((BaseSdkActivity) getActivity(), FansPrivilegeFragment.TYPE_UPGRADE_ACCELERATION);
        } else if (id == R.id.colour_barrage_area) {
            FansPrivilegeFragment.openFragment((BaseSdkActivity) getActivity(), FansPrivilegeFragment.TYPE_COLOR_BARRAGE);
        } else if (id == R.id.fly_barrage_privilege_area) {
            FansPrivilegeFragment.openFragment((BaseSdkActivity) getActivity(), FansPrivilegeFragment.TYPE_FREE_FLY_BARRAGE);
        } else if (id == R.id.forbidden_privilege_area) {
            FansPrivilegeFragment.openFragment((BaseSdkActivity) getActivity(), FansPrivilegeFragment.TYPE_BAN_BARRAGE);
        }
    }

    private void showMoreDialog() {
        if (mGroupDetailModel.getMemType() == VFansCommonProto.GroupMemType.OWNER_VALUE
                || mGroupDetailModel.getMemType() == VFansCommonProto.GroupMemType.ADMIN_VALUE
                || mGroupDetailModel.getMemType() == VFansCommonProto.GroupMemType.DEPUTY_ADMIN_VALUE) {
            new MyAlertDialog.Builder(getContext())
                    .setItems(new String[]{getString(R.string.vfans_set_group_title), getString(R.string.vfans_quit), getString(R.string.cancel)},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        FansMedalSettingFragment.openFragment((BaseSdkActivity) getActivity(), mZuid);
                                    } else if (which == 1) {
                                        quitGroup();
                                    }
                                }
                            })
                    .show();
        } else if (mGroupDetailModel.getMemType() != VFansCommonProto.GroupMemType.NONE_VALUE) {
            new MyAlertDialog.Builder(getContext())
                    .setItems(new String[]{getString(R.string.vfans_quit), getString(R.string.cancel)},
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 0) {
                                        quitGroup();
                                    }
                                }
                            })
                    .show();
        } else {
            ToastUtils.showToast(R.string.vfans_join_vfans_notice);
        }
    }

    private void quitGroup() {
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getContext());
        builder.setTitle(R.string.vfans_quit_group_confirm);
        builder.setMessage(R.string.leave_vfan_tip_message);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDetailPresenter.quitFansGroup(mGroupDetailModel.getZuid());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setAutoDismiss(true).show();
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
