package com.common.utils;

import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VideoUtils {
    VideoUtils() {
    }

    private static float MAX_CUT_RATE = 0.2f;

    /**
     * 判断视频渲染模式
     * @param layoutWidth
     * @param layoutHeight
     * @param videoWidth
     * @param videoHeight
     * @return
     */
    public boolean isNeedFill(int layoutWidth, int layoutHeight, int videoWidth, int videoHeight) {
        if (videoHeight == 0 || layoutHeight == 0) {
            return false;
        }

        float cutRate = 0;//全屏播放截掉画面占比
        if (layoutWidth > layoutHeight) {
            //横屏的
            if (videoWidth > videoHeight) {
                float videoRate = videoWidth / (float) videoHeight;
                float viewRate = layoutWidth / (float) layoutHeight;
                if (viewRate > videoRate) {//需要截高,                                                                                      /
                    float targetLayoutHeight = layoutWidth / videoRate;
                    cutRate = (targetLayoutHeight - layoutHeight) / targetLayoutHeight;
                } else {//需要截宽
                    float targetLayoutWidth = layoutHeight * videoRate;
                    cutRate = (targetLayoutWidth - layoutWidth) / targetLayoutWidth;
                }
                if (cutRate >= MAX_CUT_RATE) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        } else {
            //竖屏的
            if (videoWidth < videoHeight) {
                float videoRate = videoWidth / (float) videoHeight;
                float viewRate = layoutWidth / (float) layoutHeight;
                if (viewRate < videoRate) {//需要截宽
                    float targetLayoutWidth = layoutHeight * videoRate;
                    cutRate = (targetLayoutWidth - layoutWidth) / targetLayoutWidth;
                } else {//需要截高
                    float targetLayoutHeight = layoutWidth / videoRate;
                    cutRate = (targetLayoutHeight - layoutHeight) / targetLayoutHeight;
                }
                if (cutRate >= MAX_CUT_RATE) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
