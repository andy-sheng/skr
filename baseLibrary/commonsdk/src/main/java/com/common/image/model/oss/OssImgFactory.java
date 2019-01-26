package com.common.image.model.oss;

import android.net.Uri;
import android.text.TextUtils;

import com.common.image.model.oss.effect.OssImgBlur;
import com.common.image.model.oss.effect.OssImgBright;
import com.common.image.model.oss.effect.OssImgContrast;
import com.common.image.model.oss.effect.OssImgSharpen;
import com.common.image.model.oss.format.OssImgFormat;
import com.common.image.model.oss.format.OssImgQuality;

/**
 * 使用强大的阿里oss系统的图片处理
 * https://help.aliyun.com/document_detail/44686.html?spm=a2c4g.11186623.6.1157.49f753b3esuUNU
 */
public class OssImgFactory {

    public static OssImgResize.Builder newResizeBuilder() {
        return new OssImgResize.Builder();
    }

    public static OssImgCrop.Builder newCropBuilder() {
        return new OssImgCrop.Builder();
    }

    public static OssImgRounded.Builder newRoundBuilder() {
        return new OssImgRounded.Builder();
    }

    public static OssImgCircle.Builder newCircleBuilder() {
        return new OssImgCircle.Builder();
    }

    /* 图片效果 */
    public static OssImgBlur.Builder newBlurBuilder() {
        return new OssImgBlur.Builder();
    }

    public static OssImgBright.Builder newBrightBuilder() {
        return new OssImgBright.Builder();
    }

    public static OssImgContrast.Builder newContrastBuilder() {
        return new OssImgContrast.Builder();
    }

    public static OssImgSharpen.Builder newSharpenBuilder() {
        return new OssImgSharpen.Builder();
    }

    /* 图片格式化 */

    public static OssImgFormat.Builder newFormatBuilder() {
        return new OssImgFormat.Builder();
    }

    public static OssImgQuality.Builder newQualityBuilder() {
        return new OssImgQuality.Builder();
    }


    /**
     * 生成阿里oss 图片处理的get url
     * 如
     * http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/1111.jpg?x-oss-process=image/resize,w_480,h_1080/circle,r_500/blur,r_30,s_20
     *
     * @param url
     * @param ossProcessors
     * @return
     */
    public static String addOssParams(String url, IOssParam[] ossProcessors) {
        Uri uri = Uri.parse(url);
        if (uri != null) {
            StringBuilder paramsSb = new StringBuilder();
            for (IOssParam oss : ossProcessors) {
                paramsSb.append(oss.getOpDesc());
            }
//            String origin = uri.getQueryParameter("x-oss-process");
//            if (!TextUtils.isEmpty(origin)) {
//            替换掉原有的,后来想象也不能覆盖，先注释掉
//                url = url.replace("x-oss-process=" + origin, paramsSb.toString());
//            } else {

            String query = uri.getQuery();
            if (TextUtils.isEmpty(query)) {
                paramsSb.insert(0, "x-oss-process=image");
                url = url + "?" + paramsSb.toString();
            } else {
                String f = "x-oss-process=image";
                int index = url.indexOf(f);
                if(index>=0){
                    // 以及有图片处理相关了
                    String p1 = url.substring(0,index+f.length());
                    String p2 = url.substring(index+f.length(),url.length());
                    url = p1+paramsSb.toString()+p2;
                }else{
                    url = url + "&" + paramsSb.toString();
                }
            }
            return url;
        } else {
            return url;
        }
    }
}
