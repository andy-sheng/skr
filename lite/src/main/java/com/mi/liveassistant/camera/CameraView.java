package com.mi.liveassistant.camera;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;

import com.xiaomi.rendermanager.videoRender.VideoStreamsView;

/**
 * Created by chenyong on 2017/4/27.
 */

public class CameraView extends VideoStreamsView {

    public CameraView(Context context, Point point) {
        super(context, point);
    }

    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

}
