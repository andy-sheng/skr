package com.example.rxretrofit.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.wali.live.moduletest.R;
import com.wali.live.moduletest.retrofit.MallService;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class RxRetrofitFragment extends BaseFragment {

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
        mTestRxretrofitEncap = (TextView) mRootView.findViewById(R.id.test_rxretrofit_encap);
        mTestRxretrofitDown = (TextView) mRootView.findViewById(R.id.test_rxretrofit_down);
        mTestRxretrofitUpload = (TextView) mRootView.findViewById(R.id.test_rxretrofit_upload);

        mTestMsg = (TextView) mRootView.findViewById(R.id.test_msg);
        mTestImg = (ImageView) mRootView.findViewById(R.id.test_img);

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
        MallService mallService = ApiManager.getInstance().createService(MallService.class);
        ApiMethods.subscribe(mallService.getGoods(), new ApiObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody response) {
                try {
                    U.getToastUtil().showShort(response.string());
                } catch (IOException e) {
                    e.printStackTrace();
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
