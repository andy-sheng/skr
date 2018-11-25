package com.example.rxretrofit.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.utils.U;
import com.example.rxretrofit.TestService;
import com.example.rxretrofit.fastjson.Song;
import com.wali.live.moduletest.R;

import java.util.List;

public class RxRetrofitFragment extends BaseFragment {

    TextView mTestRxretrofitEncap1;
    TextView mTestRxretrofitEncap2;

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

}
