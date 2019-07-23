package com.respicker.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.common.base.R;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.glidebitmappool.BitmapPoolAdapter;
import com.respicker.ResPicker;
import com.respicker.model.ImageItem;
import com.respicker.model.ResItem;
import com.respicker.view.CropImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * 裁剪 Fragment
 * 使用方法在  mImagePicker.addSelectedImageItem(0, imageItem); 设好参数后
 * 直接跳转
 */
public class ImageCropFragment extends ImageBaseFragment {
    CommonTitleBar mTitleBar;
    TextView mBtnOk;
    CropImageView mCropImageView;
    Bitmap mBitmap;

    ResPicker mImagePicker;
    ArrayList<ResItem> mImageItems;

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
        mImagePicker = ResPicker.getInstance();
        mTitleBar = getRootView().findViewById(R.id.titlebar);
        mTitleBar.getLeftImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                U.getFragmentUtils().popFragment(ImageCropFragment.this);
            }
        });
        mBtnOk = (TextView) mTitleBar.getRightCustomView();
        mBtnOk.setText("完成");
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropImageView.saveBitmapToFile(mImagePicker.getCropCacheFolder()
                        , mImagePicker.getParams().getOutPutX()
                        , mImagePicker.getParams().getOutPutY()
                        , mImagePicker.getParams().isSaveRectangle());
            }
        });


        mCropImageView = getRootView().findViewById(R.id.cv_crop_image);
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
                deliverResult(ResPicker.RESULT_CODE_ITEMS, Activity.RESULT_OK, null);
            }

            @Override
            public void onBitmapSaveError(File file) {

            }
        });

        mCropImageView.setFocusStyle(mImagePicker.getParams().getCropStyle());
        mCropImageView.setFocusWidth(mImagePicker.getParams().getFocusWidth());
        mCropImageView.setFocusHeight(mImagePicker.getParams().getFocusHeight());

        mImageItems = mImagePicker.getSelectedResList();
        if(mImageItems == null || mImageItems.size() == 0){
            U.getFragmentUtils().popFragment(this);
            return;
        }

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

        U.getSoundUtils().preLoad(getTAG(), R.raw.normal_back);
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
            BitmapPoolAdapter.putBitmap(mBitmap);
            mBitmap = null;
        }
        U.getSoundUtils().release(getTAG());
    }

    /**
     * 交付选择结果,返回结果给调用方式
     */
    private void deliverResult(int requestCode, int resultCode, Bundle bundle) {
        //裁剪完成,直接返回数据，数据存在 mImagePicker 中
        if (getMFragmentDataListener() != null) {
            // bundle.getParcelableArrayList(ImagePicker.EXTRA_RESULT_ITEMS);
            getMFragmentDataListener().onFragmentResult(requestCode, resultCode, bundle, null);
        }
        U.getFragmentUtils().popFragment(this);
    }
}
