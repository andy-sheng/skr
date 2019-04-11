package com.module.home.persenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.anim.ObjectPlayControlTemplate;
import com.common.callback.Callback;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoLocalApi;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import model.RelationNumModel;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.upload.UploadTask;
import com.common.utils.U;
import com.module.home.view.IPersonView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.common.core.userinfo.model.UserLevelModel;
import com.respicker.model.ImageItem;
import com.zq.person.model.PhotoModel;
import com.zq.person.photo.PhotoDataManager;
import com.zq.person.photo.PhotoLocalApi;

public class PersonCorePresenter extends RxLifeCyclePresenter {

    UserInfoServerApi mUserInfoServerApi;
    IPersonView mView;

    long mLastUpdateTime = 0;  // 主页刷新时间
    //    long mLastPhotoUpTime = 0; // 照片墙更新时间
    boolean mUploadingPhoto = false;

    ObjectPlayControlTemplate<PhotoModel, PersonCorePresenter> mPlayControlTemplate = new ObjectPlayControlTemplate<PhotoModel, PersonCorePresenter>() {
        @Override
        protected PersonCorePresenter accept(PhotoModel cur) {
            if (mUploadingPhoto) {
                return null;
            } else {
                mUploadingPhoto = true;
                return PersonCorePresenter.this;
            }
        }

        @Override
        public void onStart(PhotoModel imageItem, PersonCorePresenter personFragment2) {
            U.getToastUtil().showShort("开始上传，队列还剩" + mPlayControlTemplate.getSize());
            execUploadPhoto(imageItem);
        }

        @Override
        protected void onEnd(PhotoModel imageItem) {

        }
    };

    public PersonCorePresenter(IPersonView view) {
        this.mView = view;
        mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
    }

    /**
     * @param flag 是否立即更新
     */
    public void getHomePage(boolean flag) {
        long now = System.currentTimeMillis();
        if (!flag) {
            if ((now - mLastUpdateTime) < 60 * 1000) {
                return;
            }
        }

        getHomePage((int) MyUserInfoManager.getInstance().getUid());
    }

    private void getHomePage(int userID) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mLastUpdateTime = System.currentTimeMillis();
                    UserInfoModel userInfoModel = JSON.parseObject(result.getData().getString("userBaseInfo"), UserInfoModel.class);
                    List<UserRankModel> userRankModels = JSON.parseArray(result.getData().getJSONObject("userRankInfo").getString("seqInfo"), UserRankModel.class);
                    List<RelationNumModel> relationNumModes = JSON.parseArray(result.getData().getJSONObject("userRelationCntInfo").getString("cnt"), RelationNumModel.class);
                    List<UserLevelModel> userLevelModels = JSON.parseArray(result.getData().getJSONObject("userScoreInfo").getString("userScore"), UserLevelModel.class);
                    List<GameStatisModel> userGameStatisModels = JSON.parseArray(result.getData().getJSONObject("userGameStatisticsInfo").getString("statistic"), GameStatisModel.class);
//                    boolean isFriend = result.getData().getJSONObject("userMateInfo").getBoolean("isFriend");
//                    boolean isFollow = result.getData().getJSONObject("userMateInfo").getBoolean("isFollow");

                    MyUserInfo myUserInfo = MyUserInfo.parseFromUserInfoModel(userInfoModel);
                    MyUserInfoLocalApi.insertOrUpdate(myUserInfo);
                    MyUserInfoManager.getInstance().setMyUserInfo(myUserInfo, true);

                    mView.showHomePageInfo(relationNumModes, userRankModels, userLevelModels, userGameStatisModels);
                }
            }
        }, this);
    }

