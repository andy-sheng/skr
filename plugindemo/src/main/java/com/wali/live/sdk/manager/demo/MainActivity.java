package com.wali.live.sdk.manager.demo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.toast.ToastUtils;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {

    public static final int CHANNEL_ID = 50000;
    RecyclerView mRecyclerView;
    MenuRecyclerviewAdapter mMenuRecyclerviewAdapter;
    TextView mChannelTv;
    int currentChannelId = CHANNEL_ID;

    android.os.Handler handler = new android.os.Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mChannelTv = (TextView) findViewById(R.id.channel_tv);

        mRecyclerView = (RecyclerView) findViewById(R.id.menu_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mMenuRecyclerviewAdapter = new MenuRecyclerviewAdapter(this);
        mRecyclerView.setAdapter(mMenuRecyclerviewAdapter);
        mMenuRecyclerviewAdapter.setChannleClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentChannelId = currentChannelId==50000?50001:50000;
                MiLiveSdkController.init(MainActivity.this.getApplication(),currentChannelId);
                mChannelTv.setText("宿主id:"+currentChannelId);
            }
        });
        //建议在 application里初始化这个
        MiLiveSdkController.init(this.getApplication(),CHANNEL_ID);
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onEvent(MiLiveSdkEvent.LoginResult event){
        if(event.code == MiLiveSdkEvent.SUCCESS){
            ToastUtils.showToast("登录成功");
        }else{

        }
    }

    @Subscribe
    public void onEvent(MiLiveSdkEvent.LogoffResult event){
        if(event.code == MiLiveSdkEvent.SUCCESS){
            ToastUtils.showToast("登出成功");
        }else{

        }
    }

    @Subscribe
    public void onEvent(MiLiveSdkEvent.WantLogin event){
        ToastUtils.showToast("用户触发了只有登录才有的操作,回调给宿主,宿主传递账号信息给插件");
        handler.post(new Runnable() {
            @Override
            public void run() {

                // 在异步线程进行
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //  小米授权登录的
                        mMenuRecyclerviewAdapter.ssoLogin();
                    }
                }).start();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
