package com.imagepicker.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.common.base.BaseFragment;
import com.imagepicker.ResPicker;

public abstract class ImageBaseFragment extends BaseFragment {

    /**
     * 可以在此恢复数据
     * @param savedInstanceState
     */
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        ResPicker.getInstance().onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 当系统认为你的fragment存在被销毁的可能时，onSaveInstanceState 就会被调用
     * 不包括用户主动退出fragment导致其被销毁，比如按BACK键后fragment被主动销毁
     * @param outState
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ResPicker.getInstance().onSaveInstanceState(outState);
    }
}
