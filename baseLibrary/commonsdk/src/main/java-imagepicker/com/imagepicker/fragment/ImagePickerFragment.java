package com.imagepicker.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.base.R;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.PermissionUtil;
import com.common.utils.U;
import com.imagepicker.ImageDataSource;
import com.imagepicker.ImagePicker;
import com.imagepicker.adapter.ImageFolderAdapter;
import com.imagepicker.adapter.ImageRecyclerAdapter;
import com.imagepicker.model.ImageFolder;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerFragment extends BaseFragment implements ImagePicker.OnImageSelectedListener, ImageDataSource.OnImagesLoadedListener, ImageRecyclerAdapter.OnImageItemClickListener {

    public static final String EXTRAS_TAKE_PICKERS = "TAKE";
    public static final String EXTRAS_IMAGES = "IMAGES";

    private boolean mDirectPhoto = false; // 默认不是直接调取相机
    private ImagePicker mImagePicker;

    LinearLayout mContent;
    RecyclerView mRecyclerView;
    RelativeLayout mFooterBar;
    RelativeLayout mLlDir;
    TextView mTvDir;
    TextView mBtnPreview;
    ImageView mBtnBack;
    TextView mTvDes;
    Button mBtnOk;
    AppCompatImageView mBtnDel;

    ImageFolderAdapter mImageFolderAdapter;
    ImageRecyclerAdapter mImageRecyclerAdapter;

    ImageDataSource mImageDataSource;

    @Override
    public int initView() {
        return R.layout.fragment_imagepicker_grid_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mImagePicker = ImagePicker.getInstance();
        mImagePicker.reset();
        mImagePicker.addOnImageSelectedListener(this);
        mContent = (LinearLayout) mRootView.findViewById(R.id.content);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mFooterBar = (RelativeLayout) mRootView.findViewById(R.id.footer_bar);
        mLlDir = (RelativeLayout) mRootView.findViewById(R.id.ll_dir);
        mTvDir = (TextView) mRootView.findViewById(R.id.tv_dir);
        mBtnPreview = (TextView) mRootView.findViewById(R.id.btn_preview);
        mBtnBack = (ImageView) mRootView.findViewById(R.id.btn_back);
        mTvDes = (TextView) mRootView.findViewById(R.id.tv_des);
        mBtnOk = (Button) mRootView.findViewById(R.id.btn_ok);
        mBtnDel = (AppCompatImageView) mRootView.findViewById(R.id.btn_del);

        Bundle data = getArguments();
        if (data != null) {
            mDirectPhoto = data.getBoolean(EXTRAS_TAKE_PICKERS, false); // 默认不是直接打开相机
            if (mDirectPhoto) {
                if (!U.getPermissionUtils().checkCamera(getActivity())) {
                    U.getPermissionUtils().requestCamera(new PermissionUtil.RequestPermission() {
                        @Override
                        public void onRequestPermissionSuccess() {
                            mImagePicker.takePicture(getActivity(), ImagePicker.REQUEST_CODE_TAKE);
                        }

                        @Override
                        public void onRequestPermissionFailure(List<String> permissions) {

                        }

                        @Override
                        public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                        }
                    }, getActivity());
                } else {
                    mImagePicker.takePicture(getActivity(), ImagePicker.REQUEST_CODE_TAKE);
                }
            }
            // 进选择器前已经选择的 image
            ArrayList<ImageItem> images = data.getParcelableArrayList(EXTRAS_IMAGES);
            mImagePicker.setSelectedImages(images);
        }
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mBtnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mLlDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        if (mImagePicker.isMultiMode()) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPreview.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPreview.setVisibility(View.GONE);
        }

        mImageFolderAdapter = new ImageFolderAdapter(getActivity());
        mImageRecyclerAdapter = new ImageRecyclerAdapter(getActivity());

        onImageSelectedChange(0, null, false);

        mImageDataSource = new ImageDataSource(this, this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (!U.getPermissionUtils().checkExternalStorage(getActivity())) {
                U.getPermissionUtils().requestExternalStorage(new PermissionUtil.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        mImageDataSource.load(null);
                    }

                    @Override
                    public void onRequestPermissionFailure(List<String> permissions) {

                    }

                    @Override
                    public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                    }
                }, getActivity());
            } else {
                mImageDataSource.load(null);
            }
        } else {
            mImageDataSource.load(null);
        }
    }

    @Override
    public void onImageSelectedAdd(int position, ImageItem item) {
        onImageSelectedChange(position, item, true);
    }

    @Override
    public void onImageSelectedRemove(int position, ImageItem item) {
        onImageSelectedChange(position, item, false);
    }

    public void onImageSelectedChange(int position, ImageItem item, boolean isAdd) {
        int selectedImageSize = mImagePicker.getSelectedImages().size();
        if (selectedImageSize > 0) {
            mBtnOk.setText(getString(R.string.ip_select_complete, selectedImageSize, mImagePicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
            mBtnPreview.setEnabled(true);
            mBtnPreview.setText(getResources().getString(R.string.ip_preview_count, selectedImageSize));
            mBtnPreview.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            mBtnOk.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            mBtnOk.setText(getString(R.string.ip_complete));
            mBtnOk.setEnabled(false);
            mBtnPreview.setEnabled(false);
            mBtnPreview.setText(getResources().getString(R.string.ip_preview));
            mBtnPreview.setTextColor(ContextCompat.getColor(getContext(), R.color.ip_text_secondary_inverted));
            mBtnOk.setTextColor(ContextCompat.getColor(getContext(), R.color.ip_text_secondary_inverted));
        }
        for (int i = mImagePicker.isShowCamera() ? 1 : 0; i < mImageRecyclerAdapter.getItemCount(); i++) {
            String path = mImageRecyclerAdapter.getItem(i).getPath();
            if (path != null && path.equals(item.getPath())) {
                mImageRecyclerAdapter.notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean onActivityResultReal(int requestCode, int resultCode, Intent data) {
        MyLog.d(TAG, "onActivityResult" + " requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        if (resultCode == FragmentActivity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_TAKE) {
            U.getImageUtils().notifyGalleryChangeByBroadcast(mImagePicker.getTakeImageFile());
            String path = mImagePicker.getTakeImageFile().getAbsolutePath();

            ImageItem imageItem = new ImageItem();
            imageItem.setPath(path);

            mImagePicker.clearSelectedImages();
            mImagePicker.addSelectedImageItem(0, imageItem);

            if (mImagePicker.isCrop()) {
                gotoCrop();
            } else {
                Intent intent = new Intent();
                intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, mImagePicker.getSelectedImages());
//                setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
//                finish();
            }
        }
        return false;
    }


    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        mImagePicker.setImageFolders(imageFolders);
        if (imageFolders.size() == 0) {
            mImageRecyclerAdapter.refreshData(null);
        } else {
            mImageRecyclerAdapter.refreshData(imageFolders.get(0).getImages());
        }
        mImageRecyclerAdapter.setOnImageItemClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, U.getDisplayUtils().dip2px(2), false));
        mRecyclerView.setAdapter(mImageRecyclerAdapter);
        mImageFolderAdapter.refreshData(imageFolders);
    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        //根据是否有相机按钮确定位置
        position = mImagePicker.isShowCamera() ? position - 1 : position;
        if (mImagePicker.isMultiMode()) {
            // 多选就去大图浏览界面


//            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
//            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
//
//            /**
//             * 2017-03-20
//             *
//             * 依然采用弱引用进行解决，采用单例加锁方式处理
//             */
//
//            // 据说这样会导致大量图片的时候崩溃
////            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getCurrentImageFolderItems());
//
//            // 但采用弱引用会导致预览弱引用直接返回空指针
//            DataHolder.getInstance().save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, imagePicker.getCurrentImageFolderItems());
//            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
//            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);  //如果是多选，点击图片进入预览界面
        } else {
            // 单选，要么跳裁剪，要么跳返回结果
            mImagePicker.clearSelectedImages();
            mImagePicker.addSelectedImageItem(position, mImagePicker.getCurrentImageFolderItems().get(position));
            if (mImagePicker.isCrop()) {
                gotoCrop();
            } else {
                if (mFragmentDataListener != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(ImagePicker.EXTRA_RESULT_ITEMS, mImagePicker.getSelectedImages());
                    mFragmentDataListener.onFragmentResult(ImagePicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, bundle);
                }
                U.getFragmentUtils().popFragment(ImagePickerFragment.this);
            }
        }
    }

    void gotoCrop() {
        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(getActivity(), CropImageFragment.class)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
                        //裁剪完成,直接返回数据，数据存在 mImagePicker 中
                        if (mFragmentDataListener != null) {
                            // bundle.getParcelableArrayList(ImagePicker.EXTRA_RESULT_ITEMS);
                            mFragmentDataListener.onFragmentResult(requestCode, requestCode, bundle);
                        }
                        U.getFragmentUtils().popFragment(ImagePickerFragment.this);
                    }
                })
                .build());
    }
}
