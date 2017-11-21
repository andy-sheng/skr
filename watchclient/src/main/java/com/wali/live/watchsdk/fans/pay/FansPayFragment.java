package com.wali.live.watchsdk.fans.pay;

import android.app.Activity;
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
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.utils.display.DisplayUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.pay.adapter.FansPayAdapter;
import com.wali.live.watchsdk.fans.pay.decoration.PaySpacesItemDecoration;
import com.wali.live.watchsdk.fans.pay.model.FansPayModel;
import com.wali.live.watchsdk.fans.pay.presenter.FansPayPresenter;
import com.wali.live.watchsdk.fans.pay.presenter.IFansPayView;

import java.util.List;

/**
 * Created by lan on 2017/11/21.
 */
public class FansPayFragment extends RxFragment implements View.OnClickListener, IFansPayView {
    private static final String EXTRA_GROUP_DETAIL = "extra_group_detail";
    private static final String EXTRA_FIRST_OPEN = "extra_first_open_data";
    private static final String EXTRA_FROM_LIVE = "extra_from_live";
    private static final String EXTRA_ROOM_ID = "extra_room_id";

    public static final int REQUEST_CODE_PAY = 1000;

    private static final int SPAN_COUNT = 4;

    private View mBgView;

    private BaseImageView mAvatarIv;
    private TextView mGroupNameTv;

    private RecyclerView mPayRv;
    private FansPayAdapter mPayAdapter;
    private LinearLayoutManager mLayoutManager;

    private TextView mOpenPrivilegeBtn;
    private TextView mTipsTv;
    private ImageView mBannerIv;

    private FansGroupDetailModel mGroupDetailModel;
    private String mRoomId;
    private Boolean mIsFirstOpen;
    private boolean mOpenFromLive;

    private FansPayPresenter mPayPresenter;

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

        mOpenPrivilegeBtn = $(R.id.open_privilege_btn);
        mOpenPrivilegeBtn.setOnClickListener(this);

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
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.open_privilege_btn) {
//            if (mVfansPayAdapter.getSelectedItem() != null) {
//                mPayPresenter.buy(mVfansPayAdapter.getSelectedItem(), mGroupDetailModel.getZuid(), mRoomId);
//            } else {
//                ToastUtils.showToast(R.string.vfans_choose_privilege);
//            }
        } else if (i == R.id.bg_view) {
            if (mDataListener != null) {
                mDataListener.onFragmentResult(getRequestCode(), Activity.RESULT_CANCELED, null);
            }
            finish();

        }
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
