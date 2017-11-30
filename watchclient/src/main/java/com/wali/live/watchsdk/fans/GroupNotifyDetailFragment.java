package com.wali.live.watchsdk.fans;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.BaseSdkActivity;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.date.DateTimeUtils;
import com.base.utils.network.NetworkUtils;
import com.base.utils.toast.ToastUtils;
import com.base.view.BackTitleBar;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.notification.ApplyJoinFansModel;
import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;
import com.wali.live.watchsdk.fans.presenter.GroupNotifyDetailPresenter;
import com.wali.live.watchsdk.fans.push.type.GroupNotifyType;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by zyh on 2017/11/24.
 */

public class GroupNotifyDetailFragment extends RxFragment implements View.OnClickListener, GroupNotifyDetailPresenter.IView {
    private final String TAG = "GroupNotifyDetailFragment";
    public final static String GROUP_NOTIFY_INFO_MODEL = "groupNotifyInfoModel";

    private BackTitleBar mTitleBar;
    private SimpleDraweeView mAvatarDv;
    private TextView mNameTv;
    private ImageView mArrowIv;
    private TextView mContentTv;
    private TextView mTimeTv;
    private TextView mApplyTv;
    private ViewGroup mPersonContainer;

    private TextView mTitleTv;
    private TextView mDenyTv;
    private TextView mAgreeTv;
    private TextView mAccountTv;
    private ViewGroup mApplyContainer;

