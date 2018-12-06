package com.common.loadsir;

import com.common.loadsir.callback.ErrorCallback;
import com.common.loadsir.callback.LoadingCallback;
import com.common.loadsir.callback.LottieEmptyCallback;
import com.kingja.loadsir.core.LoadSir;

public class LoadSirManager {
    static boolean hasInit = false;

    private static void tryInitDefault() {
        if (!hasInit) {
            LoadSir.beginBuilder()
                    .addCallback(new LoadingCallback())
                    .addCallback(new ErrorCallback())
                    .addCallback(new LottieEmptyCallback())
            .commit();
            hasInit = true;
        }
    }

    public static LoadSir getDefault() {
        tryInitDefault();
        return LoadSir.getDefault();
    }


}
