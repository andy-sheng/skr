package com.module.home.setting.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.upgrade.UpgradeManager;
import com.common.core.upgrade.UpgradeCheckApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.RomUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.TipsDialogView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.feedback.FeedbackFragment;
import com.module.home.updateinfo.EditInfoActivity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.relation.fragment.BlackListFragment;
import com.zq.toast.CommonToastView;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class SettingFragment extends BaseFragment {

    public final static String TAG = "SettingFragment";

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;

    RelativeLayout mEditPerson;
    RelativeLayout mVolumeSet;

    RelativeLayout mClearCache;
    ExImageView mCacheArraw;
    ExTextView mCacheSizeTv;

    RelativeLayout mVersionArea;
    ExTextView mVersionTips;
    ExImageView mNewVersionIv;
    ExImageView mVersionArrow;
    ExTextView mVersionTv;

    RelativeLayout mInviteCode;
    RelativeLayout mUserBlacklist;
    RelativeLayout mUserFeedback;
    RelativeLayout mComment;
    RelativeLayout mServiceAgreen;
    ExTextView mExitLogin;

    boolean hasNewVersion = false; // 判断是否有新版本

    static final String[] CACHE_CAN_DELETE = {
            "fresco", "gif", "upload"
    };

    @Override
    public int initView() {
        return R.layout.setting_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);

        mEditPerson = (RelativeLayout) mRootView.findViewById(R.id.edit_person);
        mVolumeSet = (RelativeLayout) mRootView.findViewById(R.id.volume_set);

        mClearCache = (RelativeLayout) mRootView.findViewById(R.id.clear_cache);
        mCacheArraw = (ExImageView) mRootView.findViewById(R.id.cache_arraw);
        mCacheSizeTv = (ExTextView) mRootView.findViewById(R.id.cache_size_tv);

        mVersionArea = (RelativeLayout) mRootView.findViewById(R.id.version_area);
        mVersionTips = (ExTextView) mRootView.findViewById(R.id.version_tips);
        mNewVersionIv = (ExImageView) mRootView.findViewById(R.id.new_version_iv);
        mVersionArrow = (ExImageView) mRootView.findViewById(R.id.version_arrow);
        mVersionTv = (ExTextView) mRootView.findViewById(R.id.version_tv);

        mInviteCode = (RelativeLayout) mRootView.findViewById(R.id.invite_code);
        mUserBlacklist = (RelativeLayout) mRootView.findViewById(R.id.user_blacklist);
        mUserFeedback = (RelativeLayout) mRootView.findViewById(R.id.user_feedback);
        mComment = (RelativeLayout) mRootView.findViewById(R.id.comment);
        mServiceAgreen = (RelativeLayout) mRootView.findViewById(R.id.service_agreen);
        mExitLogin = (ExTextView) mRootView.findViewById(R.id.exit_login);

        U.getSoundUtils().preLoad(TAG, R.raw.general_back, R.raw.allclick);

        initCache();
        initVersion();

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getSoundUtils().play(TAG, R.raw.general_back);
                        U.getFragmentUtils().popFragment(SettingFragment.this);
                    }
                });

        RxView.clicks(mEditPerson)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_INFO)
                                .navigation();
                    }
                });

        RxView.clicks(mClearCache)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clearCache();
                    }
                });

        RxView.clicks(mVolumeSet)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), VolumeFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
                    }
                });

        RxView.clicks(mUserBlacklist)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), BlackListFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
                    }
                });


        RxView.clicks(mUserFeedback)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
