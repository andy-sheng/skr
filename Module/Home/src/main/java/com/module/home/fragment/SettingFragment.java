package com.module.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.updateinfo.EditInfoActivity;
import com.pgyersdk.feedback.PgyerFeedbackManager;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class SettingFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mEditPerson;
    RelativeLayout mClearCache;
    RelativeLayout mUserFeedback;
    RelativeLayout mServiceAgreen;
    RelativeLayout mComment;
    ExTextView mExitLogin;
    ExTextView mVersion;

    ExTextView mCacheSizeTv;

    static final String[] CACHE_CAN_DELETE = {
            "fresco", "gif"
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
        mClearCache = (RelativeLayout) mRootView.findViewById(R.id.clear_cache);
        mUserFeedback = (RelativeLayout) mRootView.findViewById(R.id.user_feedback);
        mServiceAgreen = (RelativeLayout) mRootView.findViewById(R.id.service_agreen);
        mComment = (RelativeLayout) mRootView.findViewById(R.id.comment);
        mExitLogin = (ExTextView) mRootView.findViewById(R.id.exit_login);
        mVersion = (ExTextView) mRootView.findViewById(R.id.version);
        mCacheSizeTv = (ExTextView) mRootView.findViewById(R.id.cache_size_tv);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        onBackPressed();
                    }
                });

        RxView.clicks(mEditPerson)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        Intent intent = new Intent(getActivity(), EditInfoActivity.class);
                        startActivity(intent);
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


        RxView.clicks(mUserFeedback)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        new PgyerFeedbackManager.PgyerFeedbackBuilder()
                                .setShakeInvoke(false)           //设置是否摇一摇的方式激活反馈，默认为 true
                                .setBarBackgroundColor("")      // 设置顶部按钮和底部背景色，默认颜色为 #2E2D2D
                                .setBarButtonPressedColor("")        //设置顶部按钮和底部按钮按下时的反馈色 默认颜色为 #383737
                                .setColorPickerBackgroundColor("")   //设置颜色选择器的背景色,默认颜色为 #272828
                                .setBarImmersive(true)              //设置activity 是否以沉浸式的方式打开，默认为 false
                                .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)   //设置以Dialog 的方式打开
                                .setMoreParam("KEY1","VALUE1")
                                .setMoreParam("KEY2","VALUE2")
                                .builder()
                                .invoke();                  //激活直接显示的方式
                    }
                });

        RxView.clicks(mServiceAgreen)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });

        RxView.clicks(mComment)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });

        RxView.clicks(mExitLogin)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });
        long cacheSize = U.getPreferenceUtils().getSettingLong("key_cache_size", 0);
        long cacheLastUpdateTs = U.getPreferenceUtils().getSettingLong("key_cache_update_ts", 0);
        setCacheSize(cacheSize);
        if (System.currentTimeMillis() - cacheLastUpdateTs > 2 * 60 * 1000) {
            // 大于两分钟了，可以重新算下
            computeCache();
        }
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
                    new File(dirPath).delete();
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
                    }
                });
    }


    void setCacheSize(long byteLen) {
        String s = String.format("%.1fM", byteLen / 1024.0 / 1024.0);
        mCacheSizeTv.setText(s);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(SettingFragment.this);
        return true;
    }
}
