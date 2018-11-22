package com.example.rxretrofit.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.rxretrofit.Api.BaseResultEntity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.example.rxretrofit.HttpPostService;
import com.example.rxretrofit.entity.api.SubjectPostApi;
import com.example.rxretrofit.entity.api.UploadApi;
import com.example.rxretrofit.entity.resulte.RetrofitEntity;
import com.example.rxretrofit.entity.resulte.SubjectResulte;
import com.example.rxretrofit.entity.resulte.UploadResulte;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.wali.live.moduletest.R;
import com.wali.live.moduletest.activity.TestSdkActivity;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RxRetrofitFragment extends BaseFragment {

    TextView mTestRxretrofitSimple;
    TextView mTestRxretrofitEncap;
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
        mTestRxretrofitSimple = (TextView) mRootView.findViewById(R.id.test_rxretrofit_simple);
        mTestRxretrofitEncap = (TextView) mRootView.findViewById(R.id.test_rxretrofit_encap);
        mTestRxretrofitDown = (TextView) mRootView.findViewById(R.id.test_rxretrofit_down);
        mTestRxretrofitUpload = (TextView) mRootView.findViewById(R.id.test_rxretrofit_upload);

        mTestMsg = (TextView) mRootView.findViewById(R.id.test_msg);
        mTestImg = (ImageView) mRootView.findViewById(R.id.test_img);

        mTestRxretrofitSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simple();
            }
        });

        mTestRxretrofitEncap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                encap();
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
     *  上传
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
    private void encap() {
        SubjectPostApi postApi = new SubjectPostApi();
        Observable<BaseResultEntity<List<SubjectResulte>>> observable = postApi.getAllVedioBys(true);
        observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResultEntity<List<SubjectResulte>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BaseResultEntity<List<SubjectResulte>> listBaseResultEntity) {
                        mTestMsg.setVisibility(View.VISIBLE);
                        mTestMsg.setText("网络返回：\n" + listBaseResultEntity.getData().toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        U.getToastUtil().showShort(e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**********************************************************正常不封装使用**********************************/

    /**
     * Retrofit加入rxjava实现http请求
     */
    private void simple() {
        String BASE_URL = "http://www.izaodao.com/Api/";
        //手动创建一个OkHttpClient并设置超时时间
        okhttp3.OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        HttpPostService apiService = retrofit.create(HttpPostService.class);
        Observable<RetrofitEntity> observable = apiService.getAllVedioBy(true);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<RetrofitEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(RetrofitEntity retrofitEntity) {
                        mTestMsg.setVisibility(View.VISIBLE);
                        mTestMsg.setText("无封装：\n" + retrofitEntity.getData().toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        U.getToastUtil().showShort(e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
