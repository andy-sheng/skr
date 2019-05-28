package com.zq.person.presenter;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.anim.ObjectPlayControlTemplate;
import com.common.base.BaseFragment;
import com.common.callback.Callback;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.U;
import com.respicker.model.ImageItem;
import com.zq.person.model.PhotoModel;
import com.zq.person.photo.PhotoDataManager;
import com.zq.person.view.IPhotoWallView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class PhotoCorePresenter {

    public final static String TAG = "PhotoCorePresenter";

    UserInfoServerApi mUserInfoServerApi;
    IPhotoWallView mView;
    BaseFragment mFragment;

    Handler mUiHandler = new Handler();

    boolean mUploadingPhoto = false;
    boolean mExceedLimit = false;

    ObjectPlayControlTemplate<PhotoModel, PhotoCorePresenter> mPlayControlTemplate = new ObjectPlayControlTemplate<PhotoModel, PhotoCorePresenter>() {
        @Override
        protected PhotoCorePresenter accept(PhotoModel cur) {
            MyLog.d(TAG, "accept" + " cur=" + cur + " mUploadingPhoto=" + mUploadingPhoto);
            if (mUploadingPhoto) {
                return null;
            } else {
                mUploadingPhoto = true;
                return PhotoCorePresenter.this;
            }
        }

        @Override
        public void onStart(PhotoModel pm, PhotoCorePresenter personFragment2) {
            MyLog.d(TAG, "onStart" + "开始上传 PhotoModel=" + pm + " 队列还有 mPlayControlTemplate.getSize()=" + mPlayControlTemplate.getSize());
            execUploadPhoto(pm);
        }

        @Override
        protected void onEnd(PhotoModel pm) {
            MyLog.d(TAG, "onEnd" + " 上传结束 PhotoModel=" + pm);
        }
    };

    public PhotoCorePresenter(IPhotoWallView view, BaseFragment fragment) {
        this.mView = view;
        this.mFragment = fragment;
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    public void getPhotos(int offset, int cnt) {
        getPhotos(offset, cnt, null);
    }

    public void getPhotos(final int offset, int cnt, final Callback<List<PhotoModel>> callback) {
        MyLog.d(TAG, "getPhotos" + " offset=" + offset + " cnt=" + cnt + " callback=" + callback);
        ApiMethods.subscribe(mUserInfoServerApi.getPhotos((int) MyUserInfoManager.getInstance().getUid(), offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    if (result != null && result.getErrno() == 0) {
                        List<PhotoModel> list = JSON.parseArray(result.getData().getString("pic"), PhotoModel.class);
                        int newOffset = result.getData().getIntValue("offset");
                        int totalCount = result.getData().getIntValue("totalCount");
                        if (offset == 0) {
                            // 刷新拉
                            mView.addPhoto(list, true, totalCount);
                            if (callback != null) {
                                callback.onCallback(1, list);
                            }
                            return;
                        } else {
                            // 下拉更多拉
                            mView.addPhoto(list, false, totalCount);
                            if (callback != null) {
                                callback.onCallback(2, list);
                            }
                            return;
                        }
                    }
                } else {
                    mView.loadDataFailed();
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                mView.loadDataFailed();
            }
        }, mFragment, new ApiMethods.RequestControl("getPhotos", ApiMethods.ControlType.CancelThis));
    }

    public void uploadPhotoList(List<ImageItem> imageItems) {
        MyLog.d(TAG, "uploadPhotoList" + " imageItems=" + imageItems);
        List<PhotoModel> list = new ArrayList<>();
        for (ImageItem imageItem : imageItems) {
            PhotoModel photoModel = new PhotoModel();
            photoModel.setLocalPath(imageItem.getPath());
            photoModel.setStatus(PhotoModel.STATUS_WAIT_UPLOAD);
            list.add(photoModel);
        }
        // 数据库中的zhukey怎么定，数据库中只存未上传成功的
        upload(list, false);
    }

    public void upload(List<PhotoModel> photoModels, boolean reupload) {
        MyLog.d(TAG, "uploadPhotoList" + " photoModels=" + photoModels);
        if (photoModels != null && photoModels.size() > 0) {
            // 数据库中的zhukey怎么定，数据库中只存未上传成功的
            PhotoDataManager.insertOrUpdate(photoModels);
            for (PhotoModel photoModel : photoModels) {
                photoModel.setStatus(PhotoModel.STATUS_WAIT_UPLOAD);
                if (reupload) {
                    mView.updatePhoto(photoModel);
                } else {
                    mView.insertPhoto(photoModel);
                }
                mPlayControlTemplate.add(photoModel, true);
            }
        }
    }

    void execUploadPhoto(final PhotoModel photo) {
        MyLog.d(TAG, "execUploadPhoto" + " photo=" + photo);

        if (photo.getStatus() == photo.STATUS_DELETE) {
            MyLog.d(TAG, "execUploadPhoto" + " imageItem=" + photo + " 用户删除了，取消上传");
            mUploadingPhoto = false;
            mPlayControlTemplate.endCurrent(photo);
            return;
        }
        if (mExceedLimit) {
            photo.setStatus(PhotoModel.STATUS_FAILED_LIMIT);
            mView.updatePhoto(photo);
            mUploadingPhoto = false;
            mPlayControlTemplate.endCurrent(photo);
            return;
        }
        photo.setStatus(PhotoModel.STATUS_UPLOADING);
        mView.updatePhoto(photo);
        UploadTask uploadTask = UploadParams.newBuilder(photo.getLocalPath())
                .setNeedCompress(true)
                .setNeedMonitor(true)
                .setFileType(UploadParams.FileType.profilepic)
                .startUploadAsync(new UploadCallback() {
                    @Override
                    public void onProgress(long currentSize, long totalSize) {

                    }

                    @Override
                    public void onSuccess(String url) {
                        MyLog.d(TAG, "上传成功" + " url=" + url);
                        // 上传到服务器
                        HashMap<String, Object> map = new HashMap<>();

                        List<JSONObject> pics = new ArrayList<>();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("picPath", url);
                        pics.add(jsonObject);
                        map.put("pic", pics);
                        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

                        ApiMethods.subscribe(mUserInfoServerApi.addPhoto(body), new ApiObserver<ApiResult>() {
                            @Override
                            public void process(ApiResult obj) {
                                mUploadingPhoto = false;
                                mPlayControlTemplate.endCurrent(photo);
                                if (obj.getErrno() == 0) {
                                    JSONArray jsonArray = obj.getData().getJSONArray("pic");
                                    if (jsonArray.size() > 0) {
                                        JSONObject jo = jsonArray.getJSONObject(0);
                                        int picID = jo.getIntValue("picID");
                                        String url = jo.getString("picPath");
                                        photo.setPicID(picID);
                                        photo.setPicPath(url);
                                        photo.setStatus(PhotoModel.STATUS_SUCCESS);
                                        mView.updatePhoto(photo);
                                        // 删除数据中的
                                        PhotoDataManager.delete(photo);
                                        return;
                                    }
                                }
                                if (obj.getErrno() == 8302160) {
                                    photo.setStatus(PhotoModel.STATUS_FAILED_SEXY);
                                } else if (obj.getErrno() == 8302161) {
                                    mExceedLimit = true;
                                    photo.setStatus(PhotoModel.STATUS_FAILED_LIMIT);
                                } else {
                                    photo.setStatus(PhotoModel.STATUS_FAILED);
                                }
                                mView.updatePhoto(photo);
                                U.getToastUtil().showShort(obj.getErrmsg());
                            }

                            @Override
                            public void onNetworkError(ErrorType errorType) {
                                super.onNetworkError(errorType);
                                mUploadingPhoto = false;
                                mPlayControlTemplate.endCurrent(photo);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String msg) {
                        MyLog.d(TAG, "上传失败" + " msg=" + msg);
                        mUploadingPhoto = false;
                        mPlayControlTemplate.endCurrent(photo);
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                photo.setStatus(PhotoModel.STATUS_FAILED);
                                mView.updatePhoto(photo);
                            }
                        });
                    }
                });
    }

    public void deletePhoto(final PhotoModel photoModel) {
        MyLog.d(TAG, "deletePhoto" + " photoModel=" + photoModel);
        if (photoModel.getStatus() == PhotoModel.STATUS_SUCCESS) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("picID", photoModel.getPicID());
            RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
            ApiMethods.subscribe(mUserInfoServerApi.deletePhoto(body), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult obj) {
                    if (obj.getErrno() == 0) {
                        if (mView != null) {
                            photoModel.setStatus(PhotoModel.STATUS_DELETE);
                            mView.deletePhoto(photoModel, true);
                        }
                        mExceedLimit = false;
                    } else {
                        U.getToastUtil().showShort(obj.getErrmsg());
                    }
                }
            });
        } else {
            photoModel.setStatus(PhotoModel.STATUS_DELETE);
            PhotoDataManager.delete(photoModel);
            // 还没上传成功，本地删除就好，// 上传队列还得删除
            if (mView != null) {
                mView.deletePhoto(photoModel, false);
            }
        }
    }

    public void loadUnSuccessPhotoFromDB() {
        PhotoDataManager.getAllPhotoFromDB(new Callback<List<PhotoModel>>() {
            @Override
            public void onCallback(int r, List<PhotoModel> list) {
                for (PhotoModel photoModel : list) {
                    MyLog.d(TAG, "loadUnSuccessPhotoFromDB photoModel=" + photoModel);
                    mView.insertPhoto(photoModel);
                    mPlayControlTemplate.add(photoModel, true);
                }
            }
        });
    }

    public void destroy() {
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }
}


