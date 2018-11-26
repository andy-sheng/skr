//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview.gestures;

import android.content.Context;
import android.os.Build.VERSION;

import io.rong.photoview.gestures.CupcakeGestureDetector;
import io.rong.photoview.gestures.EclairGestureDetector;
import io.rong.photoview.gestures.FroyoGestureDetector;
import io.rong.photoview.gestures.GestureDetector;
import io.rong.photoview.gestures.OnGestureListener;

public final class VersionedGestureDetector {
    public VersionedGestureDetector() {
    }

    public static GestureDetector newInstance(Context context, OnGestureListener listener) {
        int sdkVersion = VERSION.SDK_INT;
        Object detector;
        if (sdkVersion < 5) {
            detector = new CupcakeGestureDetector(context);
        } else if (sdkVersion < 8) {
            detector = new EclairGestureDetector(context);
        } else {
            detector = new FroyoGestureDetector(context);
        }

        ((GestureDetector) detector).setOnGestureListener(listener);
        return (GestureDetector) detector;
    }
}
