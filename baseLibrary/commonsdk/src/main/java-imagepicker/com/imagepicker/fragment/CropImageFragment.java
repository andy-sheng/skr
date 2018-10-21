package com.imagepicker.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.base.R;
import com.common.utils.U;
import com.imagepicker.ImagePicker;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * 裁剪 Fragment
 * 使用方法在  mImagePicker.addSelectedImageItem(0, imageItem); 设好参数后
 * 直接跳转
 */
public class CropImageFragment extends BaseFragment {
    View mBtnBack;
    Button mBtnOk;
    TextView mTvDes;
    CropImageView mCropImageView;
    Bitmap mBitmap;

    ImagePicker mImagePicker;
    ArrayList<ImageItem> mImageItems;

    /**
     * 参数太多一层层往里传确实太麻烦
     * 直接用 ImagePicker picker 全局拿吧，在Activity 销毁时保存下
     */
//    String mSaveFilePath;
//    int mOutputX = 800;
//    int mOutputY = 800;
//    int mFocusWidth = 380;
//    int mFocusHeight = 380;
//    boolean mIsSaveRectangle = true;
//    CropImageView.Style mCropStyle = CropImageView.Style.RECTANGLE;
    @Override
    public int initView() {
        return R.layout.fragment_cropimage_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mImagePicker = ImagePicker.getInstance();
        mBtnBack = mRootView.findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                U.getFragmentUtils().popFragment(CropImageFragment.this);
            }
        });
        mBtnOk = mRootView.findViewById(R.id.btn_ok);
        mBtnOk.setText("完成");
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.saveBitmapToFile(mImagePicker.getCropCacheFolder()
                        , mImagePicker.getOutPutX()
                        , mImagePicker.getOutPutY()
                        , mImagePicker.isSaveRectangle());
            }
        });

        mTvDes = (TextView) mRootView.findViewById(R.id.tv_des);
        mTvDes.setText("图片裁剪");

        mCropImageView = mRootView.findViewById(R.id.cv_crop_image);
        mCropImageView.setOnBitmapSaveCompleteListener(new CropImageView.OnBitmapSaveCompleteListener() {
            @Override
            public void onBitmapSaveSuccess(File file) {
                mImageItems.remove(0);
                ImageItem imageItem = new ImageItem();
                imageItem.setPath(file.getAbsolutePath());
                mImageItems.add(imageItem);


                /**
                 * 数据从这里返回
                 */
                if (mFragmentDataListener != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(ImagePicker.EXTRA_RESULT_ITEMS, mImageItems);
                    mFragmentDataListener.onFragmentResult(ImagePicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, bundle);

                    U.getFragmentUtils().popFragment(CropImageFragment.this);
                }
// 其余的返回方式没必要支持
// else{
//                    // 如果没有设置回调
//                    Intent intent = new Intent();
//                    intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, mImageItems);
//                    if(getTargetFragment()!=null){
//                        getTargetFragment().onActivityResult(ImagePicker.RESULT_CODE_ITEMS,Activity.RESULT_OK,intent);
//                        U.getFragmentUtils().popFragment(CropImageFragment.this);
//                    }else{
//                        if(getActivity()!=null){
//                            getActivity().setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
//                            getActivity().finish();
//                        }
//                    }
//                }
            }

            @Override
            public void onBitmapSaveError(File file) {

            }
        });

        mCropImageView.setFocusStyle(mImagePicker.getCropStyle());
        mCropImageView.setFocusWidth(mImagePicker.getFocusWidth());
        mCropImageView.setFocusHeight(mImagePicker.getFocusHeight());

        mImageItems = mImagePicker.getSelectedImages();
        String imagePath = mImageItems.get(0).getPath();

        //缩放图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        /**
         * inSampleSize 这个属性只认2的整数倍为有效.
         * 比如你将 inSampleSize 赋值为2,那就是每隔2行采1行,每隔2列采一列,那你解析出的图片就是原图大小的1/4.
         * 这个值也可以填写非2的倍数,非2的倍数会被四舍五入.
         */
        options.inSampleSize = calculateInSampleSize(options, U.getDisplayUtils().getScreenWidth(), U.getDisplayUtils().getScreenHeight());
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeFile(imagePath, options);

        mCropImageView.setImageBitmap(mCropImageView.rotate(mBitmap, U.getBitmapUtils().getBitmapDegree(imagePath)));
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        mCropImageView.setOnBitmapSaveCompleteListener(null);
        if (null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

}