    private GroupNotifyBaseModel mGroupNotifyBaseModel;
    private GroupNotifyDetailPresenter mPresenter;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.layout_group_notify_apply_detail, container, false);
    }

    @Override
    protected void bindView() {
        initData();
        initView();
        updateView();
        initPresenter();
    }

    private void initView() {
        mTitleBar = $(R.id.back_title_bar);
        mTitleBar.setTitle(R.string.vfans_notify);
        $click(mTitleBar.getBackBtn(), this);
        mAvatarDv = $(R.id.avatar_iv);
        mNameTv = $(R.id.nickname_tv);
        mAccountTv = $(R.id.account_tv);
        mArrowIv = $(R.id.right_arrow_info);
        mArrowIv.setVisibility(View.GONE);
        mTitleTv = $(R.id.notify_title_tv);
        mContentTv = $(R.id.notify_content_tv);
        mTimeTv = $(R.id.notify_time);
        mApplyTv = $(R.id.single_tv);
        mDenyTv = $(R.id.deny_tv);
        mAgreeTv = $(R.id.agree_tv);
        mApplyContainer = $(R.id.apply_status_container);
        mPersonContainer = $(R.id.personal_info_container);
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mGroupNotifyBaseModel = (GroupNotifyBaseModel) bundle.getSerializable(GROUP_NOTIFY_INFO_MODEL);
        }
    }

    private void updateView() {
        switch (mGroupNotifyBaseModel.getNotificationType()) {
            case GroupNotifyType.APPLY_JOIN_GROUP_NOTIFY:
            case GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY:
            case GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY:
                bindApplyJoinDetail();
                break;
            case GroupNotifyType.BE_GROUP_MANAGER_NOTIFY:
            case GroupNotifyType.CANCEL_GROUP_MANAGER_NOTIFY:
            case GroupNotifyType.BE_GROUP_MEM_NOTIFY:
            case GroupNotifyType.REMOVE_GROUP_MEM_NOTIFY:
//            case  GroupNotifyType.FORBID_GROUP_MEM_NOTIFY:
//            case GroupNotifyType.CANCEL_FORBID_GROUP_MEM_NOTIFY:
            case GroupNotifyType.GROUP_MEM_DISBAND_GROUP_NOTIFY:
                bindInfoDetail();
                break;
        }
    }

    private void initPresenter() {
        mPresenter = new GroupNotifyDetailPresenter(this);
    }

    private void bindApplyJoinDetail() {
        AvatarUtils.loadAvatarByUidTs(mAvatarDv, mGroupNotifyBaseModel.getCandidate(),
                mGroupNotifyBaseModel.getCandidateTs(), true);
        mNameTv.setText(mGroupNotifyBaseModel.getCandidateName());
        mAccountTv.setText(getString(R.string.group_account, mGroupNotifyBaseModel.getCandidate()));

        SpannableStringBuilder titleSpan = new SpannableStringBuilder();
        String title = String.format(getResources().getString(R.string.group_apply_join), mGroupNotifyBaseModel.getGroupName());
        titleSpan.append(title);
        titleSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.cash_color)),
                getResources().getString(R.string.group_apply_join_title).length(), titleSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTitleTv.setText(titleSpan);
        String time = DateTimeUtils.formatTimeStringForConversation(getContext(), mGroupNotifyBaseModel.getTs());
        mTimeTv.setText(time);
        switch (mGroupNotifyBaseModel.getNotificationType()) {
            case GroupNotifyType.APPLY_JOIN_GROUP_NOTIFY: {
                ApplyJoinFansModel applyJoinFansModel = (ApplyJoinFansModel) mGroupNotifyBaseModel;
                String reason = applyJoinFansModel.getApplyMsg();
                mContentTv.setText(reason);
                mApplyTv.setVisibility(GONE);
                mAgreeTv.setVisibility(VISIBLE);
                mDenyTv.setVisibility(VISIBLE);
                $click(mAgreeTv, this);
                $click(mDenyTv, this);
            }
            break;
            case GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY:
            case GroupNotifyType.REJECT_JOIN_GROUP_NOTIFY: {
                mApplyTv.setVisibility(VISIBLE);
                mAgreeTv.setVisibility(GONE);
                mDenyTv.setVisibility(GONE);
                mContentTv.setText(mGroupNotifyBaseModel.getMsg());
                int resId = mGroupNotifyBaseModel.getNotificationType() == GroupNotifyType.AGREE_JOIN_GROUP_NOTIFY
                        ? R.string.group_apply_access_txt : R.string.group_apply_reject_txt;
                mApplyTv.setText(resId);
            }
            break;
        }
    }

    private void bindInfoDetail() {
        mApplyContainer.setVisibility(View.GONE);
        mContentTv.setVisibility(View.GONE);
        mAccountTv.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(mGroupNotifyBaseModel.getGroupIcon())) {
            FrescoWorker.loadImage(mAvatarDv, ImageFactory.newHttpImage(mGroupNotifyBaseModel
                    .getGroupIcon())
                    .setIsCircle(true)
                    .build());
        } else {
            AvatarUtils.loadAvatarByUidTs(mAvatarDv, mGroupNotifyBaseModel.getGroupOwner(),
                    mGroupNotifyBaseModel.getGroupOwnerTs(), true);
        }
        mNameTv.setText(mGroupNotifyBaseModel.getGroupName());
        String time = DateTimeUtils.formatTimeStringForConversation(getContext(), mGroupNotifyBaseModel.getTs());
        mTimeTv.setText(time);
        mTitleTv.setText(mGroupNotifyBaseModel.getMsgBrief());
        $click(mPersonContainer, this);
    }

    @Override
    public void onHandleSuccess(GroupNotifyBaseModel model) {
        mGroupNotifyBaseModel = model;
        updateView();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back_iv) {
            finish();
            return;
        }
        if (!NetworkUtils.hasNetwork(GlobalData.app())) {
            ToastUtils.showToast(R.string.network_disable);
            return;
        }
        if (id == R.id.agree_tv || id == R.id.deny_tv) {
            mPresenter.handleJoinGroup((ApplyJoinFansModel) mGroupNotifyBaseModel, id == R.id.agree_tv);
        } else if (id == R.id.personal_info_container) {
            MemGroupDetailFragment.open((BaseSdkActivity) getActivity(), mGroupNotifyBaseModel.getGroupOwner());
        }
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static void openFragment(BaseActivity activity, GroupNotifyBaseModel model) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(GROUP_NOTIFY_INFO_MODEL, model);
        FragmentNaviUtils.addFragmentToBackStack(activity, R.id.main_act_container,
                GroupNotifyDetailFragment.class, bundle, true, R.anim.slide_right_in,
                R.anim.slide_right_out);
    }
}
