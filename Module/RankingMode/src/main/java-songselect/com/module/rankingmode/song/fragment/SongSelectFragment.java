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
import com.module.rankingmode.song.model.TagModel;
import com.module.rankingmode.song.presenter.SongSelectPresenter;
import com.module.rankingmode.song.view.ISongTagView;
import com.module.rankingmode.song.view.SongListView;

import java.util.ArrayList;
import java.util.List;

public class SongSelectFragment extends BaseFragment implements ISongTagView {
    LinearLayout mMainActContainer;
    SlidingTabLayout mSelectSongTabLayout;
    NestViewPager mSonglistViewPager;

    SongSelectPresenter songSelectPresenter;
    TagPagerAdapter adapter;

    @Override
    public int initView() {
        return R.layout.song_select_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (LinearLayout) mRootView.findViewById(R.id.main_act_container);
        mSelectSongTabLayout = (SlidingTabLayout) mRootView.findViewById(R.id.select_song_tab_layout);
        mSonglistViewPager = (NestViewPager) mRootView.findViewById(R.id.songlist_view_pager);

        songSelectPresenter = new SongSelectPresenter(this);
        addPresent(songSelectPresenter);

        // todo 仅做test
        songSelectPresenter.getSongsListTags(1, 0, 100);
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

    @Override
    public void loadSongsTags(List<TagModel> list) {
        if (list == null) {
            return;
        }

        if (adapter == null) {
            adapter = new TagPagerAdapter();
        }
        adapter.setData(list);
        mSonglistViewPager.setAdapter(adapter);
        mSelectSongTabLayout.setViewPager(mSonglistViewPager);
    }

    @Override
    public void loadSongsTagsFail() {
        // 加载失败
    }

    class TagPagerAdapter extends PagerAdapter {
        List<TagModel> listData = new ArrayList<>();

        ArrayList<ViewGroup> layouts = new ArrayList<>();

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ViewGroup l = layouts.get(position);
            container.addView(l);
            return l;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(layouts.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return listData.get(position).getTagName();
        }

        public void setData(List<TagModel> listData) {
            this.listData = listData;
            for (TagModel tagModel : listData) {
                SongListView songListView = new SongListView(getContext());
                songListView.setTagModel(tagModel);
                layouts.add(songListView);
            }
            notifyDataSetChanged();
        }
    }
}
