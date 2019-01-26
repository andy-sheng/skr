package com.module.home.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
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
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.core.myinfo.event.ScoreDetailChangeEvent;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.constans.GameModeType;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.MainPageSlideApi;
import com.module.home.R;
import com.module.home.model.GameConfModel;
import com.module.home.model.SlideShowModel;
import com.module.home.view.GameTimeTipsView;
import com.module.home.widget.UserInfoTitleView;
import com.module.rank.IRankingModeService;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.zq.dialog.PersonInfoDialogView;
import com.zq.level.utils.LevelConfigUtils;
import com.zq.level.view.NormalLevelView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

import static android.widget.RelativeLayout.ALIGN_RIGHT;

public class GameFragment extends BaseFragment {

    public final static String TAG = "GameFragment";

    public static final int STAR_BADGE = 1;
    public static final int TOP_BADGE = 2;
    public static final int SHANDIAN_BADGE = 3;

    RelativeLayout mTopArea;
    ImageView mAvatarBg;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;

    ImageView mMainRankIv;    // 主段位
    ImageView mSubRankIv;     // 子段位

    RelativeLayout mRankArea;
    ExImageView mMedalIv;     // 排名的勋章
    ExTextView mRankText;     // 显示的文本
    ExImageView mRankDiffIv;  // 上升下降的标识

    ExImageView mIvAthleticsPk;
    ExImageView mIvGrabGame;

    Banner mBannerView;
    ExImageView ivAthleticsPk;
    ExImageView mIvGrabPk;

    PopupWindow mPopupWindow;  // 显示上升或者下降的标识
    LinearLayout mPopArea;
    ExTextView mRankDiffTv;
    ImageView mRankDiffIcon;

    MainPageSlideApi mMainPageSlideApi;
    UserInfoServerApi mUserInfoServerApi;

    GameConfModel mGameConfModel;

    DialogPlus mDialogPlus;

    Vector<Long> mTag = new Vector<>();

    @Override
    public int initView() {
        return R.layout.game_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTopArea = (RelativeLayout) mRootView.findViewById(R.id.top_area);
        mAvatarBg = (ImageView) mRootView.findViewById(R.id.avatar_bg);
        mAvatarIv = (SimpleDraweeView) mRootView.findViewById(R.id.avatar_iv);

        mNameTv = (ExTextView) mRootView.findViewById(R.id.name_tv);
        mMainRankIv = (ImageView) mRootView.findViewById(R.id.main_rank_iv);
        mSubRankIv = (ImageView) mRootView.findViewById(R.id.sub_rank_iv);

        mRankArea = (RelativeLayout) mRootView.findViewById(R.id.rank_area);
        mMedalIv = (ExImageView) mRootView.findViewById(R.id.medal_iv);
        mRankText = (ExTextView) mRootView.findViewById(R.id.rank_text);
        mRankDiffIv = (ExImageView) mRootView.findViewById(R.id.rank_diff_iv);

        mBannerView = (Banner) mRootView.findViewById(R.id.banner_view);
        ivAthleticsPk = (ExImageView) mRootView.findViewById(R.id.iv_athletics_pk);
        mIvGrabPk = (ExImageView) mRootView.findViewById(R.id.iv_grab_game);

        LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.area_diff_popup_window_layout, null);
        mPopArea = (LinearLayout) linearLayout.findViewById(R.id.pop_area);
        mRankDiffIcon = (ImageView) linearLayout.findViewById(R.id.rank_diff_icon);
        mRankDiffTv = (ExTextView) linearLayout.findViewById(R.id.rank_diff_tv);

        mPopupWindow = new PopupWindow(linearLayout);
        mPopupWindow.setOutsideTouchable(true);

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

        if(MyLog.isDebugLogOpen()) {
            RxView.longClicks(mIvGrabPk)
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                                    .navigation();
                        }
                    });
        }

        RxView.clicks(mRankArea).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
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
            }
        });

        U.getSoundUtils().preLoad(TAG, R.raw.home_game, R.raw.general_button);

        initBaseInfo();
        initRankLevel();
        initOperationArea();
    }

    private void initBaseInfo() {
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                        .setCircle(true)
                        .setBorderColor(Color.parseColor("#0C2275"))
                        .setBorderWidth(U.getDisplayUtils().dip2px(3))
                        .build());
        mNameTv.setText(MyUserInfoManager.getInstance().getNickName());
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
        initRankLevel();
        initOperationArea();
    }

    private void initRankLevel() {
        if (mUserInfoServerApi == null) {
            mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        }
        ApiMethods.subscribe(mUserInfoServerApi.getReginDiff(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    UserRankModel userRankModel = JSON.parseObject(result.getData().getString("diff"), UserRankModel.class);
                    showRankView(userRankModel);
                }
            }
        });
    }

    private void showRankView(UserRankModel userRankModel) {
        MyLog.d(TAG, "showRankView" + " userRankModel=" + userRankModel);
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
            mMedalIv.setImageResource(R.drawable.paiming);
        } else if (userRankModel.getBadge() == TOP_BADGE) {
            mMedalIv.setImageResource(R.drawable.paihang);
        } else if (userRankModel.getBadge() == SHANDIAN_BADGE) {
            mMedalIv.setImageResource(R.drawable.dabai);
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
            mPopupWindow.showAsDropDown(mRankText);

        } else {
            content = "下降" + Math.abs(diff) + "名";
            mRankDiffTv.setText(content);
            mPopArea.setBackground(getResources().getDrawable(R.drawable.xiajiang_bj));
            mRankDiffIcon.setImageResource(R.drawable.xiajiang_cry);

            mPopupWindow.setWidth(U.getDisplayUtils().dip2px(36) + content.length() * U.getDisplayUtils().dip2px(10));
            mPopupWindow.setHeight(U.getDisplayUtils().dip2px(31));
            mPopupWindow.showAsDropDown(mRankText);
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
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi.class);
        ApiMethods.subscribe(mMainPageSlideApi.getSlideList(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mBannerView.setVisibility(View.VISIBLE);
                    List<SlideShowModel> slideShowModelList = JSON.parseArray(result.getData().getString("slideshow"), SlideShowModel.class);
                    mBannerView.setImages(getSlideUrlList(slideShowModelList))
                            .setImageLoader(new BannerImageLoader())
                            .setOnBannerListener(new OnBannerListener() {
                                @Override
                                public void OnBannerClick(int position) {
                                    ARouter.getInstance().build(RouterConstants.ACTIVITY_SHARE_WEB)
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
                if (!mTag.contains(tag)) {
                    mTag.add(tag);
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
        if(mGameConfModel != null && mGameConfModel.isIsSupport() && !mGameConfModel.getDetail().isIsOpen()){
//        if (!MyLog.isDebugLogOpen()) {
            GameTimeTipsView gameTimeTipsView = new GameTimeTipsView(getActivity());
            gameTimeTipsView.setGameConfModel(mGameConfModel);

            mDialogPlus = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(gameTimeTipsView))
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

        ApiMethods.subscribe(mMainPageSlideApi.getGameConfig(mode), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() != 0) {
                    MyLog.w(TAG, "checkGameConf faild, traceid is " + result.getTraceId());
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
        }, this);
    }

    @Override
    public void destroy() {
        super.destroy();
        mPopupWindow.dismiss();
        U.getSoundUtils().release(TAG);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvnet(MyUserInfoEvent.UserInfoChangeEvent userInfoChangeEvent) {
        initBaseInfo();
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
}
