package com.respicker.fragment;

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
import com.common.permission.PermissionUtils;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.respicker.loader.ResDataSource;
import com.respicker.ResPicker;
import com.respicker.adapter.ImageFolderAdapter;
import com.respicker.adapter.ResRecyclerAdapter;
import com.respicker.model.ResFolder;
import com.respicker.model.ImageItem;
import com.respicker.model.ResItem;
import com.respicker.model.VideoItem;
import com.respicker.preview.image.ImagePreviewFragment;
import com.respicker.preview.video.VideoPreviewFragment;
import com.respicker.view.FolderPopUpWindow;
import com.respicker.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ResPickerFragment extends ImageBaseFragment implements ResPicker.OnResSelectedListener, ResDataSource.OnImagesLoadedListener, ResRecyclerAdapter.OnResItemClickListener {

    public static final String EXTRAS_TAKE_PICKERS = "TAKE";
    public static final String EXTRAS_IMAGES = "IMAGES";

    private boolean mDirectPhoto = false; // 默认不是直接调取相机
    private ResPicker mImagePicker;

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
    ResRecyclerAdapter mImageRecyclerAdapter; //图片适配器

    ResDataSource mImageDataSource;

    boolean mSelfActivity = false; // 表示这个Fragment 是否使用自己的Activity ResPickerActivity 包裹。会影响退出方法的调用

    @Override
    public int initView() {
        return R.layout.fragment_imagepicker_grid_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mImagePicker = ResPicker.getInstance();
        mImagePicker.reset();
        mImagePicker.addOnResSelectedListener(this);
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
                    U.getPermissionUtils().requestCamera(new PermissionUtils.RequestPermission() {
                        @Override
                        public void onRequestPermissionSuccess() {
                            mImagePicker.takePicture(getActivity(), ResPicker.REQUEST_CODE_TAKE);
                        }

                        @Override
                        public void onRequestPermissionFailure(List<String> permissions) {

                        }

                        @Override
                        public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                        }
                    }, getActivity());
                } else {
                    mImagePicker.takePicture(getActivity(), ResPicker.REQUEST_CODE_TAKE);
                }
            }
            // 进选择器前已经选择的 image
            ArrayList<ImageItem> images = data.getParcelableArrayList(EXTRAS_IMAGES);
            if (images != null) {
                mImagePicker.getSelectedResList().addAll(images);
            }
        }
        mTitlebar.getLeftTextView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                if (mSelfActivity) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                } else {
                    U.getFragmentUtils().popFragment(ResPickerFragment.this);
                }
            }
        });
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回结果
                deliverResult(ResPicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, null);
            }
        });
        mBtnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳到预览
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), ImagePreviewFragment.class)
                        .addDataBeforeAdd(1, new ArrayList<>(mImagePicker.getSelectedResList()))
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object object) {
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
                mImageFolderAdapter.refreshData(mImagePicker.getResFolders());  //刷新数据
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
        mImageRecyclerAdapter = new ResRecyclerAdapter(getActivity());

        mImageRecyclerAdapter.setOnImageItemClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, U.getDisplayUtils().dip2px(2), false));
        mRecyclerView.setAdapter(mImageRecyclerAdapter);

        onImageSelectedChange(0, null, false);

        mImageDataSource = new ResDataSource(this, this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (!U.getPermissionUtils().checkExternalStorage(getActivity())) {
                U.getPermissionUtils().requestExternalStorage(new PermissionUtils.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        mImageDataSource.loadRes();
                    }

                    @Override
                    public void onRequestPermissionFailure(List<String> permissions) {

                    }

                    @Override
                    public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {

                    }
                }, getActivity());
            } else {
                mImageDataSource.loadRes();
            }
        } else {
            mImageDataSource.loadRes();
        }

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
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
                mImagePicker.setCurrentResFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ResFolder imageFolder = (ResFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    mImageRecyclerAdapter.refreshData(imageFolder.getResItems());
                    mTvDir.setText(imageFolder.getName());
                }
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onResSelectedAdd(int position, ResItem item) {
        onImageSelectedChange(position, item, true);
    }

    @Override
    public void onResSelectedRemove(int position, ResItem item) {
        onImageSelectedChange(position, item, false);
    }

    public void onImageSelectedChange(int position, ResItem item, boolean isAdd) {
        int selectedImageSize = mImagePicker.getSelectedResList().size();
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
        U.getSoundUtils().release(TAG);
        mImagePicker.removeOnResSelectedListener(this);
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
        if (resultCode == FragmentActivity.RESULT_OK && requestCode == ResPicker.REQUEST_CODE_TAKE) {
            if (mImagePicker != null && mImagePicker.getTakeImageFile() != null) {
                // 拍照结果返回
                U.getImageUtils().notifyGalleryChangeByBroadcast(mImagePicker.getTakeImageFile());
                String path = mImagePicker.getTakeImageFile().getAbsolutePath();

                ImageItem imageItem = new ImageItem();
                imageItem.setPath(path);

                mImagePicker.clearSelectedRes();
                mImagePicker.addSelectedResItem(0, imageItem);

                if (mImagePicker.getParams().isCrop()) {
                    gotoCrop();
                } else {
                    if (data != null) {
                        deliverResult(requestCode, resultCode, data.getExtras());
                    } else {
                        deliverResult(requestCode, resultCode, null);
                    }
                }
            } else {
                MyLog.w(TAG, "onActivityResultReal" + "no takeImageFile" + " requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
            }
        }
        return false;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onImagesLoaded(List<ResFolder> imageFolders) {
        mImagePicker.setResFolders(imageFolders);
        if (imageFolders.size() == 0) {
            mImageRecyclerAdapter.refreshData(null);
        } else {
            mImageRecyclerAdapter.refreshData(imageFolders.get(0).getResItems());
        }
        mImageFolderAdapter.refreshData(imageFolders);
    }

    @Override
    public void onResItemClick(View view, ResItem resItem, int position) {
        //根据是否有相机按钮确定位置
        if (mImagePicker.getCurrentResFolderItems().size() <= (mImagePicker.getParams().isShowCamera() ? position - 1 : position)) {
            if (getActivity() != null) {
                getActivity().finish();
            }

            return;
        }

        if (resItem instanceof ImageItem) {
            position = mImagePicker.getParams().isShowCamera() ? position - 1 : position;
            if (mImagePicker.getParams().isMultiMode()) {
                // 多选就去大图浏览界面
                Bundle bundle = new Bundle();
                int p = mImagePicker.getCurrentResFolderImageItems().indexOf(resItem);
                bundle.putInt(ImagePreviewFragment.EXTRA_SELECTED_IMAGE_POSITION, p);
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), ImagePreviewFragment.class)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                                deliverResult(requestCode, resultCode, bundle);
                            }
                        })
                        .setBundle(bundle)
                        .build());
            } else {
                // 单选，要么跳裁剪，要么跳返回结果
                mImagePicker.clearSelectedRes();
                mImagePicker.addSelectedResItem(position, mImagePicker.getCurrentResFolderItems().get(position));
                if (mImagePicker.getParams().isCrop()) {
                    gotoCrop();
                } else {
                    deliverResult(ResPicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, null);
                }
            }
        } else if (resItem instanceof VideoItem) {
            int p = mImagePicker.getCurrentResFolderVideoItems().indexOf(resItem);
            Bundle bundle = new Bundle();
            bundle.putInt(VideoPreviewFragment.EXTRA_SELECTED_VIDEO_POSITION, p);
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), VideoPreviewFragment.class)
                    .setFragmentDataListener(new FragmentDataListener() {
                        @Override
                        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                            deliverResult(requestCode, resultCode, bundle);
                        }
                    })
                    .setBundle(bundle)
                    .build());
        }
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 11) {
            mSelfActivity = (boolean) data;
        }
    }

    /**
     * 跳转到裁剪页面
     */
    private void gotoCrop() {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), ImageCropFragment.class)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
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
            mFragmentDataListener.onFragmentResult(requestCode, resultCode, bundle, null);
        }
        if (!mSelfActivity) {
            U.getFragmentUtils().popFragment(ResPickerFragment.this);
        }
    }
}
