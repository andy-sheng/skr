package com.module.playways.grab.room.songmanager.presenter;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.component.busilib.friends.SpecialModel;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.inter.IGrabSongManageView;
import com.module.playways.grab.room.songmanager.event.AddSongEvent;
import com.module.playways.grab.room.songmanager.event.AddSuggestSongEvent;
import com.module.playways.grab.room.songmanager.model.GrabRoomSongModel;
import com.module.playways.grab.room.songmanager.model.GrabWishSongModel;
import com.module.playways.room.song.model.SongModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 房主可以看到的 所有轮次 的歌曲 view
 */
public class GrabSongManagePresenter extends RxLifeCyclePresenter {
    public final static String TAG = "GrabSongManagePresenter";

    IGrabSongManageView mIGrabSongManageView;

    GrabRoomServerApi mGrabRoomServerApi;

    GrabRoomData mGrabRoomData;

    Disposable mGetTagsTask;

    Disposable mGetSongModelListTask;

    List<SpecialModel> mSpecialModelList;

    List<GrabRoomSongModel> mGrabRoomSongModelList = new ArrayList<>();

    Handler mUiHandler;

    boolean mHasMore = false;

    int mTotalNum = 0;

    int mLimit = 20;

    public GrabSongManagePresenter(IGrabSongManageView view, GrabRoomData grabRoomData) {
        this.mIGrabSongManageView = view;
        mGrabRoomData = grabRoomData;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        mUiHandler = new Handler();
        addToLifeCycle();
        EventBus.getDefault().register(this);
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

        mGetTagsTask = ApiMethods.subscribe(mGrabRoomServerApi.getSepcialList(0, 20), new ApiObserver<ApiResult>() {
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

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    public void getPlayBookList() {
        if (mGetSongModelListTask != null) {
            mGetSongModelListTask.dispose();
        }

        int offset;

        if (!mGrabRoomData.hasGameBegin()) {
            offset = 0;
        } else {
            offset = mGrabRoomData.getRealRoundSeq() - 1;
        }

        if (mGrabRoomSongModelList != null && mGrabRoomSongModelList.size() > 0) {
            offset = mGrabRoomSongModelList.get(mGrabRoomSongModelList.size() - 1).getRoundSeq();
        }

        MyLog.d(TAG, "getPlayBookList offset is " + offset);

        mGetSongModelListTask = ApiMethods.subscribe(mGrabRoomServerApi.getPlaybook(mGrabRoomData.getGameId(), offset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<GrabRoomSongModel> grabRoomSongModels = JSON.parseArray(result.getData().getString("playbook"), GrabRoomSongModel.class);
                    if (grabRoomSongModels == null || grabRoomSongModels.size() == 0) {
                        //没有更多了
                        mIGrabSongManageView.hasMoreSongList(false);
                        mHasMore = false;
                        MyLog.d(TAG, "process grabRoomSongModels size is 0");
                        return;
                    }

                    MyLog.d(TAG, "process grabRoomSongModels size is is " + grabRoomSongModels.size());

                    mIGrabSongManageView.hasMoreSongList(true);
                    mHasMore = true;
                    mGrabRoomSongModelList.addAll(grabRoomSongModels);
                    updateSongList();

                    int total = result.getData().getIntValue("total");
                    mTotalNum = total;
                    mIGrabSongManageView.showNum(total);
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

    public void deleteSong(GrabRoomSongModel grabRoomSongModel) {
        MyLog.d(TAG, "deleteSong");
        int playbookItemId = grabRoomSongModel.getItemID();
        int roundSeq = grabRoomSongModel.getRoundSeq();

        if (roundSeq < 0) {
            MyLog.d(TAG, "deleteSong but roundReq is " + roundSeq);
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("itemID", playbookItemId);
        map.put("roomID", mGrabRoomData.getGameId());
        map.put("roundSeq", grabRoomSongModel.getRoundSeq());

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
                            if (grabRoomSongModel.getRoundSeq() == roundSeq) {
                                iterator.remove();
                            } else if (grabRoomSongModel.getRoundSeq() > roundSeq) {
                                grabRoomSongModel.setRoundSeq(grabRoomSongModel.getRoundSeq() - 1);
                            }
                        }

                        mIGrabSongManageView.showNum(--mTotalNum);
                        mIGrabSongManageView.deleteSong(grabRoomSongModel);

                        mUiHandler.removeCallbacksAndMessages(null);
                        mUiHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mGrabRoomSongModelList.size() < 10 && mHasMore) {
                                    getPlayBookList();
                                }
                            }
                        }, 300);
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

        if (mGrabRoomSongModelList.size() < 5 && mHasMore) {
            getPlayBookList();
        }
    }

    // 添加新歌
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
                        grabRoomSongModel.setItemID(songModel.getItemID());
                        grabRoomSongModel.setPlayType(songModel.getPlayType());
                        grabRoomSongModel.setChallengeAvailable(songModel.isChallengeAvailable());

                        if (mGrabRoomSongModelList.size() <= 2) {
                            grabRoomSongModel.setRoundSeq(mGrabRoomSongModelList.get(mGrabRoomSongModelList.size() - 1).getRoundSeq() + 1);
                            mGrabRoomSongModelList.add(grabRoomSongModel);
                        } else {
                            grabRoomSongModel.setRoundSeq(mGrabRoomSongModelList.get(1).getRoundSeq() + 1);
                            mGrabRoomSongModelList.add(2, grabRoomSongModel);
                        }

                        mIGrabSongManageView.showNum(++mTotalNum);
                        updateSongList();
                    }

                    U.getToastUtil().showShort(songModel.getItemName() + " 添加成功");
                } else {
                    MyLog.w(TAG, "addSong failed, " + " traceid is " + result.getTraceId());
                    U.getToastUtil().showShort(result.getErrmsg());
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
                MyLog.d(TAG, "changeMusicTag process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    mIGrabSongManageView.changeTagSuccess(specialModel);
                    List<GrabRoomSongModel> grabRoomSongModels = JSON.parseArray(result.getData().getString("playbook"), GrabRoomSongModel.class);
                    if (grabRoomSongModels == null || grabRoomSongModels.size() == 0) {
                        //没有更多了
                        mIGrabSongManageView.hasMoreSongList(false);
                        return;
                    }

                    int total = result.getData().getIntValue("total");
                    mTotalNum = total;
                    mIGrabSongManageView.showNum(mTotalNum);

                    mIGrabSongManageView.hasMoreSongList(true);
                    mGrabRoomSongModelList.clear();
                    mGrabRoomSongModelList.addAll(grabRoomSongModels);
                    updateSongList();
                } else {
                    MyLog.w(TAG, "changeMusicTag failed, " + " traceid is " + result.getTraceId());
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
        if (event.newRoundInfo != null && event.newRoundInfo.getRoundSeq() != 1) {
            mIGrabSongManageView.showNum(--mTotalNum);
        }
        updateSongList();
    }

    /**
     * 增加歌曲
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AddSongEvent event) {
        if (mGrabRoomData.isOwner()) {
            addSong(event.getSongModel());
        }
    }

    /**
     * 房主处理愿望清单
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AddSuggestSongEvent event) {
        // 添加非房主想唱的歌曲
        GrabRoomSongModel grabRoomSongModel = new GrabRoomSongModel();
        GrabWishSongModel grabWishSongModel = event.getGrabWishSongModel();
        grabRoomSongModel.setOwner(grabWishSongModel.getOwner());
        grabRoomSongModel.setItemName(grabWishSongModel.getItemName());
        grabRoomSongModel.setItemID(grabWishSongModel.getItemID());
        grabRoomSongModel.setPlayType(grabWishSongModel.getPlayType());
        grabRoomSongModel.setChallengeAvailable(grabWishSongModel.isChallengeAvailable());

        if (mGrabRoomSongModelList.size() <= 2) {
            grabRoomSongModel.setRoundSeq(mGrabRoomSongModelList.get(mGrabRoomSongModelList.size() - 1).getRoundSeq() + 1);
            mGrabRoomSongModelList.add(grabRoomSongModel);
        } else {
            grabRoomSongModel.setRoundSeq(mGrabRoomSongModelList.get(1).getRoundSeq() + 1);
            mGrabRoomSongModelList.add(2, grabRoomSongModel);
        }

        mIGrabSongManageView.showNum(++mTotalNum);
        updateSongList();
    }

    @Override
    public void destroy() {
        super.destroy();
        mUiHandler.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
    }
}
