package com.wali.live.sdk.manager.aardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.wali.live.livesdk.live.MiLiveSdkController;
import com.wali.live.sdk.manager.aardemo.global.GlobalData;
import com.wali.live.sdk.manager.aardemo.utils.ToastUtils;
import com.wali.live.watchsdk.IMiLiveSdk;
import com.wali.live.watchsdk.ipc.service.ShareInfo;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private MenuRecyclerAdapter mMenuRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.menu_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mMenuRecyclerAdapter = new MenuRecyclerAdapter(this);
        mRecyclerView.setAdapter(mMenuRecyclerAdapter);
        GlobalData.setApplication(this.getApplication());
//        MiLiveSdkController.getInstance().setShareType(ShareInfo.TYPE_WECHAT | ShareInfo.TYPE_MOMENT | ShareInfo.TYPE_WEIBO);
        MiLiveSdkController.getInstance().setCallback(new IMiLiveSdk.ICallback() {

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

            @Override
            public void notifyWantShare(ShareInfo shareInfo) {
                ToastUtils.showToast("notifyWantShare " + ((shareInfo != null) ? shareInfo.toString() : ""));
                if (shareInfo != null) {
                    MiLiveSdkController.getInstance().notifyShareSuc(shareInfo.getPlatForm());
                }
            }

            @Override
            public void notifyWantFollow(long uuid) {
                ToastUtils.showToast("notifyWantFollow, uuid=" + uuid);
            }
        });
    }
}
