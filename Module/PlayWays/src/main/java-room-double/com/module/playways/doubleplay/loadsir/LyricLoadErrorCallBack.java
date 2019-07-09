package com.module.playways.doubleplay.loadsir;

import com.kingja.loadsir.callback.Callback;
import com.module.playways.R;

public class LyricLoadErrorCallBack extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.lyric_load_error_layout;
    }
}
