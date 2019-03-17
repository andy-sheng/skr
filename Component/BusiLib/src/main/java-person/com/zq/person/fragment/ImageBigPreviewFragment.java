package com.zq.person.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.processor.BlurPostprocessor;
import com.common.image.fresco.processor.GrayPostprocessor;
import com.common.image.model.HttpImage;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.image.model.oss.format.OssImgFormat;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.R;
import com.facebook.drawee.drawable.ScalingUtils;
import com.imagebrowse.ImageBrowseView;

/**
 * 头像大图预览
 */
public class ImageBigPreviewFragment extends BaseFragment {

    public final static String TAG = "ImageBigPreviewFragment";
    public final static String BIG_IMAGE_PATH = "big_image_path";

    CommonTitleBar mTitlebar;
    ImageBrowseView mImageIv;

    @Override
    public int initView() {
        return R.layout.big_image_preview_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mImageIv = (ImageBrowseView) mRootView.findViewById(R.id.image_iv);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                U.getFragmentUtils().popFragment(ImageBigPreviewFragment.this);
            }
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            String imagePath = bundle.getString(BIG_IMAGE_PATH);
            loadImage(imagePath);
        }

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }


    private void loadImage(String path) {
        HttpImage image = ImageFactory.newHttpImage(path)
                //.addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_640.getW()).build())
                .build();
        mImageIv.load(image);
    }


    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }
}
