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
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.HttpImage;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.view.BackTitleBar;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.model.FansGroupDetailModel;
import com.wali.live.watchsdk.fans.presenter.FansGroupDetailPresenter;
import com.wali.live.watchsdk.fans.presenter.IFansGroupDetailView;
import com.wali.live.watchsdk.fans.view.VfansProgressView;

import rx.Observable;

/**
 * Created by lan on 2017/11/9.
 */
public class MemGroupDetailFragment extends RxFragment implements View.OnClickListener, IFansGroupDetailView {
    private static final String EXTRA_ZUID = "extra_zuid";

    private BackTitleBar mTitleBar;

    private BaseImageView mCoverIv;
    private TextView mFansNameTv;
    private ImageView mCharmTitleIv;
    private TextView mLevelTv;

    protected VfansProgressView mCharmPv;

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

        mCoverIv = $(R.id.cover_iv);
        mFansNameTv = (TextView) mRootView.findViewById(R.id.vfan_name_tv);
        mCharmTitleIv = (ImageView) mRootView.findViewById(R.id.charm_title_iv);
        mLevelTv = (TextView) mRootView.findViewById(R.id.level_tv);

        mCharmPv = (VfansProgressView) mRootView.findViewById(R.id.charm_pv);

        initPresenter();
    }

    private void initPresenter() {
        mFansGroupDetailPresenter = new FansGroupDetailPresenter(this);
        mFansGroupDetailPresenter.getFansGroupDetail(mZuid);
    }

    @Override
    public void getFansGroupDetailSuccess(FansGroupDetailModel model) {
        mGroupDetailModel = model;

        updateView();
    }

    private void updateView() {
        mTitleBar.setTitle(mGroupDetailModel.getGroupName());

        HttpImage coverImage = new HttpImage(AvatarUtils.getAvatarUrlByUid(mGroupDetailModel.getZuid(), 0));
        coverImage.setIsCircle(true);
        coverImage.setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_a));
        coverImage.setFailureDrawable(GlobalData.app().getResources().getDrawable(R.drawable.avatar_default_a));
        FrescoWorker.loadImage(mCoverIv, coverImage);

        mFansNameTv.setText(mGroupDetailModel.getGroupName());
//        mCharmTitleIv.setImageResource(FansInfoUtils.getImageResoucesByCharmLevelValue(mGroupDetailModel.getCharmLevel()));
        mLevelTv.setText("Lv." + mGroupDetailModel.getCharmLevel());
        mCharmPv.setProgress(mGroupDetailModel.getCharmExp(), mGroupDetailModel.getNextCharmExp());
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return bindUntilEvent();
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
