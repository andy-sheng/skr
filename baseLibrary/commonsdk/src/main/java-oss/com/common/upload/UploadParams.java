package com.common.upload;

public class UploadParams {
    private String filePath;

    UploadParams() {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

        public UploadParams build() {
            return mParams;
        }

        public UploadTask startUploadAsync(UploadCallback uploadCallback) {
            UploadTask uploadTask = new UploadTask(mParams);
            return uploadTask.startUpload(uploadCallback);
        }
    }
}
