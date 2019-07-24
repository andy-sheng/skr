package com.component.person.view;

import com.component.person.model.PhotoModel;

import java.util.List;

public interface IPhotoWallView {

    void addPhoto(List<PhotoModel> list, boolean clear, int totalNum);

    void insertPhoto(PhotoModel photoModel);

    void deletePhoto(PhotoModel photoModel,boolean numchange);

    void updatePhoto(PhotoModel imageItem);

    void loadDataFailed();
}
