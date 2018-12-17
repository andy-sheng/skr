package com.module.rankingmode.song.presenter;

import com.alibaba.fastjson.JSON;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.rankingmode.song.SongSelectServerApi;
import com.module.rankingmode.song.model.SongModel;
import com.module.rankingmode.song.view.ISongTagDetailView;

import java.util.ArrayList;
import java.util.List;

public class SongTagDetailsPresenter extends RxLifeCyclePresenter {

    ISongTagDetailView view;

    public SongTagDetailsPresenter(ISongTagDetailView view) {
        this.view = view;
        addToLifeCycle();
    }

    /**
     * 获取推荐的歌曲列表
     *
     * @param offset
     * @param cnt
     */
    public void getRcomdMusicItems(int offset, int cnt) {
        if (offset == -1) {
            U.getToastUtil().showShort("没有更多数据了");
            return;
        }
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        ApiMethods.subscribe(songSelectServerApi.getRcomdMusicItems(offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                    List<SongModel> data = new ArrayList<>();
                    for (int i = 0; i < 21; i++) {
                        data.addAll(list);
                    }
                    int offset = result.getData().getIntValue("offset");
                    if (view != null) {
                        view.loadSongsDetailItems(data, offset);
                    }
                } else {
                    if (view != null) {
                        view.loadSongsDetailItemsFail();
                    }
                }
            }
        }, this);
    }

    /**
     * 获取已点的歌曲列表
     *
     * @param offset
     * @param cnt
     */
    public void getClickedMusicItmes(int offset, int cnt) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        ApiMethods.subscribe(songSelectServerApi.getClickedMusicItmes(offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<SongModel> list = JSON.parseArray(result.getData().getString("items"), SongModel.class);
                    int offset = result.getData().getIntValue("offset");
                    if (view != null) {
                        view.loadSongsDetailItems(list, offset);
                    }
                } else {
                    if (view != null) {
                        view.loadSongsDetailItemsFail();
                    }
                }
            }
        }, this);
    }

}
