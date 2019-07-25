package com.respicker.preview.video;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.R;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.NestViewPager;
import com.respicker.ResPicker;
import com.respicker.fragment.ImageBaseFragment;
import com.respicker.model.VideoItem;
import com.respicker.model.ResItem;

import java.util.ArrayList;

public class VideoPreviewFragment extends ImageBaseFragment implements ResPicker.OnResSelectedListener {

    public static final String EXTRA_SELECTED_VIDEO_POSITION = "selected_video_position";

    RelativeLayout mContent;
    NestViewPager mViewpager;
    LinearLayout mBottomBar;
    View mMarginBottom;

    CommonTitleBar mTitleBar;
    TextView mBtnOk;
    TextView mTvDes;

    ResPicker mImagePicker;

    ArrayList<VideoItem> mVideoItems;      //可被选择的视频
    int mCurrentPosition = 0;              //跳转进ImagePreviewFragment时的序号，第几个图片
    ArrayList<VideoItem> mSelectedVideos;   //所有已经选中的视频

    VideoPageAdapter mVideoPageAdapter;

    @Override
    public int initView() {
        return R.layout.fragment_preivew_video_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mContent = (RelativeLayout) getRootView().findViewById(R.id.content);
        mViewpager = (NestViewPager) getRootView().findViewById(R.id.viewpager);
        mBottomBar = (LinearLayout) getRootView().findViewById(R.id.bottom_bar);
        mMarginBottom = (View) getRootView().findViewById(R.id.margin_bottom);

        mTitleBar = getRootView().findViewById(R.id.titlebar);
        mBtnOk = (TextView) mTitleBar.getRightCustomView();
        mTvDes = mTitleBar.getCenterTextView();

        mImagePicker = ResPicker.getInstance();

        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentPosition = bundle.getInt(EXTRA_SELECTED_VIDEO_POSITION, 0);

        }
        if (mVideoItems == null) {
            mVideoItems = mImagePicker.getCurrentResFolderVideoItems();
        }
        mSelectedVideos = mImagePicker.getSelectedVideoList();

        mBtnOk.setVisibility(View.VISIBLE);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImagePicker.getSelectedVideoList().size() == 0) {
                    // 表示选中了这张
                    VideoItem videoItem = mVideoItems.get(mCurrentPosition);
                    mImagePicker.addSelectedResItem(mCurrentPosition, videoItem);
                }
                deliverResult(ResPicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, null);
            }
        });
        mTitleBar.getLeftImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mVideoPageAdapter = new VideoPageAdapter(getActivity(), mVideoItems);

//        mVideoPageAdapter.setPhotoViewClickListener(new ImagePageAdapter.PhotoViewClickListener() {
//            @Override
//            public void OnPhotoTapListener(View view, float v, float v1) {
//                onImageSingleTap();
//            }
//        });
        mViewpager.setAdapter(mVideoPageAdapter);
        mViewpager.setCurrentItem(mCurrentPosition, false);

        //初始化当前页面的状态
        mTvDes.setText(getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mVideoItems.size()));

        mImagePicker.addOnResSelectedListener(this);
        mBottomBar.setVisibility(View.VISIBLE);
//        mCbOrigin.setText(getString(R.string.ip_origin));
//        mCbOrigin.setChecked(mImagePicker.isOrigin());
//        mCbOrigin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    long size = 0;
//                    for (ResItem item : mSelectedVideos) {
//                        size += item.getSize();
//                    }
//                    String fileSize = Formatter.formatFileSize(getContext(), size);
//                    mImagePicker.setOrigin(true);
//                    mCbOrigin.setText(getString(R.string.ip_origin_size, fileSize));
//                } else {
//                    mImagePicker.setOrigin(false);
//                    mCbOrigin.setText(getString(R.string.ip_origin));
//                }
//            }
//        });

        //当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除图片
//        mCbCheck.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                VideoItem imageItem = mVideoItems.get(mCurrentPosition);
//                int selectLimit = mImagePicker.getParams().getSelectLimit();
//                if (mCbCheck.isChecked() && mSelectedVideos.size() >= selectLimit) {
//                    U.getToastUtil().showShort(getString(R.string.ip_select_limit, selectLimit));
//                    mCbCheck.setChecked(false);
//                } else {
//                    if (mCbCheck.isChecked()) {
//                        mImagePicker.addSelectedResItem(mCurrentPosition, imageItem);
//                    } else {
//                        mImagePicker.removeSelectedResItem(mCurrentPosition, imageItem);
//                    }
//
//                }
//            }
//        });

        //滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的图片的位置描述文本
        mViewpager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                VideoItem item = mVideoItems.get(mCurrentPosition);
                boolean isSelected = mImagePicker.getSelectedVideoList().contains(item);
//                mCbCheck.setChecked(isSelected);
                mTvDes.setText(getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mVideoItems.size()));
            }
        });
        //初始化当前页面的状态
        onImageSelected(0, null, false);

        // 防止在低内存重建时，一些数据都没了，这里直接结束这个fragment
        if (mVideoItems == null || mVideoItems.isEmpty()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    U.getFragmentUtils().popFragment(VideoPreviewFragment.this);
                }
            });
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mImagePicker.removeOnResSelectedListener(this);
    }

    /**
     * 单击时，隐藏头和尾
     */
    public void onImageSingleTap() {
        if (mTitleBar.getVisibility() == View.VISIBLE) {
            mTitleBar.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.top_out));
            mBottomBar.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
            mTitleBar.setVisibility(View.GONE);
            mBottomBar.setVisibility(View.GONE);
            U.getStatusBarUtil().setColorBar(getActivity(), Color.TRANSPARENT);
        } else {
            mTitleBar.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.top_in));
            mBottomBar.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
            mTitleBar.setVisibility(View.VISIBLE);
            mBottomBar.setVisibility(View.VISIBLE);
            U.getStatusBarUtil().setColorBar(getActivity(), U.app().getResources().getColor(R.color.ip_color_primary_dark));
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        U.getFragmentUtils().popFragment(VideoPreviewFragment.this);
        return true;
    }

    /**
     * 可以从这里设置数据进去
     * @param type
     * @param data
     */
    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 1) {
            mVideoItems = (ArrayList<VideoItem>) data;
        }
    }

    @Override
    public void onResSelectedAdd(int position, ResItem item) {
        onImageSelected(position, item, true);
    }

    @Override
    public void onResSelectedRemove(int position, ResItem item) {
        onImageSelected(position, item, false);
    }

    /**
     * 图片添加成功后，修改当前图片的选中数量
     * 当调用 addSelectedImageItem 或 deleteSelectedImageItem 都会触发当前回调
     */
    public void onImageSelected(int position, ResItem item, boolean isAdd) {
        int selectedImageSize = mImagePicker.getSelectedVideoList().size();
        if (selectedImageSize > 0) {
            mBtnOk.setText(getString(R.string.ip_select_complete, selectedImageSize, mImagePicker.getParams().getSelectLimit()));
        } else {
            mBtnOk.setText(getString(R.string.ip_complete));
        }
    }

    /**
     * 交付选择结果,返回结果给调用方式
     */
    private void deliverResult(int requestCode, int resultCode, Bundle bundle) {
        //裁剪完成,直接返回数据，数据存在 mImagePicker 中
        if (getFragmentDataListener() != null) {
            // bundle.getParcelableArrayList(ImagePicker.EXTRA_RESULT_ITEMS);
            getFragmentDataListener().onFragmentResult(requestCode, resultCode, bundle,null);
        }
        U.getFragmentUtils().popFragment(this);
    }
}