//    public void getPhotos(int offset, int cnt, boolean flag) {
//        long now = System.currentTimeMillis();
//        if (!flag) {
//            if ((now - mLastPhotoUpTime) < 60 * 1000) {
//                return;
//            }
//        }
//        getPhotos(offset, cnt);
//    }

    public void getPhotos(int offset, int cnt) {
        getPhotos(offset, cnt, null);
    }

    public void getPhotos(int offset, int cnt, Callback<List<PhotoModel>> callback) {
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
                        } else {
                            // 下拉更多拉
                            mView.addPhoto(list, false, totalCount);
                            if (callback != null) {
                                callback.onCallback(2, list);
                            }
                        }

                    }
                }
            }
        }, this, new ApiMethods.RequestControl("getPhotos", ApiMethods.ControlType.CancelThis));
    }

    public void getRelationNums() {
        ApiMethods.subscribe(mUserInfoServerApi.getRelationNum((int) MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RelationNumModel> relationNumModels = JSON.parseArray(result.getData().getString("cnt"), RelationNumModel.class);
                    mView.showRelationNum(relationNumModels);
                }
            }
        }, this);

    }

    public void uploadPhotoList(List<ImageItem> imageItems) {
        List<PhotoModel> list = new ArrayList<>();

        for (ImageItem imageItem : imageItems) {
            PhotoModel photoModel = new PhotoModel();
            photoModel.setLocalPath(imageItem.getPath());
            photoModel.setStatus(PhotoModel.STATUS_WAIT_UPLOAD);
            list.add(photoModel);
        }
        // 数据库中的zhukey怎么定，数据库中只存未上传成功的
        PhotoDataManager.insertOrUpdate(list);
        for (PhotoModel photoModel : list) {
            mView.insertPhoto(photoModel);
            mPlayControlTemplate.add(photoModel, true);
        }
    }

    void execUploadPhoto(PhotoModel photo) {
        if (photo.getStatus() == photo.STATUS_DELETE) {
            MyLog.d(TAG, "execUploadPhoto" + " imageItem=" + photo + " 用户删除了，取消上传");
            mPlayControlTemplate.endCurrent(photo);
            return;
        }
        photo.setStatus(PhotoModel.STATUS_UPLOADING);
        mView.updatePhoto(photo);
        UploadTask uploadTask = UploadParams.newBuilder(photo.getLocalPath())
                .setNeedCompress(true)
                .setFileType(UploadParams.FileType.profilepic)
                .startUploadAsync(new UploadCallback() {
                    @Override
                    public void onProgress(long currentSize, long totalSize) {

                    }

                    @Override
                    public void onSuccess(String url) {
                        mUploadingPhoto = false;
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
                                if (obj.getErrno() == 0) {
                                    JSONArray jsonArray = obj.getData().getJSONArray("pic");
                                    if (jsonArray.size() > 0) {
                                        JSONObject jo = jsonArray.getJSONObject(0);
                                        int picID = jo.getInteger("picID");
                                        String url = jo.getString("picPath");
                                        photo.setPicID(picID);
                                        photo.setPicPath(url);
                                        photo.setStatus(PhotoModel.STATUS_SUCCESS);
                                        mView.updatePhoto(photo);
                                        // 删除数据中的
                                        PhotoDataManager.delete(photo);
                                    }
                                } else {
                                    photo.setStatus(PhotoModel.STATUS_FAILED);
                                    mView.updatePhoto(photo);
                                    U.getToastUtil().showShort(obj.getErrmsg());
                                }
                            }
                        });
                        mPlayControlTemplate.endCurrent(photo);
                    }

                    @Override
                    public void onFailure(String msg) {
                        mUploadingPhoto = false;
                        photo.setStatus(PhotoModel.STATUS_FAILED);
                        mView.updatePhoto(photo);
                        mPlayControlTemplate.endCurrent(photo);
                    }
                });
    }

    public void deletePhoto(PhotoModel photoModel) {
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
                            mView.deletePhoto(photoModel);
                        }
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
                mView.deletePhoto(photoModel);
            }
        }
    }

    public void loadUnSuccessPhotoFromDB() {
        PhotoDataManager.getAllPhotoFromDB(new Callback<List<PhotoModel>>() {
            @Override
            public void onCallback(int r, List<PhotoModel> list) {
                for (PhotoModel photoModel : list) {
                    mView.insertPhoto(photoModel);
                    mPlayControlTemplate.add(photoModel, true);
                }
            }
        });
    }
}
