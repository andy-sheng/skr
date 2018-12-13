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
import com.module.rankingmode.song.view.SongListView;

import java.util.ArrayList;
import java.util.List;

public class SongSelectFragment extends BaseFragment  {
    LinearLayout mMainActContainer;
    SlidingTabLayout mSelectSongTabLayout;
    NestViewPager mSonglistViewPager;

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

        if (adapter == null) {
            adapter = new TagPagerAdapter();
        }

        // TODO: 2018/12/13 服务器暂不提供选歌TAG的接口，先写死 
        List<TagModel> tagModelList = new ArrayList<>();
        TagModel tagModel = new TagModel();
        tagModel.setTagID(1);
        tagModel.setTagName("推荐");
        tagModelList.add(tagModel);
        TagModel tagModel1 = new TagModel();
        tagModel1.setTagID(2);
        tagModel1.setTagName("已点");
        tagModelList.add(tagModel1);

        adapter.setData(tagModelList);
        mSonglistViewPager.setAdapter(adapter);
        mSelectSongTabLayout.setViewPager(mSonglistViewPager);

//        addPresent(songSelectPresenter);
//        mLoadService = LoadSirManager.getDefault().register(mMainActContainer, new Callback.OnReloadListener() {
//            @Override
//            public void onReload(View v) {
//                songSelectPresenter.getSongsListTags(1, 0, 100);
//            }
//        });
//        mLoadService.showCallback(LoadingCallback.class);
//
//        songSelectPresenter.getSongsListTags(1, 0, 100);
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

//    @Override
//    protected View loadSirReplaceRootView() {
//        return mLoadService.getLoadLayout();
//    }
//    @Override
//    public void loadSongsTags(List<TagModel> list) {
//        if (list == null) {
//            return;
//        }
//
//        if (adapter == null) {
//            adapter = new TagPagerAdapter();
//        }
//        adapter.setData(list);
//        mSonglistViewPager.setAdapter(adapter);
//        mSelectSongTabLayout.setViewPager(mSonglistViewPager);
//        mLoadService.showSuccess();
//    }
//    @Override
//    public void loadSongsTagsFail() {
//        // 加载失败
//        mLoadService.showCallback(ErrorCallback.class);
//    }

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
