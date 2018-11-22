package com.example.rxretrofit.entity.api;

import com.common.rxretrofit.Api.BaseApi;
import com.common.rxretrofit.Api.BaseResultEntity;
import com.common.rxretrofit.http.HttpManager;
import com.example.rxretrofit.HttpPostService;
import com.example.rxretrofit.entity.resulte.SubjectResulte;

import java.util.List;

import io.reactivex.Observable;

/**
 * 测试数据
 * Created by WZG on 2016/7/16.
 */
public class SubjectPostApi extends BaseApi {
//    接口需要传入的参数 可自定义不同类型
    private boolean all;
    /*任何你先要传递的参数*/
//    String xxxxx;
//    String xxxxx;
//    String xxxxx;
//    String xxxxx;

    public SubjectPostApi() {
        super();
        setCache(false);
        setMethod("AppFiftyToneGraph/videoLink");
        setCookieNetWorkTime(60);
        setCookieNoNetWorkTime(24*60*60);
    }

    public Observable<BaseResultEntity<List<SubjectResulte>>> getAllVedioBys(boolean isAll) {
        HttpPostService service = HttpManager.getInstance().doHttpRequest(this).create(HttpPostService.class);
        return service.getAllVedioBys(isAll);
    }


}
