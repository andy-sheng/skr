package com.common.upload;

public class UploadParams {
    private String filePath;// 设置是否需要帮忙压缩一下，如果开启，就内部识别文件到达一定大小，用luban压缩一下

    private String fileName;// 注意，不要乱设，默认不设置

    private boolean needCompress = false;

    private boolean needMonitor = false;// 特别依赖回调的业务可以开启监听，防止没有回调

    private FileType fileType = FileType.picture;

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

    public void setNeedMonitor(boolean needMonitor) {
        this.needMonitor = needMonitor;
    }

    public boolean isNeedMonitor() {
        return needMonitor;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public FileType getFileType() {
        return fileType;
    }

    public static Builder newBuilder(String filePath) {
        return new Builder().setFilePath(filePath);
    }

    public enum FileType {
        audit("audit"), profilepic("profile-pic"), picture("picture"), audioAi("audio"), midiAi("midi"), log("androidLog");

        private String ossSavaDir;

        FileType(String d) {
            ossSavaDir = d;
        }

        public String getOssSavaDir() {
            return ossSavaDir;
        }
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

        public Builder setFileType(FileType fileType) {
            this.mParams.setFileType(fileType);
            return this;
        }

        /**
         * 为了保证能监听到回调事件设置的
         * 一般不需要设置，除非不回调会导致逻辑有问题可以设置上
         * @param needMonitor
         * @return
         */
        public Builder setNeedMonitor(boolean needMonitor) {
            this.mParams.setNeedMonitor(needMonitor);
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
