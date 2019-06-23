package com.module.playways.grab.room.songmanager.presenter;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.playways.doubleplay.DoubleRoomServerApi;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.inter.IGrabSongManageView;
import com.module.playways.grab.room.songmanager.SongManageData;
import com.module.playways.grab.room.songmanager.event.AddCustomGameEvent;
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
import java.util.List;

import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 房主可以看到的 所有轮次 的歌曲 view
 */
public class DoubleSongManagePresenter extends BaseSongManagePresenter {
    public final static String TAG = "GrabSongManagePresenter";

    IGrabSongManageView mIGrabSongManageView;

    DoubleRoomServerApi mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi.class);

    SongManageData mGrabRoomData;

    Disposable mGetSongModelListTask;

    List<GrabRoomSongModel> mGrabRoomSongModelList = new ArrayList<>();

    Handler mUiHandler;

    boolean mHasMore = false;

    int mTotalNum = 0;

    int mLimit = 20;

    public DoubleSongManagePresenter(IGrabSongManageView view, SongManageData grabRoomData) {
        this.mIGrabSongManageView = view;
        mGrabRoomData = grabRoomData;
        mUiHandler = new Handler();
        addToLifeCycle();
        EventBus.getDefault().register(this);
    }

    public void getPlayBookList() {
        if (mGetSongModelListTask != null) {
            mGetSongModelListTask.dispose();
        }

        int offset = mGrabRoomSongModelList.size();

        MyLog.d(TAG, "getPlayBookList offset is " + offset);

        mGetSongModelListTask = ApiMethods.subscribe(mDoubleRoomServerApi.getSongList(mGrabRoomData.getGameId(), offset, mLimit), new ApiObserver<ApiResult>() {
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

        ApiMethods.subscribe(mDoubleRoomServerApi.deleteSong(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    if (mGrabRoomSongModelList != null) {
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

        ApiMethods.subscribe(mDoubleRoomServerApi.addSong(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addSong process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    if (mGrabRoomSongModelList != null) {
                        //加一个保护
                        GrabRoomSongModel grabRoomSongModel = new GrabRoomSongModel();
                        grabRoomSongModel.setOwner(songModel.getOwner());
                        grabRoomSongModel.setItemName(songModel.getItemName());
                        grabRoomSongModel.setItemID(songModel.getItemID());
                        grabRoomSongModel.setPlayType(songModel.getPlayType());
                        grabRoomSongModel.setChallengeAvailable(songModel.isChallengeAvailable());
                        addToUiList(grabRoomSongModel);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabRoundChangeEvent event) {

        updateSongList();
    }

    /**
     * 增加歌曲
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AddSongEvent event) {
        // 双人房都可以点歌
        addSong(event.getSongModel());
    }

    /**
     * 添加自定义小游戏成功
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AddCustomGameEvent event) {
        // 添加非房主想唱的歌曲
        GrabRoomSongModel grabRoomSongModel = new GrabRoomSongModel();
        grabRoomSongModel.setOwner(MyUserInfoManager.getInstance().getNickName());
        grabRoomSongModel.setItemName("自定义小游戏");
        grabRoomSongModel.setItemID(SongModel.ID_CUSTOM_GAME);
        grabRoomSongModel.setPlayType(4);
        grabRoomSongModel.setChallengeAvailable(false);
        addToUiList(grabRoomSongModel);
    }

    /**
     * 如果list size >=2 加到 index =2的位置 ，并把之前所有的seq++
     * 否则加到最后
     *
     * @param grabRoomSongModel
     */
    void addToUiList(GrabRoomSongModel grabRoomSongModel) {
        mGrabRoomSongModelList.add(grabRoomSongModel);
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
