package com.module.playways.room.song.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.room.song.SongSelectServerApi;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.room.song.view.ISongTagDetailView;
import com.module.playways.songmanager.SongManagerActivity;

import java.util.List;

public class SongTagDetailsPresenter extends RxLifeCyclePresenter {

    ISongTagDetailView view;

    public SongTagDetailsPresenter(ISongTagDetailView view) {
        this.view = view;
    }

    /**
     * 获取推荐的歌曲列表
     *
     * @param offset
     * @param cnt
     */
    public void getRcomdMusicItems(int offset, int cnt, int from) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        if (from == SongManagerActivity.TYPE_FROM_AUDITION) {
            ApiMethods.subscribe(songSelectServerApi.getRcomdMusicItems(offset, cnt), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                        if (list != null && list.size() > 0) {
                            int offset = result.getData().getIntValue("offset");
                            view.loadSongsDetailItems(list, offset, true);
                        } else {
                            view.loadSongsDetailItems(null, offset, false);
                        }
                    } else {
                        view.loadSongsDetailItemsFail();
                    }
                }
            }, this);
        } else {
            ApiMethods.subscribe(songSelectServerApi.getRelayMusicItems(offset, cnt, MyUserInfoManager.INSTANCE.getUid()), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                        int offset = result.getData().getIntValue("offset");
                        boolean hasMore = result.getData().getBooleanValue("hasMore");
                        view.loadSongsDetailItems(list, offset, hasMore);
                    } else {
                        view.loadSongsDetailItemsFail();
                    }
                }
            }, this);
        }

    }

    /**
     * 获取已点的歌曲列表
     *
     * @param offset
     * @param cnt
     */
    public void getClickedMusicItmes(int offset, int cnt, int from) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        if (from == SongManagerActivity.TYPE_FROM_AUDITION) {
            ApiMethods.subscribe(songSelectServerApi.getClickedMusicItmes(offset, cnt), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                        if (list != null && list.size() > 0) {
                            int offset = result.getData().getIntValue("offset");
                            view.loadSongsDetailItems(list, offset, true);
                        } else {
                            view.loadSongsDetailItems(null, offset, false);
                        }
                    } else {
                        view.loadSongsDetailItemsFail();
                    }
                }
            }, this);
        } else {
            ApiMethods.subscribe(songSelectServerApi.getRelayClickedMusicItmes(offset, cnt, (int) MyUserInfoManager.INSTANCE.getUid()), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                        int offset = result.getData().getIntValue("offset");
                        boolean hasMore = result.getData().getBooleanValue("hasMore");
                        view.loadSongsDetailItems(list, offset, hasMore);
                    } else {
                        view.loadSongsDetailItemsFail();
                    }
                }
            }, this);
        }

    }

}
