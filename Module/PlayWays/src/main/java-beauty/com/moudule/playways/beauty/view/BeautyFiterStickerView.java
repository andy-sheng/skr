package com.moudule.playways.beauty.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.engine.Params;
import com.module.playways.R;
import com.zq.mediaengine.kit.ZqEngineKit;

import java.util.List;

import static com.moudule.playways.beauty.view.BeautyControlPanelView.TYPE_BEAUTY;
import static com.moudule.playways.beauty.view.BeautyControlPanelView.TYPE_FITER;
import static com.moudule.playways.beauty.view.BeautyControlPanelView.TYPE_STICKER;

/**
 * 美颜和滤镜、贴纸的view
 */
public class BeautyFiterStickerView extends FrameLayout {

    public final String TAG = "BeautyFiterStickerView";

    RecyclerView mRecyclerView;
    SeekBar mProgressBar;
    BeautyFiterPaterAdapter mAdapter;

    Listener mListener;
    BeautyControlPanelView.BeautyViewModel mSelectModel;
    int mType;
    List<BeautyControlPanelView.BeautyViewModel> mDataList;

    public BeautyFiterStickerView(Context context, int type, List<BeautyControlPanelView.BeautyViewModel> list, BeautyFiterStickerView.Listener mListener) {
        super(context);
        this.mType = type;
        this.mDataList = list;
        this.mListener = mListener;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.beauty_fiter_pater_view_layout, this);

        mRecyclerView = findViewById(R.id.recycler_view);
        mProgressBar = findViewById(R.id.progress_bar);

        if (mType == TYPE_STICKER) {
            mProgressBar.setVisibility(GONE);
        } else {
            mProgressBar.setVisibility(VISIBLE);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        if (mDataList != null && mDataList.size() <= 5) {
            layoutManager = new GridLayoutManager(getContext(), mDataList.size());
            if (mType == TYPE_STICKER) {
                // 贴纸不需要给默认的
            } else {
                // 美颜和滤镜默认第一个
                mSelectModel = mDataList.get(0);
            }
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new BeautyFiterPaterAdapter(new RecyclerOnItemClickListener<BeautyControlPanelView.BeautyViewModel>() {
            @Override
            public void onItemClicked(View view, int position, BeautyControlPanelView.BeautyViewModel model) {
                mSelectModel = model;
                mAdapter.setSelectPosition(position);
                Params config = ZqEngineKit.getInstance().getParams();
                if (mType == TYPE_BEAUTY) {
                    float intensity = 0;
                    switch (mSelectModel.getType()) {
                        case dayan:
                            intensity = config.getIntensityBigEye();
                            break;
                        case shoulian:
                            intensity = config.getIntensityThinFace();
                            break;
                        case mopi:
                            intensity = config.getIntensityMopi();
                            break;
                        case meibai:
                            intensity = config.getIntensityMeibai();
                            break;
                        case ruihua:
                            intensity = config.getIntensityRuihua();
                            break;
                    }
                    mProgressBar.setProgress((int) (intensity * 100));
                } else if (mType == TYPE_FITER) {
                    float intensity = 0;
                    switch (mSelectModel.getType()) {
                        case none_filter:
                        case ruanyang:
                        case musi:
                        case yangqi:
                            intensity = config.getIntensityFilter();
                            break;
                    }
                    mProgressBar.setProgress((int) (intensity * 100));
                    if (mListener != null) {
                        mListener.onChangeBeauty(mSelectModel.getType(), mProgressBar.getProgress() / 100.0f);
                    }
                } else if (mType == TYPE_STICKER) {
                    if (mListener != null) {
                        mListener.onChangeBeauty(mSelectModel.getType(), 0);
                    }
                }
            }
        }, layoutManager, mType);
        mAdapter.setDataList(mDataList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // TODO: 2019-06-16 回调给前台页面
                    if (mListener != null && mSelectModel != null) {
                        if (mType == BeautyControlPanelView.TYPE_BEAUTY) {
                            mListener.onChangeBeauty(mSelectModel.getType(), progress / 100.0f);
                        } else if (mType == BeautyControlPanelView.TYPE_FITER) {
                            mListener.onChangeBeauty(mSelectModel.getType(), progress / 100.0f);
                        }
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if (mType == TYPE_BEAUTY) {
            Params config = ZqEngineKit.getInstance().getParams();
            mProgressBar.setProgress((int) (config.getIntensityBigEye() * 100));
        }
    }

    public void onPageSelected() {
        Params config = ZqEngineKit.getInstance().getParams();
        if (mType == TYPE_BEAUTY) {

        } else if (mType == TYPE_FITER) {
            mAdapter.setSelectPosition(config.getNoFilter() + 1);
        } else if (mType == TYPE_STICKER) {
            mAdapter.setSelectPosition(config.getNoSticker() + 1);
        }
    }

    public interface Listener {
        void onChangeBeauty(BeautyControlPanelView.Type type, float progress);
    }
}
