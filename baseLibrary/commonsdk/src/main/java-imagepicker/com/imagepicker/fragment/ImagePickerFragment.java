package com.imagepicker.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.FragmentDataListener;
import com.common.base.R;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.PermissionUtil;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.imagepicker.loader.ImageDataSource;
import com.imagepicker.ImagePicker;
import com.imagepicker.adapter.ImageFolderAdapter;
import com.imagepicker.adapter.ImageRecyclerAdapter;
import com.imagepicker.model.ImageFolder;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.FolderPopUpWindow;
import com.imagepicker.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerFragment extends ImageBaseFragment implements ImagePicker.OnImageSelectedListener, ImageDataSource.OnImagesLoadedListener, ImageRecyclerAdapter.OnImageItemClickListener {

    public static final String EXTRAS_TAKE_PICKERS = "TAKE";
    public static final String EXTRAS_IMAGES = "IMAGES";

    private boolean mDirectPhoto = false; // 默认不是直接调取相机
    private ImagePicker mImagePicker;

    LinearLayout mContent;
    RecyclerView mRecyclerView;
    RelativeLayout mFooterBar; //底部栏
    RelativeLayout mLlDir; //目录按钮
    TextView mTvDir; //
    TextView mBtnPreview; // 预览按钮
    CommonTitleBar mTitlebar;
    TextView mBtnOk;

    FolderPopUpWindow mFolderPopupWindow;  //ImageSet的PopupWindow

    ImageFolderAdapter mImageFolderAdapter; //图片文件夹的适配器
    ImageRecyclerAdapter mImageRecyclerAdapter; //图片适配器

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
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mBtnOk = (TextView) mTitlebar.getRightCustomView();

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
            if (images != null) {
                mImagePicker.getSelectedImages().addAll(images);
            }
        }
        mTitlebar.getLeftTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                U.getFragmentUtils().popFragment(ImagePickerFragment.this);
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回结果
                deliverResult(ImagePicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, null);
            }
        });
        mBtnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳到预览
                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(getActivity(), ImagePreviewFragment.class)
                        .setDataBeforeAdd(1, new ArrayList<>(mImagePicker.getSelectedImages()))
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
                                deliverResult(requestCode, resultCode, bundle);
                            }
                        })
                        .build());
            }
        });
        mLlDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击文件夹按钮
                createPopupFolderList();
                mImageFolderAdapter.refreshData(mImagePicker.getImageFolders());  //刷新数据
                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                    //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                    int index = mImageFolderAdapter.getSelectIndex();
                    index = index == 0 ? index : index - 1;
                    mFolderPopupWindow.setSelection(index);
                }
            }
        });
        if (mImagePicker.getParams().isMultiMode()) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPreview.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPreview.setVisibility(View.GONE);
        }

        mImageFolderAdapter = new ImageFolderAdapter(getActivity());
        mImageRecyclerAdapter = new ImageRecyclerAdapter(getActivity());

        mImageRecyclerAdapter.setOnImageItemClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, U.getDisplayUtils().dip2px(2), false));
        mRecyclerView.setAdapter(mImageRecyclerAdapter);

        onImageSelectedChange(0, null, false);

        mImageDataSource = new ImageDataSource(this, this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (!U.getPermissionUtils().checkExternalStorage(getActivity())) {
                U.getPermissionUtils().requestExternalStorage(new PermissionUtil.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        mImageDataSource.load();
                    }

                    @Override
                    public void onRequestPermissionFailure(List<String> permissions) {

                    }

                    @Override
                    public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                    }
                }, getActivity());
            } else {
                mImageDataSource.load();
            }
        } else {
            mImageDataSource.load();
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(getContext(), mImageFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                mImagePicker.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    mImageRecyclerAdapter.refreshData(imageFolder.getImages());
                    mTvDir.setText(imageFolder.getName());
                }
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
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
            mBtnOk.setText(getString(R.string.ip_select_complete, selectedImageSize, mImagePicker.getParams().getSelectLimit()));
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
        for (int i = mImagePicker.getParams().isShowCamera() ? 1 : 0; i < mImageRecyclerAdapter.getItemCount(); i++) {
            String path = mImageRecyclerAdapter.getItem(i).getPath();
            if (path != null && path.equals(item.getPath())) {
                mImageRecyclerAdapter.notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mImagePicker.removeOnImageSelectedListener(this);
        if (mImageDataSource != null) {
            mImageDataSource.destroy();
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
            // 拍照结果返回
            U.getImageUtils().notifyGalleryChangeByBroadcast(mImagePicker.getTakeImageFile());
            String path = mImagePicker.getTakeImageFile().getAbsolutePath();

            ImageItem imageItem = new ImageItem();
            imageItem.setPath(path);

            mImagePicker.clearSelectedImages();
            mImagePicker.addSelectedImageItem(0, imageItem);

            if (mImagePicker.getParams().isCrop()) {
                gotoCrop();
            } else {
                deliverResult(requestCode, resultCode, data.getExtras());
            }
        }
        return false;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        mImagePicker.setImageFolders(imageFolders);
        if (imageFolders.size() == 0) {
            mImageRecyclerAdapter.refreshData(null);
        } else {
            mImageRecyclerAdapter.refreshData(imageFolders.get(0).getImages());
        }
        mImageFolderAdapter.refreshData(imageFolders);
    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        //根据是否有相机按钮确定位置
        position = mImagePicker.getParams().isShowCamera() ? position - 1 : position;
        if (mImagePicker.getParams().isMultiMode()) {
            // 多选就去大图浏览界面
            Bundle bundle = new Bundle();
            bundle.putInt(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
            U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(getActivity(), ImagePreviewFragment.class)
                    .setFragmentDataListener(new FragmentDataListener() {
                        @Override
                        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
                            deliverResult(requestCode, resultCode, bundle);
                        }
                    })
                    .setBundle(bundle)
                    .build());
        } else {
            // 单选，要么跳裁剪，要么跳返回结果
            mImagePicker.clearSelectedImages();
            mImagePicker.addSelectedImageItem(position, mImagePicker.getCurrentImageFolderItems().get(position));
            if (mImagePicker.getParams().isCrop()) {
                gotoCrop();
            } else {
                deliverResult(ImagePicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, null);
            }
        }
    }

    /**
     * 跳转到裁剪页面
     */
    private void gotoCrop() {
        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(getActivity(), ImageCropFragment.class)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
                        deliverResult(requestCode, resultCode, bundle);
                    }
                })
                .build());
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
        U.getFragmentUtils().popFragment(ImagePickerFragment.this);
    }
}
