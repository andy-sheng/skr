package com.module.home.game.model;

import com.module.home.model.SlideShowModel;

import java.util.List;

public class BannerModel {

    List<SlideShowModel> slideShowModelList;

    public BannerModel(List<SlideShowModel> slideShowModelList) {
        this.slideShowModelList = slideShowModelList;
    }

    public List<SlideShowModel> getSlideShowModelList() {
        return slideShowModelList;
    }

    public void setSlideShowModelList(List<SlideShowModel> slideShowModelList) {
        this.slideShowModelList = slideShowModelList;
    }

}
