package com.zq.person.view;

import com.zq.person.model.PhotoModel;

import java.util.List;

public interface IPhotoWallView {

    void addPhoto(List<PhotoModel> list, boolean clear, int totalNum);

    void insertPhoto(PhotoModel photoModel);

    void deletePhoto(PhotoModel photoModel,boolean numchange);

    void updatePhoto(PhotoModel imageItem);

    void loadDataFailed();
}
