package com.module.playways.grab.room.songmanager.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.inter.IGrabWishManageView;
import com.module.playways.grab.room.songmanager.event.AddSuggestSongEvent;
import com.module.playways.grab.room.songmanager.model.GrabWishSongModel;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;

import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 愿望清单这个view 的 presenter
 */
public class GrabWishSongPresenter extends RxLifeCyclePresenter {

    public final static String TAG = "GrabWishSongPresenter";

    GrabRoomData mGrabRoomData;
    IGrabWishManageView mView;
    GrabRoomServerApi mGrabRoomServerApi;

    Disposable mGetSuggestListTask;

    int mLimit = 20;

    public GrabWishSongPresenter(IGrabWishManageView view, GrabRoomData grabRoomData) {
        this.mGrabRoomData = grabRoomData;
        this.mView = view;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        addToLifeCycle();
    }

    public void getListMusicSuggested(long offset) {
        if (mGetSuggestListTask != null && !mGetSuggestListTask.isDisposed()) {
            MyLog.w(TAG, "已经加载中了...");
            return;
        }
        mGetSuggestListTask = ApiMethods.subscribe(mGrabRoomServerApi.getListMusicSuggested(mGrabRoomData.getGameId(), offset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<GrabWishSongModel> grabWishSongModels = JSONObject.parseArray(result.getData().getString("items"), GrabWishSongModel.class);
                    long newOffset = result.getData().getLongValue("offset");
                    if (offset == 0) {
                        mView.addGrabWishSongModels(true, newOffset, grabWishSongModels);
                    } else {
                        mView.addGrabWishSongModels(false, newOffset, grabWishSongModels);
                    }
                } else {
                    U.getToastUtil().showShort(result.getErrmsg() + "");
                }

            }
        }, this);
    }

    public void addWishSong(GrabWishSongModel songModel) {
        MyLog.d(TAG, "addWishSong" + " songModel=" + songModel);
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemID", songModel.getItemID());
        map.put("roomID", mGrabRoomData.getGameId());
        map.put("userID", songModel.getSuggester().getUserId());
        map.put("pID", songModel.getpID());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.addSuggestMusic(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addWishSong" + " result=" + result);
                if (result.getErrno() == 0) {
                    // 通知本页面，删除该model
                    mView.deleteWishSong(songModel);
                    // 通知GrabSongManagePresenter 接受新的数据
                    EventBus.getDefault().post(new AddSuggestSongEvent(songModel));
                    U.getToastUtil().showShort("添加成功");
                } else {
                    MyLog.w(TAG, "addWishSong failed, " + " traceid is " + result.getTraceId());
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    public void deleteWishSong(GrabWishSongModel songModel) {
        MyLog.d(TAG, "deleteWishSong" + " songModel=" + songModel);
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemID", songModel.getItemID());
        map.put("roomID", mGrabRoomData.getGameId());
        map.put("userID", songModel.getSuggester().getUserId());
        map.put("pID", songModel.getpID());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.deleteSuggestMusic(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addWishSong" + " result=" + result);
                if (result.getErrno() == 0) {
                    // 通知本页面，删除该model
                    mView.deleteWishSong(songModel);
                    U.getToastUtil().showShort("删除成功");
                } else {
                    MyLog.w(TAG, "addWishSong failed, " + " traceid is " + result.getTraceId());
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }
}
