package com.respicker.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ResFolder implements Serializable {

    String name;  //当前文件夹的名字
    String path;  //当前文件夹的路径
    ImageItem cover;   //当前文件夹需要要显示的缩略图，默认为最近的一次图片
    ArrayList<ResItem> resItems = new ArrayList<>();  //当前文件夹下所有资源的集合

    ArrayList<ImageItem> imageItems =new ArrayList<>();  //当前文件夹下所有图片的集合
    ArrayList<VideoItem> videoItems= new ArrayList<>();  //当前文件夹下所有视频的集合

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

    public ImageItem getCover() {
        return cover;
    }

    public void setCover(ImageItem cover) {
        this.cover = cover;
    }

    public ArrayList<ResItem> getResItems() {
        return resItems;
    }

    public ArrayList<ImageItem> getImageItems() {
        return imageItems;
    }

    public ArrayList<VideoItem> getVideoItems() {
        return videoItems;
    }

    /**
     * 只要文件夹的路径和名字相同，就认为是相同的文件夹
     */
    @Override
    public boolean equals(Object o) {
        try {
            ResFolder other = (ResFolder) o;
            return this.path.equalsIgnoreCase(other.path) && this.name.equalsIgnoreCase(other.name);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return this.path.hashCode() & this.name.hashCode();
    }
}
