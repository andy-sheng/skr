package com.common.core.upload;

/**
 * Ks3上传控制器，做金山云上传工作，这个类只供UploadFileLoader使用，对其包里的类不可见
 */
public class Ks3FileUploader {
    private static final String TAG = Ks3FileUploader.class.getSimpleName();

    private static final long SMALL_PAGE_SIZE = 500 * 1024;
    private static final long LARGE_PAGE_SIZE = 5 * 1024 * 1024; //5M

    public static long PART_SIZE = SMALL_PAGE_SIZE;
    public static final long MULTI_UPLOAD_THREADHOLD =  20 * 1024 * 1024; //1M
    public static final long LARGE_FILE_SIZE = 5 * 1024 * 1024;


    public Ks3FileUploader() {
    }


    public boolean startUpload() {
        return false;
    }
}

