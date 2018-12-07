package com.module.rankingmode.song.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.fragment.MatchingFragment;
import com.module.rankingmode.song.adapter.SongSelectAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;

public class SongListView extends FrameLayout {
    LinearLayout mMainActContainer;

    SmartRefreshLayout mSongRefreshLayout;

    RecyclerView mSongListView;

    SongSelectAdapter mSongSelectAdapter;

    public SongListView(Context context) {
        this(context, null);
    }

    public SongListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SongListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.song_list_view_layout, this);
        mMainActContainer = findViewById(R.id.main_act_container);
        mSongRefreshLayout = findViewById(R.id.song_refreshLayout);
        mSongListView = findViewById(R.id.song_list_view);
        mSongListView.setHasFixedSize(true);
        mSongListView.setLayoutManager(new LinearLayoutManager(getContext()));

        mSongSelectAdapter = new SongSelectAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder((BaseActivity)SongListView.this.getContext(), MatchingFragment.class)
                    .setAddToBackStack(true)
                    .setHasAnimation(false)
                    .setFragmentDataListener(new FragmentDataListener() {
                        @Override
                        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                        }
                    })
                    .build());
            }
        });

        mSongListView.setAdapter(mSongSelectAdapter);

        mSongSelectAdapter.setDataList(new ArrayList<>(10));
    }


}
