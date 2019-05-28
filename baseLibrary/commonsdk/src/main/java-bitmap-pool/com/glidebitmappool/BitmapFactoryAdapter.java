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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;

import com.glidebitmappool.internal.Util;

import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * Created by amitshekhar on 18/06/16.
 */
public class BitmapFactoryAdapter {

    public static Bitmap decodeFile(String pathName) {
        return decodeFile(pathName, Bitmap.Config.RGB_565);
    }

    public static Bitmap decodeFile(String pathName, Bitmap.Config inPreferredConfig) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = inPreferredConfig;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static Bitmap decodeResource(Resources res, int id) {
        return BitmapFactory.decodeResource(res, id);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        return decodeByteArray(data, offset, length, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length, Bitmap.Config inPreferredConfig) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = inPreferredConfig;
        return BitmapFactory.decodeByteArray(data, offset, length, options);
    }

    public static Bitmap decodeStream(InputStream is) {
        return BitmapFactory.decodeStream(is);
    }


}
