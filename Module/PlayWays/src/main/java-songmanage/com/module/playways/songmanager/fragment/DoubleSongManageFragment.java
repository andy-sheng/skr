package com.module.playways.songmanager.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.SlidingTabLayout;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.room.song.fragment.GrabSearchSongFragment;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.songmanager.SongManagerActivity;
import com.module.playways.songmanager.event.AddSongEvent;
import com.module.playways.songmanager.event.SongNumChangeEvent;
import com.module.playways.songmanager.model.RecommendTagModel;
import com.module.playways.songmanager.presenter.DoubleSongManagePresenter;
import com.module.playways.songmanager.view.ExistSongManageView;
import com.module.playways.songmanager.view.GrabSongWishView;
import com.module.playways.songmanager.view.ISongManageView;
import com.module.playways.songmanager.view.RecommendSongView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DoubleSongManageFragment extends BaseFragment implements ISongManageView {

    CommonTitleBar mTitlebar;
    ExTextView mSearchSongIv;
    SlidingTabLayout mTagTab;
    ViewPager mViewpager;

    DoubleSongManagePresenter mPresenter;
    PagerAdapter mPagerAdapter;
    DoubleRoomData mRoomData;
    List<RecommendTagModel> mTagModelList;

    @Override
    public int initView() {
        return R.layout.double_song_manage_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (mRoomData == null) {
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        mTitlebar = mRootView.findViewById(R.id.titlebar);
        mSearchSongIv = mRootView.findViewById(R.id.search_song_iv);
        mTagTab = mRootView.findViewById(R.id.tag_tab);
        mViewpager = mRootView.findViewById(R.id.viewpager);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() instanceof SongManagerActivity) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    finish();
                }
            }
        });

        mTitlebar.getRightTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), DoubleExistSongManageFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        mSearchSongIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), GrabSearchSongFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, SongManagerActivity.TYPE_FROM_DOUBLE)
                        .addDataBeforeAdd(1, false)
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

        mPresenter = new DoubleSongManagePresenter(this);
        addPresent(mPresenter);
        mPresenter.getRecommendTag();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void showRoomName(String roomName) {

    }

    @Override
    public void showRecommendSong(List<RecommendTagModel> recommendTagModelList) {

        if (recommendTagModelList == null || recommendTagModelList.size() == 0) {
            return;
        }

        mTagModelList = recommendTagModelList;
        mTagTab.setCustomTabView(R.layout.manage_song_tab, R.id.tab_tv);
        mTagTab.setSelectedIndicatorColors(U.getColor(R.color.white_trans_20));
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
                return instantiateItemDouble(container, position, mTagModelList);
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
                    } else if (view instanceof ExistSongManageView) {
                        ((ExistSongManageView) view).tryLoad();
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

        mViewpager.setCurrentItem(1);

    }

    public Object instantiateItemDouble(@NonNull ViewGroup container, int position, List<RecommendTagModel> recommendTagModelList) {
        MyLog.d(TAG, "instantiateItem" + " container=" + container + " position=" + position);
        View view;
        RecommendTagModel recommendTagModel = recommendTagModelList.get(position);
        RecommendSongView recommendSongView = new RecommendSongView(getActivity(), SongManagerActivity.TYPE_FROM_DOUBLE,
                false, mRoomData.getGameId(), recommendTagModel);
        recommendSongView.setTag(position);
        view = recommendSongView;

        if (container.indexOfChild(view) == -1) {
            container.addView(view);
        }
        return view;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (DoubleRoomData) data;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SongNumChangeEvent event) {

    }
}
