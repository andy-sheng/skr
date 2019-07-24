package com.zq.mediaengine.util.gles;

/**
 * Load gl program failed exception.
 *
 * @hide
 */
public class GLProgramLoadException extends RuntimeException {
    public GLProgramLoadException() {}

    public GLProgramLoadException(String msg) {
        super(msg);
    }
}
