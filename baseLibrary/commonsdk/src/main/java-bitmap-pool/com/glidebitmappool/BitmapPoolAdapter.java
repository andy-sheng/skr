/*
 *    Copyright (C) 2016 Amit Shekhar
 *    Copyright (C) 2011 Android Open Source Project
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.glidebitmappool;

import android.graphics.Bitmap;
import android.os.Build;

import com.umeng.commonsdk.statistics.SdkVersion;

import java.util.Set;

/**
 * Created by amitshekhar on 17/06/16.
 */
public class BitmapPoolAdapter {


    private BitmapPoolAdapter(int maxSize) {
    }

    public static void initialize(int maxSize) {
    }

    public static void initialize(int maxSize, Set<Bitmap.Config> allowedConfigs) {
    }

    public static void putBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public static Bitmap getBitmap(int width, int height, Bitmap.Config config) {
        return Bitmap.createBitmap(width, height, config);
    }

    public static void clearMemory() {
    }

}
