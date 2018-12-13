package com.module.rankingmode.song.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.rankingmode.song.SongSelectServerApi;
import com.module.rankingmode.song.model.TagModel;
import com.module.rankingmode.song.view.ISongTagView;

import java.util.List;

@Deprecated // 服务器暂时不支持拉标签
public class SongSelectPresenter extends RxLifeCyclePresenter {

    ISongTagView view;

    public SongSelectPresenter(ISongTagView view) {
        this.view = view;
    }

    /**
     * 获取曲库剧本的标签
     *
     * @param mode   1. 经典排位模式 2.抢唱模式 3. 接唱模式
     * @param offset 偏移量,开始为0
     * @param cnt    一页数量,最大不可超过100
     * @returnjcl
     */
    public void getSongsListTags(int mode, int offset, int cnt) {
        SongSelectServerApi songSelectServerApi = ApiManager.getInstance().createService(SongSelectServerApi.class);
        ApiMethods.subscribe(songSelectServerApi.getSongsListTags(mode, offset, cnt), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<TagModel> list = JSON.parseArray(result.getData().getString("tags"), TagModel.class);
                    if (view != null) {
                        view.loadSongsTags(list);
                    }
                } else {
                    if (view != null) {
                        view.loadSongsTagsFail();
                    }
                }
            }
        }, this);
    }

}
