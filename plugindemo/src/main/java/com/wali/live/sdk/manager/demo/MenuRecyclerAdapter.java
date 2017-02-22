package com.wali.live.sdk.manager.demo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wali.live.sdk.manager.IMiLiveSdk;
import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.SdkUpdateHelper;
import com.wali.live.sdk.manager.demo.global.GlobalData;
import com.wali.live.sdk.manager.demo.utils.ToastUtils;
import com.xiaomi.passport.servicetoken.ServiceTokenFuture;
import com.xiaomi.passport.servicetoken.ServiceTokenResult;
import com.xiaomi.passport.servicetoken.ServiceTokenUtilFacade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 2016/12/8.
 */
public class MenuRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static String TAG = MenuRecyclerAdapter.class.getSimpleName();

    private String sidForMiLive = "xmzhibo";

    private List<Bean> mDataList = new ArrayList();

    private Activity mActivity;
    private SdkUpdateHelper mSdkUpdateHelper;

    public MenuRecyclerAdapter(final Context context, @Nullable SdkUpdateHelper sdkUpdateHelper) {
        mActivity = (Activity) context;
        mSdkUpdateHelper = sdkUpdateHelper;

        mDataList.add(new Bean("跳转到直播(Intent)", new Runnable() {
            @Override
            public void run() {
                MiLiveSdkController.getInstance().openWatch(
                        mActivity, 21050016, "21050016_1482903828", "http://v2.zb.mi.com/live/21050016_1482903828.flv?playui=0",
                        IMiLiveSdk.TYPE_LIVE_PUBLIC, new IMiLiveSdk.IOpenCallback() {
                            @Override
                            public void notifyNotInstall() {
                                ToastUtils.showToast("notifyNotInstall");
                            }
                        });
            }
        }));
        mDataList.add(new Bean("跳转到回放(Intent)", new Runnable() {
            @Override
            public void run() {
                MiLiveSdkController.getInstance().openReplay(
                        mActivity, 22869193l, "22869193_1480938327", "http://playback.ks.zb.mi.com/record/live/22869193_1480938327/hls/22869193_1480938327.m3u8?playui=1",
                        IMiLiveSdk.TYPE_LIVE_PUBLIC, new IMiLiveSdk.IOpenCallback() {
                            @Override
                            public void notifyNotInstall() {
                                ToastUtils.showToast("notifyNotInstall");
                            }
                        });
            }
        }));
//        mDataList.add(new Bean("开启游戏直播(AIDL)", new Runnable() {
//            @Override
//            public void run() {
//                MiLiveSdkController.openGameLive();
//            }
//        }));
        mDataList.add(new Bean("宿主传OAuth登录账号(AIDL)", new Runnable() {
            @Override
            public void run() {
                oauthLogin();
            }
        }));
        mDataList.add(new Bean("宿主传Sso登录账号(AIDL)", new Runnable() {
            @Override
            public void run() {
                ssoLogin();
            }
        }));
        mDataList.add(new Bean("登出当前宿主账号(AIDL)", new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //  小米授权登录的
                        MiLiveSdkController.getInstance().clearAccount();
                    }
                }).start();
            }
        }));

        mDataList.add(new Bean("跳转到随机直播(Intent，测试之后会删除)", new Runnable() {
            @Override
            public void run() {
                // just for test
                MiLiveSdkController.getInstance().openRandomLive((Activity) context);
            }
        }));

        mDataList.add(new Bean("检查更新", new Runnable() {
            @Override
            public void run() {
                if (mSdkUpdateHelper != null) {
                    mSdkUpdateHelper.checkUpdate();
                }
            }
        }));

        mDataList.add(new Bean("切换宿主id,模拟切换了宿主", new Runnable() {
            @Override
            public void run() {
                if (channleClickListener != null) {
                    channleClickListener.onClick(null);
                }
            }
        }));
    }

    public void oauthLogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //  小米授权登录的
                String code = XiaoMiOAuth.getOAuthCode(mActivity);
                MiLiveSdkController.getInstance().loginByMiAccountOAuth(code);
            }
        }).start();
    }

    public void ssoLogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //  小米授权登录的
                AccountManager am = AccountManager.get(GlobalData.app().getApplicationContext());
                if (ActivityCompat.checkSelfPermission(GlobalData.app(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ToastUtils.showToast("没有获取账号的权限");
                    return;
                }
                Account[] accounts = am.getAccountsByType("com.xiaomi");
                if (accounts != null && accounts.length > 0) {
                    long miid = Long.parseLong(accounts[0].name);
                    /**
                     * 这里获取ssotoken有两种方式
                     */
                    String ssoToken = getServiceTokenNew(GlobalData.app());
                    MiLiveSdkController.getInstance().loginByMiAccountSso(miid, ssoToken);
                }
            }
        }).start();
    }

    private String getServiceTokenNew(Context context) {
        ServiceTokenFuture serviceTokenFuture = ServiceTokenUtilFacade.getInstance()
                .buildMiuiServiceTokenUtil()
                .getServiceToken(context, sidForMiLive);
        ServiceTokenResult serviceTokenResult = serviceTokenFuture.get();
        if (serviceTokenResult == null) {
            Log.e(TAG, "getServiceToken is null");
            return null;
        }
        ServiceTokenResult.ErrorCode errCode = serviceTokenResult.errorCode;
        if (errCode != ServiceTokenResult.ErrorCode.ERROR_NONE) {
            ToastUtils.showToast("account getServiceToken errCode=" + errCode);
            return null;
        }
        Log.d(TAG, "getServiceToken success");
        return serviceTokenResult.serviceToken;
    }

    private String getServiceTokenOld(Account account) {
        try {
            AccountManagerFuture<Bundle> future = null;
            if (GlobalData.app() == null) {
                future = AccountManager.get(GlobalData.app()).getAuthToken(account, sidForMiLive, null, true, null, null);
            } else {
                future = AccountManager.get(GlobalData.app()).getAuthToken(account, sidForMiLive, null, mActivity, null, null);
            }
            if (future != null) {
                String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                return authToken;
            }
        } catch (OperationCanceledException e) {
            Log.e(TAG, "get auth token error", e);
        } catch (AuthenticatorException e) {
            Log.e(TAG, "get auth token error", e);
        } catch (IOException e) {
            Log.e(TAG, "get auth token error", e);
        } catch (Exception e) {
            Log.e(TAG, "get auth token error", e);
        }
        return null;
    }

    View.OnClickListener channleClickListener;

    public void setChannleClickListener(View.OnClickListener listener) {
        channleClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        tv.setLayoutParams(lp);
        RecyclerView.ViewHolder vh = new NormalViewHolder(tv);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
        final Bean bean = mDataList.get(position);
        normalViewHolder.tv.setText(bean.content);
        normalViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bean.runnable.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private static class NormalViewHolder extends RecyclerView.ViewHolder {
        public TextView tv;

        public NormalViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView;
        }
    }

    private static class Bean {
        public String content;
        public Runnable runnable;

        public Bean(String content, Runnable runnable) {
            this.content = content;
            this.runnable = runnable;
        }
    }
}
