package com.base.utils.image;

import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.base.global.GlobalData;

/**
 * Created by lan on 15-9-2.
 * RenderScript
 */
public class RSUtils {

    // 内置高斯模糊效果
    public static void blur(Bitmap input, float radius, Bitmap output) {
        // 使用Renderscript,虽然多了一步565转8888,但是速度还是更快
        RenderScript rs = RenderScript.create(GlobalData.app());
        Bitmap original = ImageUtils.convertRGB565ToARGB8888(input, false);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, output);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blur.setInput(allocIn);
        blur.setRadius(radius);
        blur.forEach(allocOut);

        allocOut.copyTo(output);
        rs.destroy();
        original.recycle();
    }
}
