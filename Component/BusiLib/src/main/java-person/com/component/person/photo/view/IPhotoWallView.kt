package com.component.person.photo.view

import com.component.person.photo.model.PhotoModel

interface IPhotoWallView {

    fun addPhoto(offset: Int, list: List<PhotoModel>?, clear: Boolean, totalNum: Int)

    fun insertPhoto(photoModel: PhotoModel)

    fun deletePhoto(photoModel: PhotoModel, numchange: Boolean)

    fun updatePhoto(imageItem: PhotoModel)

    fun loadDataFailed()
}
