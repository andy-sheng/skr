package com.module.home.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.banner.BannerImageLoader;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.account.UserAccountManager;
import com.common.core.account.event.AccountEvent;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.permission.SkrLocationPermission;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserRankModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.BuildConfig;
import com.module.home.MainPageSlideApi;
import com.module.home.R;
import com.module.home.model.GameConfModel;
import com.module.home.model.GameKConfigModel;
import com.module.home.model.SlideShowModel;
import com.module.home.view.GameTimeTipsView;
import com.module.home.widget.UserInfoTitleView;
import com.module.rank.IRankingModeService;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.header.FalsifyHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;
import com.zq.level.utils.LevelConfigUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.functions.Consumer;

public class GameFragment extends BaseFragment {

    public final static String TAG = "GameFragment";

    public static final int STAR_BADGE = 1;
    public static final int TOP_BADGE = 2;
    public static final int SHANDIAN_BADGE = 3;

    RelativeLayout mTopArea;
    UserInfoTitleView mUserInfoTitle;

    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;

    ImageView mMainRankIv;    // 主段位
    ImageView mSubRankIv;     // 子段位

    RelativeLayout mRankArea;
    ExImageView mMedalIv;     // 排名的勋章
    ExTextView mRankText;     // 显示的文本
    ExImageView mRankDiffIv;  // 上升下降的标识

    Banner mBannerView;
    ExImageView mIvGrabGame;
    ExTextView mTextGrabGame;
    ExImageView mIvAthleticsPk;
    ExTextView mTextAthleticsPk;

    PopupWindow mPopupWindow;  // 显示上升或者下降的标识
    LinearLayout mPopArea;
    ExTextView mRankDiffTv;
    ImageView mRankDiffIcon;

    MainPageSlideApi mMainPageSlideApi;
    UserInfoServerApi mUserInfoServerApi;

    GameConfModel mGameConfModel;

    DialogPlus mDialogPlus;
    BaseImageView mIvOpFirst;

    SmartRefreshLayout mSmartRefreshLayout;
    ClassicsHeader mClassicsHeader;

    Vector<Long> mTag = new Vector<>();

    @Override
    public int initView() {
        return R.layout.game_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTopArea = (RelativeLayout) mRootView.findViewById(R.id.top_area);
        mUserInfoTitle = (UserInfoTitleView) mRootView.findViewById(R.id.user_info_title);
        mAvatarBg = (ImageView) mRootView.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);
        mIvOpFirst = (BaseImageView) mRootView.findViewById(R.id.iv_op_first);

        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mMainRankIv = (ImageView) mRootView.findViewById(R.id.main_rank_iv);
        mSubRankIv = (ImageView) mRootView.findViewById(R.id.sub_rank_iv);

        mRankArea = (RelativeLayout) mRootView.findViewById(R.id.rank_area);
        mMedalIv = (ExImageView) mRootView.findViewById(R.id.medal_iv);
        mRankText = (ExTextView) mRootView.findViewById(R.id.rank_text);
        mRankDiffIv = (ExImageView) mRootView.findViewById(R.id.rank_diff_iv);

        mBannerView = (Banner) mRootView.findViewById(R.id.banner_view);
        mIvGrabGame = (ExImageView) mRootView.findViewById(R.id.iv_grab_game);
        mTextGrabGame = (ExTextView) mRootView.findViewById(R.id.text_grab_game);
        mIvAthleticsPk = (ExImageView) mRootView.findViewById(R.id.iv_athletics_pk);
        mTextAthleticsPk = (ExTextView) mRootView.findViewById(R.id.text_athletics_pk);

        LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.area_diff_popup_window_layout, null);
        mPopArea = (LinearLayout) linearLayout.findViewById(R.id.pop_area);
        mRankDiffIcon = (ImageView) linearLayout.findViewById(R.id.rank_diff_icon);
        mRankDiffTv = (ExTextView) linearLayout.findViewById(R.id.rank_diff_tv);
        mSmartRefreshLayout = mRootView.findViewById(R.id.smart_refresh_layout);
        mClassicsHeader = (ClassicsHeader) mRootView.findViewById(R.id.classics_header);
        mPopupWindow = new PopupWindow(linearLayout);
        mPopupWindow.setOutsideTouchable(true);

        mSmartRefreshLayout.setEnableRefresh(true);
        mSmartRefreshLayout.setEnableLoadMore(false);
        mSmartRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mSmartRefreshLayout.setEnableOverScrollDrag(true);
        mSmartRefreshLayout.setRefreshHeader(mClassicsHeader);
        mSmartRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                initRankLevel();
                initOperationArea();
            }
        });


        mIvAthleticsPk.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                long tag = System.currentTimeMillis();
                checkGameConf(1, tag, mIvAthleticsPk);
                clickAnimation(mIvAthleticsPk, mTextAthleticsPk, tag);
                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_HOME),
                        StatConstants.KEY_RANK_CLICK, null);
            }
        });

        mIvGrabGame.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                long tag = System.currentTimeMillis();
                checkGameConf(3, tag, mIvGrabGame);
                clickAnimation(mIvGrabGame, mTextGrabGame, tag);
                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_HOME),
                        StatConstants.KEY_GRAB_CLICK, null);
            }
        });

        if (BuildConfig.DEBUG) {
            mIvAthleticsPk.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_VOICEROOM)
                            .navigation();
                    return false;
                }
            });
        }

        if (BuildConfig.DEBUG) {
            mIvGrabGame.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                            .navigation();
                    return false;
                }
            });
        }
        mRankArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                IRankingModeService iRankingModeService = (IRankingModeService) ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation();
                Class<BaseFragment> baseFragment = (Class<BaseFragment>) iRankingModeService.getLeaderboardFragmentClass();
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), baseFragment)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                            }
                        })
                        .build());
                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_HOME), "ranklist_click", null);
            }
        });

        U.getSoundUtils().preLoad(TAG, R.raw.home_game, R.raw.general_button);

        initBaseInfo();
        initRankLevel();
        initOperationArea();
        initGameKConfig();
    }

    private void initBaseInfo() {
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderColor(Color.parseColor("#0C2275"))
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .build());
        mNameTv.setText(MyUserInfoManager.getInstance().getNickName());

        mUserInfoTitle.showBaseInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onFragmentInvisible() {
        super.onFragmentInvisible();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        initRankLevel();
        initOperationArea();
        initGameKConfig();
        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_HOME), StatConstants.KEY_EXPOSE, null);
    }

    private void initRankLevel() {
        if (mUserInfoServerApi == null) {
            mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        }
        ApiMethods.subscribe(mUserInfoServerApi.getReginDiff(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mSmartRefreshLayout.finishRefresh();
                if (result.getErrno() == 0) {
                    UserRankModel userRankModel = JSON.parseObject(result.getData().getString("diff"), UserRankModel.class);
                    showRankView(userRankModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
                mSmartRefreshLayout.finishRefresh();
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                mSmartRefreshLayout.finishRefresh();
                U.getToastUtil().showShort("网络超时");
            }
        });
    }

    private void showRankView(UserRankModel userRankModel) {
        MyLog.d(TAG, "showRankView" + " userRankModel=" + userRankModel);
        mUserInfoTitle.showRankView(userRankModel);
        mClassicsHeader.setBackgroundColor(Color.parseColor(LevelConfigUtils.getHomePageTopBgColor(userRankModel.getMainRanking())));
        mMainRankIv.setImageResource(LevelConfigUtils.getImageResoucesLevel(userRankModel.getMainRanking()));
        mSubRankIv.setImageResource(LevelConfigUtils.getImageResoucesSubLevel(userRankModel.getMainRanking(), userRankModel.getSubRanking()));

        if (userRankModel.getDiff() == 0) {
            // 默认按照上升显示
            mRankDiffIv.setVisibility(View.GONE);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), true));
        } else if (userRankModel.getDiff() > 0) {
            mRankDiffIv.setVisibility(View.VISIBLE);
            mRankDiffIv.setImageResource(R.drawable.shangsheng_ic);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), true));
        } else if (userRankModel.getDiff() < 0) {
            mRankDiffIv.setVisibility(View.VISIBLE);
            mRankDiffIv.setImageResource(R.drawable.xiajiang_ic);
            mRankText.setText(highlight(userRankModel.getText(), userRankModel.getHighlight(), false));
        }

        showPopWindow(userRankModel.getDiff());

        if (userRankModel.getBadge() == STAR_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.paiming));
        } else if (userRankModel.getBadge() == TOP_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.paihang));
        } else if (userRankModel.getBadge() == SHANDIAN_BADGE) {
            mMedalIv.setBackground(getResources().getDrawable(R.drawable.dabai));
        }
    }

    private void showPopWindow(int diff) {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }

        if (diff == 0) {
            return;
        }

        String content = "";
        if (diff > 0) {
            content = "上升" + diff + "名";
            mRankDiffTv.setText(content);
            mPopArea.setBackground(getResources().getDrawable(R.drawable.shangsheng_bj));
            mRankDiffIcon.setImageResource(R.drawable.shangsheng_smail);

            mPopupWindow.setWidth(U.getDisplayUtils().dip2px(36) + content.length() * U.getDisplayUtils().dip2px(10));
            mPopupWindow.setHeight(U.getDisplayUtils().dip2px(31));
            mRankText.post(new Runnable() {
                @Override
                public void run() {
                    if (GameFragment.this.fragmentVisible) {
                        mPopupWindow.showAsDropDown(mRankText);
                    }
                }
            });
        } else {
            content = "下降" + Math.abs(diff) + "名";
            mRankDiffTv.setText(content);
            mPopArea.setBackground(getResources().getDrawable(R.drawable.xiajiang_bj));
            mRankDiffIcon.setImageResource(R.drawable.xiajiang_cry);

            mPopupWindow.setWidth(U.getDisplayUtils().dip2px(36) + content.length() * U.getDisplayUtils().dip2px(10));
            mPopupWindow.setHeight(U.getDisplayUtils().dip2px(31));
            mRankText.post(new Runnable() {
                @Override
                public void run() {
                    if (GameFragment.this.fragmentVisible) {
                        mPopupWindow.showAsDropDown(mRankText);
                    }
                }
            });
        }

    }

    private SpannableString highlight(String text, String target, boolean isUp) {
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile(target);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#FF3B3C"));
            spannableString.setSpan(span, matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    private void initOperationArea() {
        String slideshow = U.getPreferenceUtils().getSettingString("slideshow", "");
        if (!TextUtils.isEmpty(slideshow)) {
            try {
                List<SlideShowModel> slideShowModelList = JSON.parseArray(slideshow, SlideShowModel.class);
                setBannerImage(slideShowModelList);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }

        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        ApiMethods.subscribe(mMainPageSlideApi.getSlideList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mSmartRefreshLayout.finishRefresh();
                if (result.getErrno() == 0) {
                    List<SlideShowModel> slideShowModelList = JSON.parseArray(result.getData().getString("slideshow"), SlideShowModel.class);
                    U.getPreferenceUtils().setSettingString("slideshow", result.getData().getString("slideshow"));
                    setBannerImage(slideShowModelList);
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
                mSmartRefreshLayout.finishRefresh();
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络超时");
                mSmartRefreshLayout.finishRefresh();
            }
        });
    }

    private void initGameKConfig() {
        if (mTextGrabGame.getVisibility() == View.VISIBLE || mTextAthleticsPk.getVisibility() == View.VISIBLE) {
            return;
        }

        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        ApiMethods.subscribe(mMainPageSlideApi.getKConfig(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    GameKConfigModel gameKConfigModel = JSON.parseObject(result.getData().getString("common"), GameKConfigModel.class);
                    showGameKConfig(gameKConfigModel);

                    GameKConfigModel.HomepagesitefirstBean homepagesitefirstBean = gameKConfigModel.getHomepagesitefirst();
                    if (homepagesitefirstBean != null && homepagesitefirstBean.isEnable()) {
                        AvatarUtils.loadAvatarByUrl(mIvOpFirst,
                                AvatarUtils.newParamsBuilder(homepagesitefirstBean.getPic())
                                        .setWidth(U.getDisplayUtils().dip2px(48f))
                                        .setHeight(U.getDisplayUtils().dip2px(53f))
                                        .build());
                        mIvOpFirst.setVisibility(View.VISIBLE);
                        mIvOpFirst.setOnClickListener(new DebounceViewClickListener() {
                            @Override
                            public void clickValid(View v) {
                                ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                                        .withString("uri", homepagesitefirstBean.getSchema())
                                        .navigation();
                            }
                        });
                    } else {
                        MyLog.w(TAG, "initGameKConfig first operation area is empty");
                        mIvOpFirst.setVisibility(View.GONE);
                    }
                } else {
                    MyLog.w(TAG, "initGameKConfig failed, " + " traceid is " + result.getErrno());
                    mIvOpFirst.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络异常");
                mSmartRefreshLayout.finishRefresh();
            }
        });
    }

    private void showGameKConfig(GameKConfigModel gameKConfigModel) {
        String pkTags = "";
        String standTags = "";
        if (gameKConfigModel != null) {
            if (gameKConfigModel.getPktags() != null && gameKConfigModel.getPktags().size() > 0) {
                pkTags = gameKConfigModel.getPktags().get(0);
            }

            if (gameKConfigModel.getStandtags() != null && gameKConfigModel.getStandtags().size() > 0) {
                standTags = gameKConfigModel.getStandtags().get(0);
            }
        }

        if (!TextUtils.isEmpty(pkTags)) {
            mTextAthleticsPk.setVisibility(View.VISIBLE);
            mTextAthleticsPk.setText(pkTags);
        }

        if (!TextUtils.isEmpty(standTags)) {
            mTextGrabGame.setVisibility(View.VISIBLE);
            mTextGrabGame.setText(standTags);
        }

    }

    private void setBannerImage(List<SlideShowModel> slideShowModelList) {
        if (slideShowModelList == null || slideShowModelList.size() == 0) {
            MyLog.w(TAG, "initOperationArea 为null");
            return;
        }

        MyLog.d(TAG, "initOperationArea " + slideShowModelList.get(0).getCoverURL());
        mBannerView.setIndicatorGravity(BannerConfig.RIGHT);
        mBannerView.setImages(getSlideUrlList(slideShowModelList))
                .setImageLoader(new BannerImageLoader())
                .setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(int position) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                                .withString("uri", slideShowModelList.get(position).getSchema())
                                .navigation();
                    }
                })
                .start();
    }

    private ArrayList<String> getSlideUrlList(List<SlideShowModel> slideShowModelList) {
        ArrayList<String> urlList = new ArrayList<>();
        for (SlideShowModel slideShowModel :
                slideShowModelList) {
            urlList.add(slideShowModel.getCoverURL());
        }

        return urlList;
    }

    // 点击缩放动画
    public void clickAnimation(View view, View viewcard, final long tag) {
        U.getSoundUtils().play(TAG, R.raw.home_game);

        AnimatorSet set = new AnimatorSet();

        ObjectAnimator a1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator a4 = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);

        if (viewcard.getVisibility() == View.VISIBLE) {
            ObjectAnimator a5 = ObjectAnimator.ofFloat(viewcard, "scaleX", 1f, 0.95f);
            ObjectAnimator a6 = ObjectAnimator.ofFloat(viewcard, "scaleY", 1f, 0.95f);
            ObjectAnimator a7 = ObjectAnimator.ofFloat(viewcard, "scaleX", 0.95f, 1f);
            ObjectAnimator a8 = ObjectAnimator.ofFloat(viewcard, "scaleY", 0.95f, 1f);
            set.play(a1).with(a2).with(a5).with(a6);
            set.play(a3).with(a4).with(a7).with(a8).after(a1);
        } else {
            set.play(a1).with(a2);
            set.play(a3).with(a4).after(a1);
        }
        set.setDuration(80);
        set.start();


        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!mTag.contains(tag)) {
                    mTag.add(tag);
                    //U.getToastUtil().showShort("您的网络有延迟");
                    return;
                }

                mTag.remove(tag);

                if (isGameOpen()) {
                    jump(view, "onAnimationEnd");
                }
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

    private boolean isGameOpen() {
        //mGameConfModel不应该为null，加个保护
        if (mGameConfModel != null && mGameConfModel.isIsSupport() && !mGameConfModel.getDetail().isIsOpen()) {
//        if (!MyLog.isDebugLogOpen()) {
            GameTimeTipsView gameTimeTipsView = new GameTimeTipsView(getActivity());
            gameTimeTipsView.setGameConfModel(mGameConfModel);

            mDialogPlus = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(gameTimeTipsView))
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.transparent)
                    .setGravity(Gravity.CENTER)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                            mDialogPlus.dismiss();
                        }
                    }).create();

            mDialogPlus.show();
            return false;
        }

        return true;
    }

    private void jump(View view, String from) {
        MyLog.d(TAG, "jump" + " view=" + view + " from=" + from);
        if (view.getId() == R.id.iv_athletics_pk) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                    .withInt("key_game_type", GameModeType.GAME_MODE_CLASSIC_RANK)
                    .withBoolean("selectSong", true)
                    .navigation();
        } else if (view.getId() == R.id.iv_grab_game) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                    .withInt("key_game_type", GameModeType.GAME_MODE_GRAB)
                    .withBoolean("selectSong", false)
                    .navigation();
        }
    }

    private void checkGameConf(int mode, long tag, final View view) {
        ApiMethods.subscribe(mMainPageSlideApi.getGameConfig(mode, true), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() != 0) {
                    MyLog.w(TAG, "checkGameConf faild, traceid is " + result.getTraceId());
                    U.getToastUtil().showShort(result.getErrmsg());
                    return;
                }
                GameConfModel gameConfModel = JSON.parseObject(result.getData().toString(), GameConfModel.class);
                //说明接口的数据在500毫秒内拉到的
                if (!mTag.contains(tag)) {
                    mGameConfModel = gameConfModel;
                    mTag.add(tag);
                    return;
                }
                mTag.remove(tag);
                if (isGameOpen()) {
                    jump(view, "checkGameConf");
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络异常");
            }
        }, this);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
        U.getSoundUtils().release(TAG);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        initBaseInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEvent.SetAccountEvent event) {
        initRankLevel();
        initOperationArea();
        initGameKConfig();
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvnet(ScoreDetailChangeEvent scoreDetailChangeEvent) {
//        mLevelView.bindData(scoreDetailChangeEvent.level, scoreDetailChangeEvent.subLevel,
//                scoreDetailChangeEvent.totalStats, scoreDetailChangeEvent.selecStats);
//    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public boolean isInViewPager() {
        return true;
    }
}
