package com.module.home.musictest.presenter;

import com.alibaba.fastjson.JSON;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.home.musictest.MusicTestServerApi;
import com.module.home.musictest.model.Question;
import com.module.home.musictest.view.IQuestionView;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MusicTestPresenter extends RxLifeCyclePresenter {

    IQuestionView mView;
    MusicTestServerApi mMusicTestServerApi;

    public MusicTestPresenter(IQuestionView view) {
        mMusicTestServerApi = ApiManager.getInstance().createService(MusicTestServerApi.class);
        this.mView = view;
    }

    // 获取题目
    public void getQuestionList() {
        ApiMethods.subscribe(mMusicTestServerApi.getQuestionList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<Question> questions = JSON.parseArray(result.getData().getString("list"), Question.class);
                    mView.loadQuestionsData(questions);
                }
            }
        }, this);
    }

    // 上报题目答案
    public void reportAnswer() {
        HashMap<String, Object> map = new HashMap<>();
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mMusicTestServerApi.reportAnswer(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {

                }
            }
        });
    }

}
