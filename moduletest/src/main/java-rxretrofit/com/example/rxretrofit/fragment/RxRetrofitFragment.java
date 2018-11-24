package com.example.rxretrofit.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.rxretrofit.Api.BaseResultEntity;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.example.rxretrofit.entity.api.UploadApi;
import com.example.rxretrofit.entity.resulte.UploadResulte;
import com.example.rxretrofit.fastjson.Song;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.wali.live.moduletest.R;
import com.example.rxretrofit.TestService;

import java.io.File;
import java.net.URI;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RxRetrofitFragment extends BaseFragment {

    TextView mTestRxretrofitEncap1;
    TextView mTestRxretrofitEncap2;
    TextView mTestRxretrofitDown;
    TextView mTestRxretrofitUpload;

    TextView mTestMsg;
    ImageView mTestImg;

    @Override
    public int initView() {
        return R.layout.rxretrofit_test_fragment_layout;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTestRxretrofitEncap1 = (TextView) mRootView.findViewById(R.id.test_rxretrofit_encap1);
        mTestRxretrofitEncap2 = (TextView) mRootView.findViewById(R.id.test_rxretrofit_encap2);
        mTestRxretrofitDown = (TextView) mRootView.findViewById(R.id.test_rxretrofit_down);
        mTestRxretrofitUpload = (TextView) mRootView.findViewById(R.id.test_rxretrofit_upload);

        mTestMsg = (TextView) mRootView.findViewById(R.id.test_msg);
        mTestImg = (ImageView) mRootView.findViewById(R.id.test_img);

        mTestRxretrofitEncap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                encap1();
            }
        });

        mTestRxretrofitEncap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                encap2();
            }
        });


        mTestRxretrofitDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDownFragment();
            }
        });

        mTestRxretrofitUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload();
            }
        });
    }

    /**
     * 上传
     */
    private void upload() {
        UploadApi uploadApi = new UploadApi();
        uploadApi.uploadImage(4811420, "cfed6cc8caad0d79ea56d917376dc4df", "/storage/emulated/0/Download/11.jpg", "image/jpeg")
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new Observer<BaseResultEntity<UploadResulte>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BaseResultEntity<UploadResulte> uploadResulteBaseResultEntity) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**********************************************************封装使用**********************************/

    /**
     * Retrofit加入rxjava实现http请求
     */
    private void encap1() {
        TestService mallService = ApiManager.getInstance().createService(TestService.class);
        ApiMethods.subscribe(mallService.getGoods("裤子"), new ApiObserver<JSONObject>() {

            @Override
            public void process(JSONObject obj) {
                try {
                    U.getToastUtil().showShort(obj.toJSONString());
                    JSONArray jsonArray = obj.getJSONArray("result");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONArray array2 = jsonArray.getJSONArray(i);
                        for (int j = 0; j < array2.size(); j++) {
                            MyLog.d(TAG, "i:" + i + ",j:" + j + " v:" + array2.getString(j));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, this);
    }

    private void encap2() {
        TestService mallService = ApiManager.getInstance().createService(TestService.class);
        ApiMethods.subscribe(mallService.getCartoons(), new ApiObserver<JSONObject>() {

            @Override
            public void process(JSONObject obj) {
                List<Song> list = JSON.parseArray(obj.getJSONObject("data").getString("playlist"), Song.class);
                U.getToastUtil().showShort("得到数据结果 使用fastjoson转成list，list.size:"+list.size());
                for(Song s:list){
                    MyLog.w(TAG,"song:"+s.toString());
                }
            }
        }, this);
    }


    /**
     * 打开下载
     */
    private void openDownFragment() {
        U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(getActivity(), RxRetrDownFragment.class)
                .setHasAnimation(true)
                .build());
    }
}
