package com.zq.mediaengine.capture.camera;

import android.graphics.Rect;

/**
 * Interface of CameraHintView for CameraTouchHelper.
 */

public interface ICameraHintView {
    /**
     * Start camera auto focus with given area.
     *
     * @param rect area to focus
     */
    void startFocus(Rect rect);

    /**
     * Camera auto focus complete.
     *
     * @param success is auto focus success.
     */
    void setFocused(boolean success);

    /**
     * Update zoom ratio value while zooming.
     *
     * @param val zoom ratio to show
     */
    void updateZoomRatio(float val);
}
