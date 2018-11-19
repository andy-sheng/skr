package com.imagepicker.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.text.format.Formatter;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.base.BaseFragment;
import com.common.base.R;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.common.view.viewpager.NestViewPager;
import com.imagepicker.ImagePicker;
import com.imagepicker.adapter.ImagePageAdapter;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.SuperCheckBox;

import java.util.ArrayList;

public class ImagePreviewFragment extends ImageBaseFragment implements ImagePicker.OnImageSelectedListener {

    RelativeLayout mContent;
    NestViewPager mViewpager;
    LinearLayout mBottomBar;
    SuperCheckBox mCbOrigin;
    SuperCheckBox mCbCheck;
    View mMarginBottom;

    CommonTitleBar mTitleBar;
    TextView mBtnOk;
    TextView mTvDes;

    ImagePicker mImagePicker;

    ArrayList<ImageItem> mImageItems;      //跳转进ImagePreviewFragment的图片文件夹
    int mCurrentPosition = 0;              //跳转进ImagePreviewFragment时的序号，第几个图片
    ArrayList<ImageItem> mSelectedImages;   //所有已经选中的图片

    ImagePageAdapter mImagePageAdapter;

    @Override
    public int initView() {
        return R.layout.fragment_preivew_image_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mContent = (RelativeLayout) mRootView.findViewById(R.id.content);
        mViewpager = (NestViewPager) mRootView.findViewById(R.id.viewpager);
        mBottomBar = (LinearLayout) mRootView.findViewById(R.id.bottom_bar);
        mCbOrigin = (SuperCheckBox) mRootView.findViewById(R.id.cb_origin);
        mCbCheck = (SuperCheckBox) mRootView.findViewById(R.id.cb_check);
        mMarginBottom = (View) mRootView.findViewById(R.id.margin_bottom);

        mTitleBar = mRootView.findViewById(R.id.titlebar);
        mBtnOk = (TextView) mTitleBar.getRightCustomView();
        mTvDes = mTitleBar.getCenterTextView();

        mImagePicker = ImagePicker.getInstance();

        Bundle bundle = getArguments();
        if (bundle != null) {
            mCurrentPosition = bundle.getInt(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);

        }
        if (mImageItems == null) {
            mImageItems = mImagePicker.getCurrentImageFolderItems();
        }
        mSelectedImages = mImagePicker.getSelectedImages();

        mBtnOk.setVisibility(View.VISIBLE);
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mImagePicker.getSelectedImages().size() == 0) {
                    // 表示选中了这张
                    mCbCheck.setChecked(true);
                    ImageItem imageItem = mImageItems.get(mCurrentPosition);
                    mImagePicker.addSelectedImageItem(mCurrentPosition, imageItem);
                }
                deliverResult(ImagePicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, null);
            }
        });
        mTitleBar.getLeftImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mImagePageAdapter = new ImagePageAdapter(getActivity(), mImageItems);

        mImagePageAdapter.setPhotoViewClickListener(new ImagePageAdapter.PhotoViewClickListener() {
            @Override
            public void OnPhotoTapListener(View view, float v, float v1) {
                onImageSingleTap();
            }
        });
        mViewpager.setAdapter(mImagePageAdapter);
        mViewpager.setCurrentItem(mCurrentPosition, false);

        //初始化当前页面的状态
        mTvDes.setText(getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems.size()));

        mImagePicker.addOnImageSelectedListener(this);
        mBottomBar.setVisibility(View.VISIBLE);
        mCbOrigin.setText(getString(R.string.ip_origin));
        mCbOrigin.setChecked(mImagePicker.isOrigin());
        mCbOrigin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    long size = 0;
                    for (ImageItem item : mSelectedImages)
                        size += item.getSize();
                    String fileSize = Formatter.formatFileSize(getContext(), size);
                    mImagePicker.setOrigin(true);
                    mCbOrigin.setText(getString(R.string.ip_origin_size, fileSize));
                } else {
                    mImagePicker.setOrigin(false);
                    mCbOrigin.setText(getString(R.string.ip_origin));
                }
            }
        });

        //当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除图片
        mCbCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageItem imageItem = mImageItems.get(mCurrentPosition);
                int selectLimit = mImagePicker.getParams().getSelectLimit();
                if (mCbCheck.isChecked() && mSelectedImages.size() >= selectLimit) {
                    U.getToastUtil().showShort(getString(R.string.ip_select_limit, selectLimit));
                    mCbCheck.setChecked(false);
                } else {
                    if (mCbCheck.isChecked()) {
                        mImagePicker.addSelectedImageItem(mCurrentPosition, imageItem);
                    } else {
                        mImagePicker.removeSelectedImageItem(mCurrentPosition, imageItem);
                    }

                }
            }
        });

        //滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的图片的位置描述文本
        mViewpager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                ImageItem item = mImageItems.get(mCurrentPosition);
                boolean isSelected = mImagePicker.getSelectedImages().contains(item);
                mCbCheck.setChecked(isSelected);
                mTvDes.setText(getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems.size()));
            }
        });
        //初始化当前页面的状态
        onImageSelected(0, null, false);

        // 防止在低内存重建时，一些数据都没了，这里直接结束这个fragment
        if (mImageItems == null || mImageItems.isEmpty()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    U.getFragmentUtils().popFragment(ImagePreviewFragment.this);
                }
            });
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mImagePicker.removeOnImageSelectedListener(this);
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
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(ImagePreviewFragment.this);
        return true;
    }

    /**
     * 可以从这里设置数据进去
     * @param type
     * @param data
     */
    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 1) {
            mImageItems = (ArrayList<ImageItem>) data;
        }
    }

    @Override
    public void onImageSelectedAdd(int position, ImageItem item) {
        onImageSelected(position, item, true);
    }

    @Override
    public void onImageSelectedRemove(int position, ImageItem item) {
        onImageSelected(position, item, false);
    }

    /**
     * 图片添加成功后，修改当前图片的选中数量
     * 当调用 addSelectedImageItem 或 deleteSelectedImageItem 都会触发当前回调
     */
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        int selectedImageSize = mImagePicker.getSelectedImages().size();
        if (selectedImageSize > 0) {
            mBtnOk.setText(getString(R.string.ip_select_complete, selectedImageSize, mImagePicker.getParams().getSelectLimit()));
        } else {
            mBtnOk.setText(getString(R.string.ip_complete));
        }

        if (mCbOrigin.isChecked()) {
            long size = 0;
            for (ImageItem imageItem : mSelectedImages) {
                size += imageItem.getSize();
            }
            String fileSize = Formatter.formatFileSize(getContext(), size);
            mCbOrigin.setText(getString(R.string.ip_origin_size, fileSize));
        }
    }

    /**
     * 交付选择结果,返回结果给调用方式
     */
    private void deliverResult(int requestCode, int resultCode, Bundle bundle) {
        //裁剪完成,直接返回数据，数据存在 mImagePicker 中
        if (mFragmentDataListener != null) {
            // bundle.getParcelableArrayList(ImagePicker.EXTRA_RESULT_ITEMS);
            mFragmentDataListener.onFragmentResult(requestCode, resultCode, bundle);
        }
        U.getFragmentUtils().popFragment(this);
    }
}
