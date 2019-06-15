package com.moudule.playways.beauty.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.common.log.MyLog;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.R;

import java.util.List;

/**
 * 美颜和滤镜、贴纸的view
 */
public class BeautyFiterPaterView extends FrameLayout {

    public final static String TAG = "BeautyFiterPaterView";

    RecyclerView mRecyclerView;
    SeekBar mProgressBar;
    BeautyFiterPaterAdapter mAdapter;

    int type;
    List<BeautyControlPanelView.BeautyViewModel> mDataList;

    public BeautyFiterPaterView(Context context, int type, List<BeautyControlPanelView.BeautyViewModel> list) {
        super(context);
        this.type = type;
        mDataList = list;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.beauty_fiter_pater_view_layout, this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mProgressBar = (SeekBar) findViewById(R.id.progress_bar);

        if (type == BeautyControlPanelView.TYPE_PATER) {
            mProgressBar.setVisibility(GONE);
        } else {
            mProgressBar.setVisibility(VISIBLE);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        if (mDataList != null && mDataList.size() <= 5) {
            layoutManager = new GridLayoutManager(getContext(), mDataList.size());
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new BeautyFiterPaterAdapter(new RecyclerOnItemClickListener<BeautyControlPanelView.BeautyViewModel>() {
            @Override
            public void onItemClicked(View view, int position, BeautyControlPanelView.BeautyViewModel model) {
                mAdapter.setSelectPosition(position);
                // TODO: 2019-06-15 得根据选中，设置相应进度条得位置
            }
        }, layoutManager);
        mAdapter.setDataList(mDataList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // 回调给前台页面
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    interface Listener {
        // 美颜改变
        void onChangeBeauty();

        // 滤镜改变
        void onChangeFiter();

        // 贴纸改变
        void onChangePater();
    }
}
