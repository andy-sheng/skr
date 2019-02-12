package com.doraemon;

import com.common.utils.U;
import com.didichuxing.doraemonkit.DoraemonKit;

public class DoraemonManager {
    public final static String TAG = "DoraemonManager";

    public static void init(){
        DoraemonKit.install(U.app());
//
//        // H5任意门功能需要，非必须
//        DoraemonKit.setWebDoorCallback(new WebDoorManager.WebDoorCallback() {
//            @Override
//            public void overrideUrlLoading(String s) {
//                // 使用自己的H5容器打开这个链接
//                Log.d(TAG,"overrideUrlLoading" + " s=" + s);
//
//            }
//        });
    }

}