//                        FeedbackManager.openFeedbackActivity();
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), FeedbackFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
                    }
                });

        RxView.clicks(mServiceAgreen)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        // TODO: 2018/12/26 用户服务协议
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                .withString(RouterConstants.KEY_WEB_URL, "https://api.inframe.mobi/user-agreement.html")
                                .navigation();
                    }
                });

        RxView.clicks(mComment)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        gotoMarketDetail(getActivity());
                    }
                });

        RxView.clicks(mExitLogin)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        exitLogin();
                    }
                });

        mVersionTv.setText("版本号:" + U.getAppInfoUtils().getVersionName());
        mVersionArea.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 跳到调试中心页面
                ARouter.getInstance().build(RouterConstants.ACTIVITY_DEBUG_CORE_ACTIVITY).navigation();
                return false;
            }
        });

        mVersionArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasNewVersion) {
                    UpgradeManager.getInstance().checkUpdate2();
                }
                UpgradeManager.getInstance().setNotNeedShowRedDotTips();
            }
        });

        RxView.clicks(mInviteCode)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), InviteCodeFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
                    }
                });
    }

    private void initVersion() {
        UpgradeCheckApi checkApi = ApiManager.getInstance().createService(UpgradeCheckApi.class);
        ApiMethods.subscribe(checkApi.getUpdateInfo(U.getAppInfoUtils().getPackageName(), 2, 1, U.getAppInfoUtils().getVersionCode()),
                new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult apiResult) {
                        if (apiResult.getErrno() == 0) {
                            boolean needUpdate = apiResult.getData().getBoolean("needUpdate");
                            if (needUpdate) {
                                hasNewVersion = true;
                                mNewVersionIv.setVisibility(View.VISIBLE);
                                mVersionArrow.setVisibility(View.VISIBLE);
                            } else {
                                hasNewVersion = false;
                                mNewVersionIv.setVisibility(View.GONE);
                                mVersionArrow.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });
    }

    private void initCache() {
        long cacheSize = U.getPreferenceUtils().getSettingLong("key_cache_size", 0);
        long cacheLastUpdateTs = U.getPreferenceUtils().getSettingLong("key_cache_update_ts", 0);
        setCacheSize(cacheSize);
        if (System.currentTimeMillis() - cacheLastUpdateTs > 2 * 60 * 1000) {
            // 大于两分钟了，可以重新算下
            computeCache();
        }
    }


    private void exitLogin() {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setMessageTip("确定退出当前账号么？")
                .setConfirmTip("确定")
                .setCancelTip("取消")
                .build();

        DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view instanceof ExTextView) {
                            if (view.getId() == R.id.confirm_tv) {
                                dialog.dismiss();
                                U.getSoundUtils().play(TAG, R.raw.allclick);
                                U.getFragmentUtils().popFragment(SettingFragment.this);
                                UserAccountManager.getInstance().logoff();
                            }

                            if (view.getId() == R.id.cancel_tv) {
                                U.getSoundUtils().play(TAG, R.raw.general_back);
                                dialog.dismiss();
                            }
                        }
                    }
                })
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {

                    }
                })
                .create().show();
    }

    void computeCache() {
        Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {
                /**
                 * 缓存中有原唱 伴奏 歌词 midi logs fresco git 音效。目前好像就fresco图片可以删除，别的删除都会影响
                 */
                long len = 0;
                for (String dirName : CACHE_CAN_DELETE) {
                    String dirPath = U.getAppInfoUtils().getSubDirPath(dirName);
                    len += U.getFileUtils().getDirSize(dirPath);
                }
                emitter.onNext(len);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long size) throws Exception {
                        U.getPreferenceUtils().setSettingLong("key_cache_size", size);
                        U.getPreferenceUtils().setSettingLong("key_cache_update_ts", System.currentTimeMillis());
                        setCacheSize(size);
                    }
                });
    }

    void clearCache() {
        Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> emitter) throws Exception {
                /**
                 * 缓存中有原唱 伴奏 歌词 midi logs fresco git 音效。目前好像就fresco图片可以删除，别的删除都会影响
                 */
                for (String dirName : CACHE_CAN_DELETE) {
                    String dirPath = U.getAppInfoUtils().getSubDirPath(dirName);
                    U.getFileUtils().deleteAllFiles(dirPath);
                }
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long size) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        U.getToastUtil().showShort("缓存已清除");
                        // 做个假的无所谓，不管清没清干净，都给设置成0
                        U.getPreferenceUtils().setSettingLong("key_cache_size", 0);
                        U.getPreferenceUtils().setSettingLong("key_cache_update_ts", System.currentTimeMillis());
                        setCacheSize(0);
                        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(getContext())
                                .setImage(R.drawable.qingchuhuancun)
                                .setText("清除缓存成功")
                                .build());
                    }
                });
    }


    void setCacheSize(long byteLen) {
        String s = String.format("%.1fM", byteLen / 1024.0 / 1024.0);
        mCacheSizeTv.setText(s);
    }

    void gotoMarketDetail(Activity activity) {
//        String appPkg = U.getAppInfoUtils().getPackageName();
        String appPkg = "com.zq.live";
        String marketPkg = RomUtils.getRomMarketPkgName();
        try {
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
