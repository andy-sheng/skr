package com.respicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.respicker.fragment.ResPickerFragment;


public class ResPickerActivity extends BaseActivity {
    public static final int REQ_CODE_RES_PICK = 8;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, ResPickerFragment.class)
                .setHasAnimation(false)
                .setAddToBackStack(false)
                .addDataBeforeAdd(11,true)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                        ResPickerActivity.this.setResult(Activity.RESULT_OK);
                        ResPickerActivity.this.finish();
                    }
                })
                .build()
        );
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    /**
     * 启动前，请用设置好相关参数
     * ResPicker.getInstance().setParams(ResPicker.newParamsBuilder()
     * .setSelectLimit(1)
     * .setCropStyle(CropImageView.Style.CIRCLE)
     * .build()
     * );
     * 结果也会在 onActivity中用 ResPicker.getInstance().getSingleSelectedImage() 返回
     *
     * @param activity
     */
    public static void open(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, ResPickerActivity.class);
        activity.startActivityForResult(intent, REQ_CODE_RES_PICK);
    }
}
