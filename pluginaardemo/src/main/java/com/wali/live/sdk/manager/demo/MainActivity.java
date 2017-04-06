package com.wali.live.sdk.manager.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.wali.live.livesdk.live.IMiLiveSdk;
import com.wali.live.livesdk.live.MiLiveSdkController;
import com.wali.live.sdk.manager.demo.global.GlobalData;
import com.wali.live.sdk.manager.demo.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    public static final int CHANNEL_ID = 50000;

    private int currentChannelId = CHANNEL_ID;

    private RecyclerView mRecyclerView;
    private MenuRecyclerAdapter mMenuRecyclerAdapter;
    private TextView mChannelTv;

    private Handler mUiHander = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mChannelTv = (TextView) findViewById(R.id.channel_tv);
        mRecyclerView = (RecyclerView) findViewById(R.id.menu_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mMenuRecyclerAdapter = new MenuRecyclerAdapter(this);
        mRecyclerView.setAdapter(mMenuRecyclerAdapter);
        GlobalData.setApplication(this.getApplication());
        //建议在 application里初始化这个
        MiLiveSdkController.getInstance().init(this.getApplication(), CHANNEL_ID, "TEST SECRET", new IMiLiveSdk.ICallback() {

            @Override
            public void notifyLogin(int var1) {
                if (var1 == IMiLiveSdk.ICallback.CODE_SUCCESS) {
                    ToastUtils.showToast("登录成功");
                } else {
                    ToastUtils.showToast("登录错误，错误码：" + var1);
                }
            }

            @Override
            public void notifyLogoff(int var1) {
                if (var1 == IMiLiveSdk.ICallback.CODE_SUCCESS) {
                    ToastUtils.showToast("登出成功");
                }
            }

            @Override
            public void notifyWantLogin() {
                ToastUtils.showToast("用户触发了只有登录才有的操作,回调给宿主,宿主传递账号信息给插件");
                mMenuRecyclerAdapter.oauthLogin();
            }

            @Override
            public void notifyVerifyFailure(int var1) {
                ToastUtils.showToast("验证失败，errCode=" + var1);
            }

            @Override
            public void notifyOtherAppActive() {
                ToastUtils.showToast("有其他APP在活跃");
            }
        });
    }

    public int getCurrentChannelId() {
        return currentChannelId;
    }
}
