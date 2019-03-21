package com.module.playways.grab.room.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.inter.IGrabSongManageView;
import com.module.playways.grab.room.model.GrabRoomSongModel;
import com.module.playways.grab.songselect.GrabSongApi;
import com.module.playways.grab.songselect.model.SpecialModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabSongManagePresenter extends RxLifeCyclePresenter {
    public final static String TAG = "GrabSongManagePresenter";
    IGrabSongManageView mIGrabSongManageView;

    GrabRoomServerApi mGrabRoomServerApi;

    GrabRoomData mGrabRoomData;

    List<SpecialModel> mSpecialModelList;

    List<GrabRoomSongModel> mGrabRoomSongModelList = new ArrayList<>();

    int mOffset = 0;
    int mLimit = 20;

    public GrabSongManagePresenter(IGrabSongManageView view, GrabRoomData grabRoomData) {
        this.mIGrabSongManageView = view;
        mGrabRoomData = grabRoomData;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
    }

    public void getTagList() {
        if (mSpecialModelList != null && mSpecialModelList.size() > 0) {
            mIGrabSongManageView.showTagList(mSpecialModelList);
            return;
        }

        ApiMethods.subscribe(mGrabRoomServerApi.getSepcialList(0, 20), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mSpecialModelList = JSON.parseArray(obj.getData().getString("tags"), SpecialModel.class);
                    mIGrabSongManageView.showTagList(mSpecialModelList);
                } else {
                    MyLog.d(TAG, "getTagList failed, " + "result is " + obj.toString());
                }
            }
        }, this);
    }

    public void getPlayBookList() {
        ApiMethods.subscribe(mGrabRoomServerApi.getPlaybook(mGrabRoomData.getGameId(), mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<GrabRoomSongModel> grabRoomSongModels = JSON.parseArray(result.getData().getString("playbook"), GrabRoomSongModel.class);
                    if (grabRoomSongModels == null || grabRoomSongModels.size() == 0) {
                        //没有更多了
                        mIGrabSongManageView.hasMoreSongList(false);
                        return;
                    }

                    mIGrabSongManageView.hasMoreSongList(true);
                    mGrabRoomSongModelList.addAll(grabRoomSongModels);
                    mOffset = mGrabRoomSongModelList.size();
                    mIGrabSongManageView.updateSongList(mGrabRoomSongModelList);
                } else {
                    MyLog.w(TAG, "getPlayBookList failed, " + result.getErrmsg() + ", traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {

            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    public void deleteSong(int playbookItemId, int roundReq) {
        MyLog.d(TAG, "deleteSong");
        HashMap<String, Object> map = new HashMap<>();
        map.put("playbookItemID", playbookItemId);
        map.put("roundReq", roundReq);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.delMusic(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    if (mGrabRoomSongModelList != null) {
                        Iterator<GrabRoomSongModel> iterator = mGrabRoomSongModelList.iterator();
                        while (iterator.hasNext()) {
                            GrabRoomSongModel grabRoomSongModel = iterator.next();
                            if (grabRoomSongModel.getRoundSeq() == roundReq) {
                                iterator.remove();
                            } else if (grabRoomSongModel.getRoundSeq() > roundReq) {
                                grabRoomSongModel.setRoundSeq(grabRoomSongModel.getRoundSeq() - 1);
                            }
                        }

                        updateSongList();
                    }
                } else {
                    MyLog.w(TAG, "deleteSong failed, " + " traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    public void updateSongList() {
        Iterator<GrabRoomSongModel> iterator = mGrabRoomSongModelList.iterator();
        int seq = mGrabRoomData.getRealRoundSeq();

        while (iterator.hasNext()) {
            GrabRoomSongModel grabRoomSongModel = iterator.next();
            if (grabRoomSongModel.getRoundSeq() < seq) {
                iterator.remove();
            }
        }

        mIGrabSongManageView.updateSongList(mGrabRoomSongModelList);
    }

    public void addSong(int playbookItemId) {
        MyLog.d(TAG, "addSong");
        HashMap<String, Object> map = new HashMap<>();
        map.put("playbookItemID", playbookItemId);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.addMusic(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addSong process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    if (mGrabRoomSongModelList != null) {
                        //加一个保护
                        if (mGrabRoomSongModelList.size() <= 2) {
                            mGrabRoomSongModelList.add(null);
                        } else {
                            mGrabRoomSongModelList.add(2, null);
                        }

                        updateSongList();
                    }
                } else {
                    MyLog.w(TAG, "addSong failed, " + " traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    public void changeMusicTag(SpecialModel specialModel, int roomID) {
        MyLog.d(TAG, "changeMusicTag");
        HashMap<String, Object> map = new HashMap<>();
        map.put("newTagID", specialModel.getTagID());
        map.put("roomID", roomID);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.changeMusicTag(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addSong process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    mIGrabSongManageView.changeTagSuccess(specialModel);
                } else {
                    MyLog.w(TAG, "addSong failed, " + " traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }
}
