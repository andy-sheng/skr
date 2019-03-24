package com.module.playways.grab.room.songmanager;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.inter.IGrabSongManageView;
import com.module.playways.grab.createroom.model.SpecialModel;
import com.module.playways.rank.song.model.SongModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabSongManagePresenter extends RxLifeCyclePresenter {
    public final static String TAG = "GrabSongManagePresenter";

    IGrabSongManageView mIGrabSongManageView;

    GrabRoomServerApi mGrabRoomServerApi;

    GrabRoomData mGrabRoomData;

    Disposable mGetTagsTask;

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

        if (mGetTagsTask != null && !mGetTagsTask.isDisposed()) {
            MyLog.w(TAG, "已经加载中了...");
            return;
        }

        mGetTagsTask = ApiMethods.subscribeWith(mGrabRoomServerApi.getSepcialList(0, 20), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mSpecialModelList = JSON.parseArray(obj.getData().getString("tags"), SpecialModel.class);
                    if (mSpecialModelList != null && mSpecialModelList.size() > 0) {

                        mIGrabSongManageView.showTagList(mSpecialModelList);
                    }
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
                    MyLog.w(TAG, "getFriendList failed, " + result.getErrmsg() + ", traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                MyLog.e(TAG, "getFriendList 网络延迟");
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    public void deleteSong(int playbookItemId, int roundReq) {
        MyLog.d(TAG, "deleteSong");
        if (roundReq < 0) {
            MyLog.d(TAG, "deleteSong but roundReq is " + roundReq);
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemID", playbookItemId);
        map.put("roomID", mGrabRoomData.getGameId());
        map.put("roundReq", roundReq);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabRoundChangeEvent event) {
        updateSongList();
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

    public void addSong(SongModel songModel) {
        MyLog.d(TAG, "addSong");
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemID", songModel.getItemID());
        map.put("roomID", mGrabRoomData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.addMusic(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addSong process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    if (mGrabRoomSongModelList != null && mGrabRoomSongModelList.size() > 0) {
                        if (mGrabRoomSongModelList.size() > 2) {
                            for (int i = 2; i < mGrabRoomSongModelList.size(); i++) {
                                GrabRoomSongModel grabRoomSongModel = mGrabRoomSongModelList.get(i);
                                grabRoomSongModel.setRoundSeq(grabRoomSongModel.getRoundSeq() + 1);
                            }
                        }

                        //加一个保护
                        GrabRoomSongModel grabRoomSongModel = new GrabRoomSongModel();
                        grabRoomSongModel.setOwner(songModel.getOwner());
                        grabRoomSongModel.setItemName(songModel.getItemName());
                        grabRoomSongModel.setPlaybookItemID(songModel.getItemID());

                        if (mGrabRoomSongModelList.size() <= 2) {
                            grabRoomSongModel.setRoundSeq(mGrabRoomSongModelList.get(mGrabRoomSongModelList.size() - 1).getRoundSeq() + 1);
                            mGrabRoomSongModelList.add(grabRoomSongModel);
                        } else {
                            grabRoomSongModel.setRoundSeq(mGrabRoomSongModelList.get(1).getRoundSeq() + 1);
                            mGrabRoomSongModelList.add(2, grabRoomSongModel);
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

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.changeMusicTag(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addSong process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    mIGrabSongManageView.changeTagSuccess(specialModel);
                    List<GrabRoomSongModel> grabRoomSongModels = JSON.parseArray(result.getData().getString("playbook"), GrabRoomSongModel.class);
                    if (grabRoomSongModels == null || grabRoomSongModels.size() == 0) {
                        //没有更多了
                        mIGrabSongManageView.hasMoreSongList(false);
                        return;
                    }

                    mIGrabSongManageView.hasMoreSongList(true);
                    mGrabRoomSongModelList.clear();
                    mGrabRoomSongModelList.addAll(grabRoomSongModels);
                    mOffset = mGrabRoomSongModelList.size();
                    mIGrabSongManageView.updateSongList(mGrabRoomSongModelList);
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
