package com.module.rankingmode.song.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.common.base.BaseFragment;
import com.common.view.viewpager.NestViewPager;
import com.common.view.viewpager.SlidingTabLayout;
import com.module.rankingmode.R;
import com.module.rankingmode.song.view.SongListView;

import java.util.ArrayList;

public class SongSelectFragment extends BaseFragment {
    LinearLayout mMainActContainer;
    SlidingTabLayout mSelectSongTabLayout;
    NestViewPager mSonglistViewPager;

    @Override
    public int initView() {
        return R.layout.song_select_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (LinearLayout)mRootView.findViewById(R.id.main_act_container);
        mSelectSongTabLayout = (SlidingTabLayout)mRootView.findViewById(R.id.select_song_tab_layout);
        mSonglistViewPager = (NestViewPager)mRootView.findViewById(R.id.songlist_view_pager);

        MyAdapte adapter= new MyAdapte();
        mSonglistViewPager.setAdapter(adapter);
        mSelectSongTabLayout.setViewPager(mSonglistViewPager);
    }

    private void loadData(boolean test) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().finish();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    class MyAdapte extends PagerAdapter {
        String[] titles={getResources().getString(R.string.select_song_recommend),getResources().getString(R.string.select_song_history)};

        ArrayList<ViewGroup> layouts=new ArrayList<>(2);
        MyAdapte() {
            layouts.add(new SongListView(getContext()));
            layouts.add(new SongListView(getContext()));
        }

        @Override
        public int getCount() {
            return layouts.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view==o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup l=layouts.get(position);
            container.addView(l);
            return l;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(layouts.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
