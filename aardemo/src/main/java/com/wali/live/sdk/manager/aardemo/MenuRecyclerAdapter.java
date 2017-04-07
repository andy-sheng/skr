package com.wali.live.sdk.manager.aardemo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.log.MyLog;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.data.LiveShow;
import com.mi.live.data.location.Location;
import com.mi.live.data.manager.UserInfoManager;
import com.wali.live.livesdk.live.MiLiveSdkController;
import com.wali.live.proto.Live2Proto;
import com.wali.live.proto.LiveProto;
import com.wali.live.sdk.manager.aardemo.global.GlobalData;
import com.wali.live.sdk.manager.aardemo.utils.ToastUtils;
import com.xiaomi.passport.servicetoken.ServiceTokenFuture;
import com.xiaomi.passport.servicetoken.ServiceTokenResult;
import com.xiaomi.passport.servicetoken.ServiceTokenUtilFacade;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 2016/12/8.
 */
public class MenuRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public final static String TAG = MenuRecyclerAdapter.class.getSimpleName();

    private String sidForMiLive = "xmzhibo";

    private List<Bean> mDataList = new ArrayList();

    private Activity mActivity;

    private String mPlayerId = "";
    private String mRePlayerId = "";

    public MenuRecyclerAdapter(final Context context) {
        mActivity = (Activity) context;

        mDataList.add(new Bean("跳转到直播(Intent)", new Runnable() {
            @Override
            public void run() {
                // 测试使用，外部调用请使用MiLiveSdkController.openWatch跳转到直播
                inputPlayerId(false);
            }
        }));
        mDataList.add(new Bean("跳转到回放(Intent)", new Runnable() {
            @Override
            public void run() {
                // 测试使用，外部调用请使用MiLiveSdkController.openRelay跳转到回放
                inputPlayerId(true);
            }
        }));
        mDataList.add(new Bean("开启秀场直播(Intent)", new Runnable() {
            @Override
            public void run() {
                // Location根据需要传，不需要可以传空
                MiLiveSdkController.getInstance().openNormalLive(mActivity, null);
            }
        }));
        mDataList.add(new Bean("开启游戏直播(Intent)", new Runnable() {
            @Override
            public void run() {
                // Location根据需要传，不需要可以传空
                MiLiveSdkController.getInstance().openGameLive(mActivity, Location.Builder.newInstance(223, 224).setCountry("USA").setProvince("New York").setCity("New York").build());
            }
        }));
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
        mDataList.add(new Bean("第三方登录(AIDL)", new Runnable() {
            @Override
            public void run() {
                thirdPartLogin();
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
    }

    private void inputPlayerId(final boolean isReplay) {
        final MyAlertDialog.Builder builder = new MyAlertDialog.Builder(mActivity);
        builder.setTitle(isReplay ? "请输入回放的主播id" : "请输入对应的主播id");
        builder.setInputView();
        builder.getInputView().setText(isReplay ? mRePlayerId : mPlayerId);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playerId = builder.getInputView().getText().toString();
                if (TextUtils.isEmpty(playerId)) {
                    ToastUtils.showToast("主播id不能为空");
                    return;
                }
                if (isReplay) {
                    mRePlayerId = playerId;
                    enterReplay(playerId);
                } else {
                    mPlayerId = playerId;
                    enterWatch(playerId);
                }
            }
        });
        builder.show();
    }

    private void enterReplay(final String playerId) {
        Observable
                .just(0)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        /**
                         * 内部接口，这里方便demo测试使用，外层应用请不要随意调用
                         */
                        LiveProto.HistoryLiveRsp rsp = LiveManager.historyRsp(Long.parseLong(playerId));
                        if (rsp == null) {
                            return null;
                        }
                        Live2Proto.HisLive hisLive = rsp.getHisLive(0);
                        MiLiveSdkController.getInstance().openReplay(
                                mActivity, Long.parseLong(playerId), hisLive.getLiveId(), hisLive.getUrl(), 0);
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    private void enterWatch(final String playerId) {
        Observable
                .just(0)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        /**
                         * 内部接口，这里方便demo测试使用，外层应用请不要随意调用
                         */
                        LiveShow liveShow = UserInfoManager.getLiveShowByUserId(Long.parseLong(playerId));
                        if (liveShow == null) {
                            return null;
                        }
                        MiLiveSdkController.getInstance().openWatch(
                                mActivity, liveShow.getUid(), liveShow.getLiveId(), liveShow.getUrl(), 0);
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
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

    private void ssoLogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 小米授权登录的
                AccountManager am = AccountManager.get(GlobalData.app().getApplicationContext());
                if (ActivityCompat.checkSelfPermission(GlobalData.app(), Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ToastUtils.showToast("没有获取账号的权限");
                    return;
                }
                Account[] accounts = am.getAccountsByType("com.xiaomi");
                if (accounts != null && accounts.length > 0) {
                    long miid = Long.parseLong(accounts[0].name);
                    String ssoToken = getServiceToken(GlobalData.app());
                    MiLiveSdkController.getInstance().loginByMiAccountSso(miid, ssoToken);
                }
            }
        }).start();
    }

    private void thirdPartLogin() {
        Intent intent = new Intent(mActivity, ThirdPartLoginActivity.class);
        intent.putExtra(ThirdPartLoginActivity.KEY_CHANNELID, ((MainActivity) mActivity).getCurrentChannelId());
        mActivity.startActivity(intent);
    }

    private String getServiceToken(Context context) {
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView tv = new TextView(parent.getContext());
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
