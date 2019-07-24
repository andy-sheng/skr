package com.component.mediaengine.capture.camera;

/**
 * This class represents the condition that we cannot open the camera hardware
 * successfully. For example, another process is using the camera.
 *
 * @hide
 */
public class CameraHardwareException extends Exception {

    private static final long serialVersionUID = 1021271504206608539L;

    public CameraHardwareException() {

    }

    public CameraHardwareException(Throwable t) {
        super(t);
    }
}
