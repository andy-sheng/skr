package com.wali.live.watchsdk.fans.pay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.recharge.view.RechargeFragment;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.pay.adapter.FansPayAdapter;
import com.wali.live.watchsdk.fans.pay.decoration.PaySpacesItemDecoration;
import com.wali.live.watchsdk.fans.pay.model.FansPayModel;
import com.wali.live.watchsdk.fans.pay.presenter.FansPayPresenter;
import com.wali.live.watchsdk.fans.pay.presenter.IFansPayView;

import java.util.List;

import static com.wali.live.statistics.StatisticsKey.Recharge.FROM_OTHER;
import static com.wali.live.statistics.StatisticsKey.Recharge.RECHARGE_FROM;

/**
 * Created by lan on 2017/11/21.
 */
public class FansPayFragment extends RxFragment implements View.OnClickListener, IFansPayView {
    private static final String EXTRA_GROUP_DETAIL = "extra_group_detail";
    private static final String EXTRA_FIRST_OPEN = "extra_first_open_data";
    private static final String EXTRA_FROM_LIVE = "extra_from_live";
    private static final String EXTRA_ROOM_ID = "extra_room_id";

    public static final int REQUEST_CODE_PAY = 1000;
    public static final int SPAN_COUNT = 4;

    private View mBgView;

    private BaseImageView mAvatarIv;
    private TextView mGroupNameTv;

    private ImageView mBannerIv;
    private TextView mTipsTv;

    private RecyclerView mPayRv;
    private FansPayAdapter mPayAdapter;
    private LinearLayoutManager mLayoutManager;

    private TextView mOpenPrivilegeBtn;

    private TextView mYearTv;

    private FansGroupDetailModel mGroupDetailModel;
    private String mRoomId;
    private Boolean mIsFirstOpen;
    private boolean mOpenFromLive;

    private FansPayPresenter mPayPresenter;
    private Animator mAnimator1;
    private Animator mAnimator2;
    private AnimatorSet mAnimatorSet;

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

