package com.common.upload;

public class UploadParams {
    private String filePath;// 设置是否需要帮忙压缩一下，如果开启，就内部识别文件到达一定大小，用luban压缩一下

    private String fileName;// 注意，不要乱设，默认不设置

    private boolean needCompress = false;

    UploadParams() {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void set(String filePath) {
        this.filePath = filePath;
    }

    public boolean isNeedCompress() {
        return needCompress;
    }

    public void setNeedCompress(boolean needCompress) {
        this.needCompress = needCompress;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static Builder newBuilder(String filePath) {
        return new Builder().setFilePath(filePath);
    }

    public static class Builder {
        UploadParams mParams = new UploadParams();

        Builder() {
        }

        public Builder setFilePath(String filePath) {
            mParams.setFilePath(filePath);
            return this;
        }

        public Builder setNeedCompress(boolean needCompress) {
            mParams.setNeedCompress(needCompress);
            return this;
        }

        // 注意，不要乱设，默认不设置
        public Builder setFileName(String fileName) {
            mParams.setFileName(fileName);
            return this;
        }

        public UploadParams build() {
            return mParams;
        }

        public UploadTask startUploadAsync(UploadCallback uploadCallback) {
            UploadTask uploadTask = new UploadTask(mParams);
            return uploadTask.startUpload(uploadCallback);
        }
    }
}
