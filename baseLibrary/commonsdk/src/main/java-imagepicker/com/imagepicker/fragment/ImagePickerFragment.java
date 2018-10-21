package com.imagepicker.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
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
import com.imagepicker.ImagePicker;
import com.imagepicker.model.ImageItem;

import java.util.List;

public class ImagePickerFragment extends BaseFragment {

    public static final String EXTRAS_TAKE_PICKERS = "TAKE";
    private boolean mDirectPhoto = false; // 默认不是直接调取相机
    private ImagePicker mImagePicker;

    LinearLayout mContent;
    RecyclerView mRecyclerView;
    RelativeLayout mFooterBar;
    RelativeLayout mLlDir;
    TextView mTvDir;
    TextView mBtnPreview;

    @Override
    public int initView() {
        return R.layout.fragment_imagepicker_grid_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mImagePicker = ImagePicker.getInstance();
        mContent = (LinearLayout) mRootView.findViewById(R.id.content);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mFooterBar = (RelativeLayout) mRootView.findViewById(R.id.footer_bar);
        mLlDir = (RelativeLayout) mRootView.findViewById(R.id.ll_dir);
        mTvDir = (TextView) mRootView.findViewById(R.id.tv_dir);
        mBtnPreview = (TextView) mRootView.findViewById(R.id.btn_preview);

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
//            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(EXTRAS_IMAGES);
//            mImagePicker.setSelectedImages(images);
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
                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(getActivity(), CropImageFragment.class)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
                                bundle.getParcelableArrayList(ImagePicker.EXTRA_RESULT_ITEMS);
                            }
                        })
                        .build());
            } else {
                Intent intent = new Intent();
                intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, mImagePicker.getSelectedImages());
//                setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
//                finish();
            }
        }
        return false;
    }
}
