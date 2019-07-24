package com.component.person.photo.presenter

import android.os.Handler

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.common.anim.ObjectPlayControlTemplate
import com.common.base.BaseFragment
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.upload.UploadTask
import com.common.utils.U
import com.component.person.photo.manager.PhotoDataManager
import com.respicker.model.ImageItem
import com.component.person.photo.model.PhotoModel
import com.component.person.photo.view.IPhotoWallView

import java.util.ArrayList
import java.util.HashMap

import okhttp3.MediaType
import okhttp3.RequestBody

class PhotoCorePresenter(internal var mView: IPhotoWallView, private var mFragment: BaseFragment) {

    val TAG = "PhotoCorePresenter"

    val mUserInfoServerApi: UserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    internal var mUiHandler: Handler? = Handler()

    internal var mUploadingPhoto = false
    internal var mExceedLimit = false

    internal var mPlayControlTemplate: ObjectPlayControlTemplate<PhotoModel, PhotoCorePresenter> = object : ObjectPlayControlTemplate<PhotoModel, PhotoCorePresenter>() {
        override fun accept(cur: PhotoModel): PhotoCorePresenter? {
            MyLog.d(TAG, "accept cur=$cur mUploadingPhoto=$mUploadingPhoto")
            if (mUploadingPhoto) {
                return null
            } else {
                mUploadingPhoto = true
                return this@PhotoCorePresenter
            }
        }

        override fun onStart(pm: PhotoModel, personFragment2: PhotoCorePresenter) {
            MyLog.d(TAG, "onStart" + "开始上传 PhotoModel=" + pm + " 队列还有 mPlayControlTemplate.getSize()=" + this.size)
            execUploadPhoto(pm)
        }

        override fun onEnd(pm: PhotoModel) {
            MyLog.d(TAG, "onEnd 上传结束 PhotoModel=$pm")
        }
    }

    init {
    }

