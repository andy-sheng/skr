package com.imagepicker.model;

public class VideoItem extends ResItem {
    int videoId;
    String name;       //视频的名字
    String path;       //视频的路径
    long size;         //视频的大小
    int width;         //视频的宽度
    int height;        //视频的高度
    String mimeType;   //视频的类型
    long addTime;      //视频的创建时间
    ImageItem thumb;  //缩略图信息

    @Override
    public int getType() {
        return RES_VIDEO;
    }

    public int getVideoId() {
        return videoId;
    }

    public void setVideoId(int videoId) {
        this.videoId = videoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public ImageItem getThumb() {
        return thumb;
    }

    public void setThumb(ImageItem thumb) {
        this.thumb = thumb;
    }
}
