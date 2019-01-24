package com.module.home.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.banner.BannerImageLoader;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.myinfo.event.ScoreDetailChangeEvent;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.component.busilib.constans.GameModeType;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.MainPageSlideApi;
import com.module.home.R;
import com.module.home.model.SlideShowModel;
import com.module.home.widget.UserInfoTitleView;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.zq.level.view.NormalLevelView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

public class GameFragment extends BaseFragment {

    public final static String TAG = "GameFragment";

    UserInfoTitleView mUserTitleView;
    NormalLevelView mLevelView;

    Banner mBannerView;

    int mRank = 0;           //当前父段位
    int mSubRank = 0;        //当前子段位
    int mStarNum = 0;        //当前星星
    int mStarLimit = 0;      //当前星星上限

    MainPageSlideApi mMainPageSlideApi;

    BehaviorSubject<Integer> mIntegerBehaviorSubject;

    Vector<Long> mTag = new Vector<>();

    @Override
    public int initView() {
        return R.layout.game_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        ExImageView ivAthleticsPk = (ExImageView) mRootView.findViewById(R.id.iv_athletics_pk);
        ExImageView mIvGrabPk = (ExImageView) mRootView.findViewById(R.id.iv_grab_game);
        mUserTitleView = (UserInfoTitleView) mRootView.findViewById(R.id.user_title_view);
        mLevelView = (NormalLevelView) mRootView.findViewById(R.id.level_view);
        mBannerView = (Banner) mRootView.findViewById(R.id.banner_view);

        initLevel();

        RxView.clicks(ivAthleticsPk)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        long tag = System.currentTimeMillis();
                        checkGameConf(1, tag, ivAthleticsPk);
                        clickAnimation(ivAthleticsPk, tag);
                    }
                });

        RxView.clicks(mIvGrabPk)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        long tag = System.currentTimeMillis();
                        checkGameConf(3, tag, mIvGrabPk);
                        clickAnimation(mIvGrabPk, tag);
                    }
                });

        U.getSoundUtils().preLoad(TAG, R.raw.home_game, R.raw.general_button);

        initOperationArea();
    }

    private void initOperationArea() {
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        ApiMethods.subscribe(mMainPageSlideApi.getSlideList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<SlideShowModel> slideShowModelList = JSON.parseArray(result.getData().getString("slideshow"), SlideShowModel.class);
                    mBannerView.setImages(getSlideUrlList(slideShowModelList))
                            .setImageLoader(new BannerImageLoader())
                            .setOnBannerListener(new OnBannerListener() {
                                @Override
                                public void OnBannerClick(int position) {
                                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                            .withString("url", slideShowModelList.get(position).getLinkURL())
                                            .greenChannel().navigation();
                                }
                            })
                            .start();
                }
            }
        });
    }

    private ArrayList<String> getSlideUrlList(List<SlideShowModel> slideShowModelList) {
        ArrayList<String> urlList = new ArrayList<>();
        for (SlideShowModel slideShowModel :
                slideShowModelList) {
            urlList.add(slideShowModel.getCoverURL());
        }

        return urlList;
    }

    private void initLevel() {
        if (MyUserInfoManager.getInstance().getUid() != 0) {
            UserInfoServerApi mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
            ApiMethods.subscribe(mUserInfoServerApi.getScoreDetail((int) MyUserInfoManager.getInstance().getUid()), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        List<UserLevelModel> userLevelModels = JSON.parseArray(result.getData().getString("userScore"), UserLevelModel.class);
                        // 展示段位信息
                        for (UserLevelModel userLevelModel : userLevelModels) {
                            if (userLevelModel.getType() == UserLevelModel.RANKING_TYPE) {
                                mRank = userLevelModel.getScore();
                            } else if (userLevelModel.getType() == UserLevelModel.SUB_RANKING_TYPE) {
                                mSubRank = userLevelModel.getScore();
                            } else if (userLevelModel.getType() == UserLevelModel.TOTAL_RANKING_STAR_TYPE) {
                                mStarNum = userLevelModel.getScore();
                            } else if (userLevelModel.getType() == UserLevelModel.REAL_RANKING_STAR_TYPE) {
                                mStarLimit = userLevelModel.getScore();
                            }
                        }
                        mLevelView.bindData(mRank, mSubRank, mStarLimit, mStarNum);
                    }
                }
            }, this);
        }
    }

    // 点击缩放动画
    public void clickAnimation(View view, final long tag) {
        U.getSoundUtils().play(TAG, R.raw.home_game);

        ObjectAnimator a1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator a4 = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(80);
        set.play(a1).with(a2);
        set.play(a3).with(a4).after(a1);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(mTag.contains(tag)){
                    jump(view, "onAnimationEnd");
                    mTag.remove(tag);
                    return;
                }

                mTag.add(tag);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    private void jump(View view, String from){
        MyLog.d(TAG, "jump" + " view=" + view + " from=" + from);
        if (view.getId() == R.id.iv_athletics_pk) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                    .withInt("key_game_type", GameModeType.GAME_MODE_CLASSIC_RANK)
                    .withBoolean("selectSong", true)
                    .navigation();
        } else if (view.getId() == R.id.iv_grab_game) {
            if (MyLog.isDebugLogOpen()) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                        .withInt("key_game_type", GameModeType.GAME_MODE_GRAB)
                        .withBoolean("selectSong", false)
                        .navigation();
            } else {
                U.getToastUtil().showShort("正在开发中，敬请期待");
            }
        }
    }

    private void checkGameConf(int mode, long tag, final View view) {

        ApiMethods.subscribe(mMainPageSlideApi.getGameConfig(mode), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "checkGameConf " + result.getErrno());
                if (result.getErrno() == 0) {
                    if(mTag.contains(tag)){
                        jump(view, "checkGameConf");
                        mTag.remove(tag);
                        return;
                    }

                    mTag.add(tag);
                } else {
                    MyLog.e(TAG, "checkGameConf failed, traceid is " + result.getTraceId());
                }
            }
        }, this);
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
        mUserTitleView.destroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        mUserTitleView.setData();
        initLevel();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(ScoreDetailChangeEvent scoreDetailChangeEvent) {
        mLevelView.bindData(scoreDetailChangeEvent.level, scoreDetailChangeEvent.subLevel,
                scoreDetailChangeEvent.totalStats, scoreDetailChangeEvent.selecStats);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}
