// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
package com.example.videortc;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

public class ResourceHelper {

    private static File sRootDir;
    public static final String ResourceZip = "resource.zip";
    public static final String FACE = "model/ttfacemodel/tt_face_v5.0.model";
    public static final String DetectParamFile = "model/handmodel/tt_hand_det_v7.0.model";
    public static final String BoxRegParamFile = "model/handmodel/tt_hand_box_reg_v8.0.model";
    public static final String GestureParamFile = "model/handmodel/tt_hand_gesture_v7.0.model";
    public static final String KeyPointParamFile = "model/handmodel/tt_hand_kp_v4.0.model";
    public static final String SegParamFile = "model/handmodel/tt_hand_seg_v1.0.model";
    public static final String BEAUTY_RESOURCE = "res/BeautyResource.bundle";
    public static final String FILTER_RESOURCE = "res/FilterResource.bundle/1";
    public static final String RESHAPE_RESOURCE = "res/ReshapeResource.bundle";
    public static final String MAKEUP_RESOURCE = "res/BuildinMakeup.bundle";
    public static final String STICKER_RESOURCE = "res/stickers";
    private static final String FACEEXTA = "model/ttfacemodel/tt_face_extra_v8.0.model";
    private static final String FACEATTRI = "model/ttfaceattri/tt_face_attribute_v3.2.model";
    private static final String FACEVERIFY = "model/ttfaceverify/tt_faceverify_v3.0.model";
    private static final String SKELETON = "model/ttskeletonmodel/tt_skeleton_v3.0.model";
    private static final String PORTRAITMATTING = "model/ttportaitmatting/tt_matting_v7.0.model";
    private static final String HAIRPARSING = "model/hairparser/tt_hair_v6.2.model";

    public static void init(final Context context) {
        sRootDir = context.getExternalFilesDir("assets");
    }


    public static String getModelDir() {
        File file = new File(new File(sRootDir, "model"), "");
        return file.getAbsolutePath();
    }

    public static String getFaceModelPath() {
        File file = new File(new File(sRootDir, FACE), "");
        return file.getAbsolutePath();
    }

    public static String getFaceExtaModelPath() {
        File file = new File(new File(sRootDir, FACEEXTA), "");
        return file.getAbsolutePath();
    }

    public static String getFaceAttriModelPath() {
        File file = new File(new File(sRootDir, FACEATTRI), "");
        return file.getAbsolutePath();
    }

    public static String getFaceVerifyModelPath() {
        File file = new File(new File(sRootDir, FACEVERIFY), "");
        return file.getAbsolutePath();
    }

    public static String getSkeletonModelPath() {
        File file = new File(new File(sRootDir, SKELETON), "");
        return file.getAbsolutePath();
    }

    public static String getPortraitmattingModelPath()
    {
        File file = new File(new File(sRootDir, PORTRAITMATTING), "");
        return file.getAbsolutePath();
    }

    public static String getHairParsingModelPath()
    {
        File file = new File(new File(sRootDir, HAIRPARSING), "");
        return file.getAbsolutePath();
    }

    public static String getHandModelPath(String path) {
        File file = new File(new File(sRootDir, path), "");
        return file.getAbsolutePath();
    }

    public static String getLicensePath(String path) {
        File file = new File(new File(sRootDir, "license"), path);
        return file.getAbsolutePath();
    }

    public static String getStickersPath() {
        File file = new File(new File(sRootDir, "res"), "stickers");
        return file.getAbsolutePath();
    }



    public static File[] getBeautyResources() {
        return getResources(BEAUTY_RESOURCE);
    }

    public static File[] getFilterResources() {
        return getResources(FILTER_RESOURCE);
    }

    public static File[] getReshapeResource() {
        return getResources(RESHAPE_RESOURCE);
    }

    public static File[] getMakeUpResource() {
        return getResources(MAKEUP_RESOURCE);
    }

    public static File[] getResources(String type) {
        File file = new File(new File(sRootDir, type), "");
        if (file.exists() && file.isDirectory())
            return file.listFiles();
        return new File[0];
    }

    public static String getStickerPath(String name) {
        return getStickersPath() + File.separator + name;

    }

    public static boolean isResourceReady(Context context, int versionCode) {

        SharedPreferences preferences=context.getSharedPreferences("user", Context.MODE_PRIVATE);
       boolean resourceReady = preferences.getBoolean("resource", false);
        int preVersioncode = preferences.getInt("versionCode", 0);

        // 如果上次已经拷贝过 继续检查版本号
       if (resourceReady && versionCode == preVersioncode){
           return true;
       }
       return false;
    }

    public static void setResourceReady(Context context, boolean isReady, int versionCode){
        SharedPreferences preferences=context.getSharedPreferences("user", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("resource", isReady);
        editor.putInt("versionCode", versionCode);
        editor.commit();
    }

    public static String getDownloadedStickerDir(){
        File file = new File(new File(sRootDir, "download"), "sticker");
        if (!file.exists()) {
            file.mkdirs();

        }
        return file.getAbsolutePath();


    }




}
