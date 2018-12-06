package com.common.loadsir;

import com.kingja.loadsir.core.LoadSir;

public class LoadSirManager {
    static boolean hasInit = false;

    private static void tryInitDefault() {
        if (!hasInit) {
            LoadSir.beginBuilder();
            hasInit = true;
        }
    }

    public static LoadSir getDefault() {
        tryInitDefault();
        return LoadSir.getDefault();
    }


}
