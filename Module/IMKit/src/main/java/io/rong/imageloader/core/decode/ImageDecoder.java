//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.core.decode;

import android.graphics.Bitmap;

import java.io.IOException;

import io.rong.imageloader.core.decode.ImageDecodingInfo;

public interface ImageDecoder {
  Bitmap decode(ImageDecodingInfo var1) throws IOException;
}
