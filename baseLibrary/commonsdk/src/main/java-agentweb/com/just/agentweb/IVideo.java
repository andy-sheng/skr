package com.just.agentweb;


import android.view.View;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;

/**
 * Created by cenxiaozhong on 2017/6/10.
 * source CODE  https://github.com/Justson/AgentWebX5
 */

public interface IVideo {


    void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback);


    void onHideCustomView();


    boolean isVideoState();

}