    @JvmOverloads
    fun getPhotos(offset: Int, cnt: Int, callback: Callback<List<PhotoModel>>? = null) {
        MyLog.d(TAG, "getPhotos offset=$offset cnt=$cnt callback=$callback")
        ApiMethods.subscribe(mUserInfoServerApi.getPhotos(MyUserInfoManager.getInstance().uid.toInt().toLong(), offset, cnt), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result!!.errno == 0) {
                    if (result != null && result.errno == 0) {
                        val list = JSON.parseArray(result.data?.getString("pic"), PhotoModel::class.java)
                        val newOffset = result.data!!.getIntValue("offset")
                        val totalCount = result.data!!.getIntValue("totalCount")
                        if (offset == 0) {
                            // 刷新拉
                            mView.addPhoto(list, true, totalCount)
                            callback?.onCallback(1, list)
                            return
                        } else {
                            // 下拉更多拉
                            mView.addPhoto(list, false, totalCount)
                            callback?.onCallback(2, list)
                            return
                        }
                    }
                } else {
                    mView!!.loadDataFailed()
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                mView!!.loadDataFailed()
            }
        }, mFragment, ApiMethods.RequestControl("getPhotos", ApiMethods.ControlType.CancelThis))
    }

    fun uploadPhotoList(imageItems: List<ImageItem>) {
        MyLog.d(TAG, "uploadPhotoList imageItems=$imageItems")
        val list = ArrayList<PhotoModel>()
        for (imageItem in imageItems) {
            val photoModel = PhotoModel()
            photoModel.localPath = imageItem.path
            photoModel.status = PhotoModel.STATUS_WAIT_UPLOAD
            list.add(photoModel)
        }
        // 数据库中的zhukey怎么定，数据库中只存未上传成功的
        upload(list, false)
    }

    fun upload(photoModels: List<PhotoModel>?, reupload: Boolean) {
        MyLog.d(TAG, "uploadPhotoList photoModels=$photoModels")
        if (photoModels != null && photoModels.size > 0) {
            // 数据库中的zhukey怎么定，数据库中只存未上传成功的
            PhotoDataManager.insertOrUpdate(photoModels)
            for (photoModel in photoModels) {
                photoModel.status = PhotoModel.STATUS_WAIT_UPLOAD
                if (reupload) {
                    mView!!.updatePhoto(photoModel)
                } else {
                    mView!!.insertPhoto(photoModel)
                }
                mPlayControlTemplate.add(photoModel, true)
            }
        }
    }

    internal fun execUploadPhoto(photo: PhotoModel) {
        MyLog.d(TAG, "execUploadPhoto photo=$photo")

        if (photo.status == PhotoModel.Companion.STATUS_DELETE) {
            MyLog.d(TAG, "execUploadPhoto imageItem=$photo 用户删除了，取消上传")
            mUploadingPhoto = false
            mPlayControlTemplate.endCurrent(photo)
            return
        }
        if (mExceedLimit) {
            photo.status = PhotoModel.STATUS_FAILED_LIMIT
            mView!!.updatePhoto(photo)
            mUploadingPhoto = false
            mPlayControlTemplate.endCurrent(photo)
            return
        }
        photo.status = PhotoModel.STATUS_UPLOADING
        mView!!.updatePhoto(photo)
        val uploadTask = UploadParams.newBuilder(photo.localPath)
                .setNeedCompress(true)
                .setNeedMonitor(true)
                .setFileType(UploadParams.FileType.profilepic)
                .startUploadAsync(object : UploadCallback {
                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {

                    }

                    override fun onSuccessNotInUiThread(url: String) {
                        MyLog.d(TAG, "上传成功 url=$url")
                        // 上传到服务器
                        val map = HashMap<String, Any>()

                        val pics = ArrayList<JSONObject>()
                        val jsonObject = JSONObject()
                        jsonObject["picPath"] = url
                        pics.add(jsonObject)
                        map["pic"] = pics
                        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

                        ApiMethods.subscribe(mUserInfoServerApi.addPhoto(body), object : ApiObserver<ApiResult>() {
                            override fun process(obj: ApiResult) {
                                mUploadingPhoto = false
                                mPlayControlTemplate.endCurrent(photo)
                                if (obj.errno == 0) {
                                    val jsonArray = obj.data!!.getJSONArray("pic")
                                    if (jsonArray.size > 0) {
                                        val jo = jsonArray.getJSONObject(0)
                                        val picID = jo.getIntValue("picID")
                                        val url = jo.getString("picPath")
                                        photo.picID = picID
                                        photo.picPath = url
                                        photo.status = PhotoModel.STATUS_SUCCESS
                                        mView!!.updatePhoto(photo)
                                        // 删除数据中的
                                        PhotoDataManager.delete(photo)
                                        return
                                    }
                                }
                                if (obj.errno == 8302160) {
                                    photo.status = PhotoModel.STATUS_FAILED_SEXY
                                } else if (obj.errno == 8302161) {
                                    mExceedLimit = true
                                    photo.status = PhotoModel.STATUS_FAILED_LIMIT
                                } else {
                                    photo.status = PhotoModel.STATUS_FAILED
                                }
                                mView!!.updatePhoto(photo)
                                U.getToastUtil().showShort(obj.errmsg)
                            }

                            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                                super.onNetworkError(errorType)
                                mUploadingPhoto = false
                                mPlayControlTemplate.endCurrent(photo)
                            }
                        })
                    }

                    override fun onFailureNotInUiThread(msg: String) {
                        MyLog.d(TAG, "上传失败 msg=$msg")
                        mUploadingPhoto = false
                        mPlayControlTemplate.endCurrent(photo)
                        mUiHandler!!.post {
                            photo.status = PhotoModel.STATUS_FAILED
                            mView!!.updatePhoto(photo)
                        }
                    }
                })
    }

    fun deletePhoto(photoModel: PhotoModel) {
        MyLog.d(TAG, "deletePhoto photoModel=$photoModel")
        if (photoModel.status == PhotoModel.STATUS_SUCCESS) {
            val map = HashMap<String, Any>()
            map["picID"] = photoModel.picID
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            ApiMethods.subscribe(mUserInfoServerApi.deletePhoto(body), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult) {
                    if (obj.errno == 0) {
                        if (mView != null) {
                            photoModel.status = PhotoModel.STATUS_DELETE
                            mView!!.deletePhoto(photoModel, true)
                        }
                        mExceedLimit = false
                    } else {
                        U.getToastUtil().showShort(obj.errmsg)
                    }
                }
            })
        } else {
            photoModel.status = PhotoModel.STATUS_DELETE
            PhotoDataManager.delete(photoModel)
            // 还没上传成功，本地删除就好，// 上传队列还得删除
            if (mView != null) {
                mView!!.deletePhoto(photoModel, false)
            }
        }
    }

    fun loadUnSuccessPhotoFromDB() {
        PhotoDataManager.getAllPhotoFromDB { r, list ->
            for (photoModel in list) {
                MyLog.d(TAG, "loadUnSuccessPhotoFromDB photoModel=$photoModel")
                mView!!.insertPhoto(photoModel)
                mPlayControlTemplate.add(photoModel, true)
            }
        }
    }

    fun destroy() {
        if (mUiHandler != null) {
            mUiHandler!!.removeCallbacksAndMessages(null)
        }
    }
}


