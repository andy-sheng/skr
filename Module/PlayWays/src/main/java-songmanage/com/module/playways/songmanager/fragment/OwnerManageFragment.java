package com.module.playways.songmanager.fragment;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

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
import com.common.view.viewpager.SlidingTabLayout;
import com.module.playways.R;
import com.module.playways.songmanager.OwnerManagerActivity;
import com.module.playways.songmanager.SongManageData;
import com.module.playways.songmanager.event.AddSongEvent;
import com.module.playways.songmanager.event.SongNumChangeEvent;
import com.module.playways.songmanager.model.RecommendTagModel;
import com.module.playways.songmanager.presenter.OwnerManagePresenter;
import com.module.playways.songmanager.view.GrabEditView;
import com.module.playways.songmanager.view.GrabSongManageView;
import com.module.playways.songmanager.view.GrabSongWishView;
import com.module.playways.songmanager.view.IOwnerManageView;
import com.module.playways.songmanager.view.RecommendSongView;
import com.module.playways.room.song.fragment.GrabSearchSongFragment;
import com.module.playways.room.song.model.SongModel;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class OwnerManageFragment extends BaseFragment implements IOwnerManageView {
    public final static String TAG = "OwnerManageFragment";

    ExRelativeLayout mRlContent;
    ExTextView mSearchSongIv;
    ViewPager mViewpager;
    SlidingTabLayout mTagTab;
    //    List<RecommendSongView> mRecommendSongViews = new ArrayList<>();
    GrabSongManageView mGrabSongManageView;
    GrabSongWishView mGrabSongWishView;
    DialogPlus mEditRoomDialog;

    CommonTitleBar mCommonTitleBar;
    OwnerManagePresenter mOwnerManagePresenter;
    PagerAdapter mPagerAdapter;

//    GrabRoomData mRoomData;

    SongManageData mSongManageData;
    List<RecommendTagModel> mTagModelList;

    @Override
    public int initView() {
        return R.layout.owner_manage_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (mSongManageData == null) {
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        mCommonTitleBar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mRlContent = (ExRelativeLayout) mRootView.findViewById(R.id.rl_content);
        mSearchSongIv = (ExTextView) mRootView.findViewById(R.id.search_song_iv);
        mTagTab = (SlidingTabLayout) mRootView.findViewById(R.id.tag_tab);
        mViewpager = (ViewPager) mRootView.findViewById(R.id.viewpager);

        mCommonTitleBar.getCenterTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                showEditRoomDialog();
            }
        });

        mCommonTitleBar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() instanceof OwnerManagerActivity) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    finish();
                }
            }
        });

        mOwnerManagePresenter = new OwnerManagePresenter(this, mSongManageData);
        addPresent(mOwnerManagePresenter);
        mOwnerManagePresenter.getRecommendTag();
        showRoomName(mSongManageData.getRoomName());

        mSearchSongIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), GrabSearchSongFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, mSongManageData)
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
        if (mSongManageData.isGrabRoom()) {
            if (mSongManageData.isOwner()) {
                Drawable drawable = U.getDrawable(R.drawable.ycdd_edit_roomname_icon);
                drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
                mCommonTitleBar.getCenterTextView().setCompoundDrawables(null, null, drawable, null);
                mCommonTitleBar.getCenterTextView().setCompoundDrawablePadding(U.getDisplayUtils().dip2px(7));
                mCommonTitleBar.getCenterTextView().setClickable(true);
            } else {
                mCommonTitleBar.getCenterTextView().setClickable(false);
            }
        } else {
            mCommonTitleBar.getCenterTextView().setClickable(false);
        }
    }

    /**
     * 得到所有类别
     *
     * @param recommendTagModelList
     */
    @Override
    public void showRecommendSong(List<RecommendTagModel> recommendTagModelList) {
        if (recommendTagModelList == null || recommendTagModelList.size() == 0) {
            return;
        }

//        for (RecommendSongView recommendSongView : mRecommendSongViews) {
//            recommendSongView.destroy();
//        }
//        mRecommendSongViews.clear();

        if (mSongManageData.isGrabRoom()) {
            if (mSongManageData.isOwner()) {
                RecommendTagModel recommendModel = new RecommendTagModel();
                recommendModel.setType(-1);
                recommendModel.setName("愿望歌单");
                recommendTagModelList.add(0, recommendModel);

                RecommendTagModel recommendTagModel = new RecommendTagModel();
                recommendTagModel.setType(-1);
                recommendTagModel.setName("已点0");
                recommendTagModelList.add(0, recommendTagModel);
            }
        } else {
            RecommendTagModel recommendTagModel = new RecommendTagModel();
            recommendTagModel.setType(-1);
            recommendTagModel.setName("已点0");
            recommendTagModelList.add(0, recommendTagModel);
        }

        mTagModelList = recommendTagModelList;
        mTagTab.setCustomTabView(R.layout.manage_song_tab, R.id.tab_tv);
        mTagTab.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20));
        mTagTab.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_NONE);
        mTagTab.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NORMAL);
        mTagTab.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24));
        mTagTab.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12));
        mPagerAdapter = new PagerAdapter() {

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                MyLog.d(TAG, "destroyItem" + " container=" + container + " position=" + position + " object=" + object);
                container.removeView((View) object);
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                MyLog.d(TAG, "instantiateItem" + " container=" + container + " position=" + position);
                if (mSongManageData.isGrabRoom()) {
                    return instantiateItemGrab(container, position, mTagModelList);
                } else {
                    return instantiateItemDouble(container, position, mTagModelList);
                }
            }

            @Override
            public int getCount() {
                return mTagModelList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == (object);
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return mTagModelList.get(position).getName();
            }
        };

        mTagTab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                View view = mViewpager.findViewWithTag(position);
                if (view != null) {
                    if (view instanceof RecommendSongView) {
                        ((RecommendSongView) view).tryLoad();
                    } else if (view instanceof GrabSongWishView) {
                        ((GrabSongWishView) view).tryLoad();
                    } else if (view instanceof GrabSongManageView) {
                        ((GrabSongManageView) view).tryLoad();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewpager.setAdapter(mPagerAdapter);
        mTagTab.setViewPager(mViewpager);
        mPagerAdapter.notifyDataSetChanged();

        if (mSongManageData.isDoubleRoom()) {
            mViewpager.setCurrentItem(1);
        }
    }

    public Object instantiateItemGrab(@NonNull ViewGroup container, int position, List<RecommendTagModel> recommendTagModelList) {
        MyLog.d(TAG, "instantiateItem" + " container=" + container + " position=" + position);
        View view = null;

        if (mSongManageData.isOwner()) {
            if (position == 0) {
                if (mGrabSongManageView == null) {
                    mGrabSongManageView = new GrabSongManageView(getContext(), mSongManageData);
                }
                view = mGrabSongManageView;
            } else if (position == 1) {
                if (mGrabSongWishView == null) {
                    mGrabSongWishView = new GrabSongWishView(getContext(), mSongManageData.getGrabRoomData());
                }
                mGrabSongWishView.setTag(position);
                view = mGrabSongWishView;
            } else {
                RecommendTagModel recommendTagModel = recommendTagModelList.get(position);
                RecommendSongView recommendSongView = new RecommendSongView(getActivity(), mSongManageData, recommendTagModel);
                recommendSongView.setTag(position);
                view = recommendSongView;
            }
        } else {
            RecommendTagModel recommendTagModel = recommendTagModelList.get(position);
            RecommendSongView recommendSongView = new RecommendSongView(getActivity(), mSongManageData, recommendTagModel);
            recommendSongView.setTag(position);
            view = recommendSongView;
        }

        if (container.indexOfChild(view) == -1) {
            container.addView(view);
        }

        return view;
    }

    public Object instantiateItemDouble(@NonNull ViewGroup container, int position, List<RecommendTagModel> recommendTagModelList) {
        MyLog.d(TAG, "instantiateItem" + " container=" + container + " position=" + position);
        View view = null;

        if (position == 0) {
            if (mGrabSongManageView == null) {
                mGrabSongManageView = new GrabSongManageView(getContext(), mSongManageData);
            }
            view = mGrabSongManageView;
        } else {
            RecommendTagModel recommendTagModel = recommendTagModelList.get(position);
            RecommendSongView recommendSongView = new RecommendSongView(getActivity(), mSongManageData, recommendTagModel);
            recommendSongView.setTag(position);
            view = recommendSongView;
        }

        if (container.indexOfChild(view) == -1) {
            container.addView(view);
        }

        return view;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mSongManageData = (SongManageData) data;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SongNumChangeEvent event) {
        mTagModelList.get(0).setName("已点" + event.getSongNum());
        mTagTab.notifyDataChange();
    }

    private void showEditRoomDialog() {
        GrabEditView grabEditView = new GrabEditView(getContext(), mSongManageData.getRoomName());
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
                    mOwnerManagePresenter.updateRoomName(mSongManageData.getGameId(), roomName);
                } else {
                    // TODO: 2019/4/18 房间名为空
                    U.getToastUtil().showShort("输入的房间名为空");
                }
            }
        });

        if (mEditRoomDialog != null) {
            mEditRoomDialog.dismiss(false);
        }
        mEditRoomDialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(grabEditView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
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
    public void destroy() {
        super.destroy();
//        for (RecommendSongView recommendSongView : mRecommendSongViews) {
//            recommendSongView.destroy();
//        }

        if (mGrabSongManageView != null) {
            mGrabSongManageView.destroy();
        }

        if (mGrabSongWishView != null) {
            mGrabSongWishView.destroy();
        }
    }

    @Override
    protected boolean onBackPressed() {
        if (mEditRoomDialog != null && mEditRoomDialog.isShowing()) {
            mEditRoomDialog.dismiss(false);
            mEditRoomDialog = null;
            return true;
        }
        if (getActivity() != null) {
            getActivity().finish();
        }
        return true;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}
