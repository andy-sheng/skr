package com.common.image.model.oss;

import android.net.Uri;
import android.text.TextUtils;

/**
 * 使用强大的阿里oss系统的图片处理
 * https://help.aliyun.com/document_detail/44686.html?spm=a2c4g.11186623.6.1157.49f753b3esuUNU
 */
public class OssPsFactory {

    public static OssImgResize.Builder newResizeBuilder() {
        return new OssImgResize.Builder();
    }

    public static OssImgCrop.Builder newCropBuilder() {
        return new OssImgCrop.Builder();
    }

    /**
     * 生成阿里oss 图片处理的get url
     * 如
     * http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/1111.jpg?x-oss-process=image/resize,w_480,h_1080/circle,r_500/blur,r_30,s_20
     * @param url
     * @param ossProcessors
     * @return
     */
    public static String fillOssParams(String url, IOssParam[] ossProcessors) {
        Uri uri = Uri.parse(url);
        if (uri != null) {
            StringBuilder paramsSb = new StringBuilder();
            for (IOssParam oss : ossProcessors) {
                paramsSb.append(oss.getOpDesc());
            }
            paramsSb.insert(0, "x-oss-process=image");
            String origin = uri.getQueryParameter("x-oss-process");
            if (!TextUtils.isEmpty(origin)) {
                url = url.replace("x-oss-process=" + origin, paramsSb.toString());
            } else {
                String query = uri.getQuery();
                if (TextUtils.isEmpty(query)) {
                    url = url + "?" + paramsSb.toString();
                } else {
                    url = url + "&" + paramsSb.toString();
                }
            }
            return url;
        } else {
            return url;
        }
    }
}