        mGroupDetailModel = (FansGroupDetailModel) bundle.getSerializable(EXTRA_GROUP_DETAIL);
        mRoomId = bundle.getString(EXTRA_ROOM_ID);
        mIsFirstOpen = bundle.getBoolean(EXTRA_FIRST_OPEN, true);
        mOpenFromLive = bundle.getBoolean(EXTRA_FROM_LIVE, true);
    }

    @Override
    public int getRequestCode() {
        return REQUEST_CODE_PAY;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_fans_pay, container, false);
    }

    @Override
    protected void bindView() {
        mBgView = mRootView.findViewById(R.id.bg_view);
        mBgView.setOnClickListener(this);

        mGroupNameTv = (TextView) mRootView.findViewById(R.id.group_name_tv);
        mAvatarIv = (BaseImageView) mRootView.findViewById(R.id.group_avatar);

        mBannerIv = $(R.id.banner_iv);
        mPayRv = $(R.id.recycler_view);
        mTipsTv = $(R.id.tips_tv);

        $click(mOpenPrivilegeBtn = $(R.id.open_privilege_btn), this);

        mYearTv = $(R.id.year_tv);

        updateMyArea();
        updatePayListArea();
        updateOtherArea();

        initPresenter();
    }

    private void updateMyArea() {
        HttpImage httpImage = new HttpImage(AvatarUtils.getAvatarUrlByUid(mGroupDetailModel.getZuid(), 0));
        httpImage.setHeight(DisplayUtils.dip2px(30));
        httpImage.setWidth(DisplayUtils.dip2px(30));
        httpImage.setIsCircle(true);
        FrescoWorker.loadImage(mAvatarIv, httpImage);

        mGroupNameTv.setText(mGroupDetailModel.getGroupName());
    }

    private void updatePayListArea() {
        mPayAdapter = new FansPayAdapter();
        mPayRv.setAdapter(mPayAdapter);

        RecyclerView.ItemAnimator animator = mPayRv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        mPayRv.addItemDecoration(new PaySpacesItemDecoration(DisplayUtils.dip2px(10)));

        mLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT);
        mPayRv.setLayoutManager(mLayoutManager);
        mPayRv.setHasFixedSize(true);
    }

    private void updateOtherArea() {
        if (!mIsFirstOpen) {
            mOpenPrivilegeBtn.setText(R.string.vfans_continue_privilege_now);
            mBannerIv.setImageResource(R.drawable.pet_group_privilege_banner);
        }

        mTipsTv.setText(GlobalData.app().getString(R.string.vfans_pay_privilige_one_tips));
    }

    private void initPresenter() {
        mPayPresenter = new FansPayPresenter(this);
        mPayPresenter.getPayList();
//        mPayPresenter.getVfansPrivilegePermit(mGroupDetailModel.getZuid(), MyUserInfoManager.getInstance().getUid());
    }

    @Override
    public void setPayList(List<FansPayModel> list) {
        mPayAdapter.setDataList(list);
    }

    @Override
    public void notifyPayResult(int errorCode, int giftId) {
        if (errorCode == ErrorCode.CODE_SUCCESS) {
            notifyPaySuccess(giftId);
        } else if (errorCode == ErrorCode.GIFT_PAY_BARRAGE) {
            MyAlertDialog dialog = new MyAlertDialog.Builder(this.getActivity()).create();
            dialog.setTitle(R.string.account_withdraw_pay_user_account_not_enough);
            dialog.setMessage(getContext().getString(R.string.account_vfans_privilege_pay_user_account_not_enough_tip));
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.recharge),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(RECHARGE_FROM, FROM_OTHER);
                            RechargeFragment.openFragment(getActivity(), R.id.main_act_container, bundle, true);
                            dialog.dismiss();
                        }
                    });
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            dialog.setCancelable(false);
            dialog.show();
        } else if (errorCode == ErrorCode.VFANS_RPIVILEGE_GIFT_OVER_LIMIT) {
            ToastUtils.showToast(getResources().getQuantityString(
                    R.plurals.vfans_pay_privilige_over_limit_tips, 3, 3));
        } else if (errorCode == ErrorCode.VFANS_PRIVILEGE_GIFT_GROUP_NOT_EXIST) {
            ToastUtils.showToast(R.string.vfans_pay_privilige_group_not_exist_tips);
        } else if (errorCode == ErrorCode.VFANS_PRIVILEGE_GIFT_NOT_IN_GROUP) {
            ToastUtils.showToast(R.string.vfans_pay_privilige_group_not_be_member_tips);
        } else {
            if (mIsFirstOpen) {
                ToastUtils.showToast(getString(R.string.vfans_first_by_privilege_faild));
            } else {
                ToastUtils.showToast(getString(R.string.vfans_renew_by_privilege_faild));
            }
            MyLog.e(TAG, "pay failure=" + errorCode);
        }
    }

    private void notifyPaySuccess(int giftId) {
        if (mIsFirstOpen) {
            ToastUtils.showToast(R.string.vfans_first_by_privilege_success);
        } else {
            ToastUtils.showToast(R.string.vfans_renew_by_privilege_success);
        }

        MyLog.d(TAG, "pay success=" + giftId);
        if (giftId == 189732) {
            // 年费会员giftid 仅此一处，就不加静态变量了
            // yearAnimate();
            finishOk();
        } else {
            finishOk();
        }
    }

    private void yearAnimate() {
        if (mAnimator1 == null) {
            PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
            PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("translationY", 0f, -200f);
            mAnimator1 = ObjectAnimator.ofPropertyValuesHolder(mYearTv, holder1, holder2);
            mAnimator1.setDuration(600);
        }
        if (mAnimator2 == null) {
            mAnimator2 = ObjectAnimator.ofFloat(mYearTv, "alpha", 1f, 0f);
            mAnimator2.setDuration(200);
            mAnimator2.setStartDelay(1600);
        }
        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishOk();
                }
            });
        }
        mAnimatorSet.playSequentially(mAnimator1, mAnimator2);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.open_privilege_btn) {
            if (mPayAdapter.getSelectedItem() != null) {
                mPayPresenter.buyPayGift(mPayAdapter.getSelectedItem().getGift(), mGroupDetailModel.getZuid(), mRoomId);
            } else {
                ToastUtils.showToast(R.string.vfans_choose_privilege);
            }
        } else if (id == R.id.bg_view) {
            finishCanceled();
        }
    }

    @Override
    public boolean onBackPressed() {
        finishCanceled();
        return true;
    }

    private void finishOk() {
        if (mDataListener != null) {
            mDataListener.onFragmentResult(getRequestCode(), Activity.RESULT_OK, null);
        }
        finish();
    }

    private void finishCanceled() {
        if (mDataListener != null) {
            mDataListener.onFragmentResult(getRequestCode(), Activity.RESULT_CANCELED, null);
        }
        finish();
    }

    private void finish() {
        FragmentNaviUtils.popFragment(getActivity());
    }

    public static FansPayFragment open(BaseSdkActivity activity, FansGroupDetailModel groupDetailModel,
                                       String roomId, boolean isFirstOpen, boolean isFromLive,
                                       FragmentDataListener fragmentDataListener) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_GROUP_DETAIL, groupDetailModel);
        bundle.putSerializable(EXTRA_FIRST_OPEN, isFirstOpen);
        bundle.putBoolean(EXTRA_FROM_LIVE, isFromLive);
        bundle.putString(EXTRA_ROOM_ID, roomId);

        int[] anim = {R.anim.slide_bottom_in, R.anim.slide_bottom_out, R.anim.slide_bottom_in, R.anim.slide_bottom_out};

        FansPayFragment fragment = (FansPayFragment) FragmentNaviUtils.addFragment(activity,
                R.id.main_act_container, FansPayFragment.class, bundle, true, true, anim, true);
        fragment.initDataResult(REQUEST_CODE_PAY, fragmentDataListener);
        return fragment;
    }
}
