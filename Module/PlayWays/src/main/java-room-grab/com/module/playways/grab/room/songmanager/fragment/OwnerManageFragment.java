package com.module.playways.grab.room.songmanager.fragment;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.songmanager.model.RecommendTagModel;
import com.module.playways.grab.room.songmanager.event.AddSongEvent;
import com.module.playways.grab.room.songmanager.event.SongNumChangeEvent;
import com.module.playways.grab.room.songmanager.presenter.OwnerManagePresenter;
import com.module.playways.grab.room.songmanager.view.GrabEditView;
import com.module.playways.grab.room.songmanager.view.IOwnerManageView;
import com.module.playways.grab.room.songmanager.view.OwnerViewPagerTitleView;
import com.module.playways.room.song.fragment.GrabSearchSongFragment;
import com.module.playways.room.song.model.SongModel;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class OwnerManageFragment extends BaseFragment implements IOwnerManageView {
    public final static String TAG = "GrabSongManageFragment";

    ExRelativeLayout mRlContent;
    ExTextView mSearchSongIv;
    OwnerViewPagerTitleView mOwnerTitleView;
    ViewPager mViewpager;
    List<RecommendSongFragment> mRecommendSongFragmentList = new ArrayList<>();
    DialogPlus mEditRoomDialog;
    GrabRoomData mRoomData;
    CommonTitleBar mCommonTitleBar;
    OwnerManagePresenter mOwnerManagePresenter;

    @Override
    public int initView() {
        return R.layout.owner_manage_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mCommonTitleBar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRlContent = (ExRelativeLayout) mRootView.findViewById(R.id.rl_content);
        mSearchSongIv = (ExTextView) mRootView.findViewById(R.id.search_song_iv);
        mOwnerTitleView = (OwnerViewPagerTitleView) mRootView.findViewById(R.id.owner_title_view);
        mViewpager = (ViewPager) mRootView.findViewById(R.id.viewpager);
        mOwnerTitleView.setViewPager(mViewpager);
        mViewpager.setOffscreenPageLimit(3);
        mCommonTitleBar.getCenterTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                showEditRoomDialog();
            }
        });

        mCommonTitleBar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        mOwnerManagePresenter = new OwnerManagePresenter(this, mRoomData);
        addPresent(mOwnerManagePresenter);
        mOwnerManagePresenter.getRecommendTag();
        showRoomName(mRoomData.getRoomName());

        mSearchSongIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), GrabSearchSongFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                                if (requestCode == 0 && resultCode == 0 && obj != null) {
                                    SongModel model = (SongModel) obj;
                                    MyLog.d(TAG, "onFragmentResult" + " model=" + model);
                                    EventBus.getDefault().post(new AddSongEvent(model));
                                }
                            }
                        })
                        .build());
            }
        });
    }

    @Override
    public void showRoomName(String roomName) {
        mCommonTitleBar.getCenterTextView().setText(roomName);
        Drawable drawable = U.getDrawable(R.drawable.ycdd_edit_roomname_icon);
        drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
        mCommonTitleBar.getCenterTextView().setCompoundDrawables(null, null, drawable, null);
        mCommonTitleBar.getCenterTextView().setCompoundDrawablePadding(U.getDisplayUtils().dip2px(7));
    }

    @Override
    public void showRecommendSong(List<RecommendTagModel> recommendTagModelList) {
        if (recommendTagModelList == null || recommendTagModelList.size() == 0) {
            return;
        }

        for (RecommendTagModel recommendTagModel : recommendTagModelList) {
            RecommendSongFragment recommendSongFragment = new RecommendSongFragment();
            recommendSongFragment.setRecommendTagModel(recommendTagModel);
            mRecommendSongFragmentList.add(recommendSongFragment);
        }

        FragmentStatePagerAdapter fragmentPagerAdapter = new FragmentStatePagerAdapter(getActivity().getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(TAG, "getItem" + " position=" + position);
                if (position == 0) {
                    GrabSongManageFragment grabSongManageFragment = new GrabSongManageFragment();
                    grabSongManageFragment.setData(0, mRoomData);
                    return grabSongManageFragment;
                } else {
                    return mRecommendSongFragmentList.get(position - 1);
                }
            }

            @Override
            public int getCount() {
                return mRecommendSongFragmentList.size() + 1;
            }
        };

        RecommendTagModel recommendTagModel = new RecommendTagModel();
        recommendTagModel.setType(-1);
        recommendTagModel.setName("已点0");
        recommendTagModelList.add(0, recommendTagModel);

        mOwnerTitleView.setRecommendTagModelList(recommendTagModelList);
        mViewpager.setAdapter(fragmentPagerAdapter);
        fragmentPagerAdapter.notifyDataSetChanged();
        mViewpager.setPageMargin(U.getDisplayUtils().dip2px(12));
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (GrabRoomData) data;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SongNumChangeEvent event) {
        mOwnerTitleView.updateSelectedSongNum(event.getSongNum());
    }

    private void showEditRoomDialog() {
        GrabEditView grabEditView = new GrabEditView(getContext(), mRoomData.getRoomName());
        grabEditView.setListener(new GrabEditView.Listener() {
            @Override
            public void onClickCancel() {
                if (mEditRoomDialog != null) {
                    mEditRoomDialog.dismiss();
                }
            }

            @Override
            public void onClickSave(String roomName) {
                if (!TextUtils.isEmpty(roomName)) {
                    // TODO: 2019/4/18 修改房间名
                    mEditRoomDialog.dismiss(false);
                    mOwnerManagePresenter.updateRoomName(mRoomData.getGameId(), roomName);
                } else {
                    // TODO: 2019/4/18 房间名为空
                    U.getToastUtil().showShort("输入的房间名为空");
                }
            }
        });

        mEditRoomDialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(grabEditView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                    }
                })
                .create();
        U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity());
        mEditRoomDialog.show();
    }

    @Override
    protected boolean onBackPressed() {
        return super.onBackPressed();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}