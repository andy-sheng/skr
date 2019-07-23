package com.example.rxretrofit.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.utils.U;
import com.example.rxretrofit.TestService;
import com.example.rxretrofit.fastjson.Sex;
import com.example.rxretrofit.fastjson.Song;
import com.example.rxretrofit.fastjson.SongsEnum;
import com.example.rxretrofit.fastjson.Student;
import com.wali.live.moduletest.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        mTestRxretrofitEncap1 = (TextView) getRootView().findViewById(R.id.test_rxretrofit_encap1);
        mTestRxretrofitEncap2 = (TextView) getRootView().findViewById(R.id.test_rxretrofit_encap2);

        mTestMsg = (TextView) getRootView().findViewById(R.id.test_msg);
        mTestImg = (ImageView) getRootView().findViewById(R.id.test_img);

        mTestRxretrofitEncap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                fastJson1();
//                encap1();
                testParams1();
            }
        });

        mTestRxretrofitEncap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                encap2();
//                fastJson2();
                testParams2();
            }
        });


    }

    private void fastJson1() {
        String text = JSON.toJSONString(SongsEnum.SOUND_OF_SILENCE);
        U.getToastUtil().showShort(text);
    }

    private void fastJson2() {
        Student student = new Student();
        student.setName("name");
        student.setId(1L);
        student.setSex(Sex.MAN);
        MyLog.d(getTAG(), "fastJson2 toString = " + JSON.toJSON(student).toString());
        MyLog.d(getTAG(), "fastJson2 toJSONString = " + JSON.toJSONString(student));
        Student student1 = JSON.parseObject(JSON.toJSON(student).toString(), Student.class);
        Student student2 = JSON.parseObject(JSON.toJSONString(student), Student.class);
        String str1 = "{\"id\":1,\"name\":\"name\",\"sex\":\"MAN\"}";
        Student stu1 = JSON.parseObject(str1, Student.class);
        U.getToastUtil().showShort(JSON.toJSONString(stu1));
    }

    String text = "";
    public void testParams1() {
        Map<Integer, Song> map = new HashMap<>();
        Song song = new Song();
        song.setXqusic_id("2");
        song.setXqusic_mid("3");
        map.put(0, song);
        map.put(1, song);
        MyLog.d(getTAG(), "testParams1 toString = " + JSON.toJSON(map).toString());
        text = JSON.toJSON(map).toString();
    }

    public void testParams2() {
        Map<Integer,Song> s = JSON.parseObject(text, new TypeReference<Map<Integer, Song>>(){});
        MyLog.d(getTAG(), "testParams2" );
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
                            MyLog.d(getTAG(), "i:" + i + ",j:" + j + " v:" + array2.getString(j));
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
                U.getToastUtil().showShort("得到数据结果 使用fastjoson转成list，list.size:" + list.size());
                for (Song s : list) {
                    MyLog.w(getTAG(), "song:" + s.toString());
                }
            }
        }, this);
    }

}
