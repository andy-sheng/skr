package com.moudule.playways.beauty.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ExViewStub;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.viewpager.SlidingTabLayout;
import com.module.playways.R;
import com.zq.mediaengine.effect.DyEffectResManager;
import com.zq.mediaengine.kit.ZqEngineKit;
import com.zq.mediaengine.kit.bytedance.BytedEffectFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BeautyControlPanelView extends ExViewStub implements BeautyFiterStickerView.Listener {


    public static final int TYPE_BEAUTY = 1;     // 美颜
    public static final int TYPE_FITER = 2;     // 滤镜
    public static final int TYPE_STICKER = 3;      // 贴纸

    SlidingTabLayout mBeautyTitleStl;
    PagerAdapter mPagerAdapter;
    ViewPager mBeautyVp;
    Animator mShowOrHideAnimator;
    View mPlaceHolderView;

    private BeautyFiterStickerView mBeautyView;
    private BeautyFiterStickerView mFiterView;
    private BeautyFiterStickerView mStickerView;

    private DyEffectResManager mDyEffectResManager = new DyEffectResManager();

    public BeautyControlPanelView(ViewStub viewStub) {
        super(viewStub);
    }

    @Override
    protected void init(View parentView) {
        mBeautyTitleStl = mParentView.findViewById(R.id.beauty_title_stl);
        mBeautyTitleStl.setCustomTabView(R.layout.beauty_tab_view, R.id.tab_tv);
        mBeautyTitleStl.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20));
        mBeautyTitleStl.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mBeautyTitleStl.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE);
        mBeautyTitleStl.setIndicatorWidth(U.getDisplayUtils().dip2px(56f));
        mBeautyTitleStl.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(13f));
        mBeautyTitleStl.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f));
        mBeautyTitleStl.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f));

        mBeautyVp = mParentView.findViewById(R.id.beauty_vp);
        mPagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return 3;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                if (position == 0) {
                    // 美颜
                    if (mBeautyView == null) {
                        mBeautyView = new BeautyFiterStickerView(mBeautyVp.getContext(), TYPE_BEAUTY, getViewModel(TYPE_BEAUTY), BeautyControlPanelView.this);
                    }
                    if (container.indexOfChild(mBeautyView) == -1) {
                        container.addView(mBeautyView);
                    }
                    return mBeautyView;
                } else if (position == 1) {
                    // 滤镜
                    if (mFiterView == null) {
                        mFiterView = new BeautyFiterStickerView(mBeautyVp.getContext(), TYPE_FITER, getViewModel(TYPE_FITER), BeautyControlPanelView.this);
                    }
                    if (container.indexOfChild(mFiterView) == -1) {
                        container.addView(mFiterView);
                    }
                    return mFiterView;
                } else if (position == 2) {
                    // 贴纸
                    if (mStickerView == null) {
                        mStickerView = new BeautyFiterStickerView(mBeautyVp.getContext(), TYPE_STICKER, getViewModel(TYPE_STICKER), BeautyControlPanelView.this);
                    }
                    if (container.indexOfChild(mStickerView) == -1) {
                        container.addView(mStickerView);
                    }
                    return mStickerView;
                }
                return super.instantiateItem(container, position);
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) {
                    return "美颜";
                } else if (position == 1) {
                    return "滤镜";
                } else if (position == 2) {
                    return "贴纸";
                }
                return "";
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        };

        mBeautyVp.setAdapter(mPagerAdapter);
        mBeautyTitleStl.setViewPager(mBeautyVp);
        mPagerAdapter.notifyDataSetChanged();
        mPlaceHolderView = mParentView.findViewById(R.id.place_holder_view);
        mPlaceHolderView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                hide();
            }
        });

        mDyEffectResManager.tryLoadRes(new DyEffectResManager.Callback() {
            @Override
            public void onResReady(String modelDir, String licensePath) {
                /**
                 * 初始化抖音特效
                 */
                BytedEffectFilter effectFilter = ZqEngineKit.getInstance().getBytedEffectFilter();
                if (effectFilter != null) {
                    effectFilter.init(U.app(), modelDir, licensePath);
                }
                String beautyResPath = mDyEffectResManager.getBeautyResPath()[0].getPath();
                effectFilter.setBeauty(beautyResPath);
                String reshapeResPath = mDyEffectResManager.getReshapeResPath()[0].getPath();
                effectFilter.setReshape(reshapeResPath);

            }
        });
    }

    @Override
    protected int layoutDesc() {
        return R.layout.beauty_control_panel_view_stub_layout;
    }


    List<BeautyViewModel> getViewModel(int type) {
        List mList = new ArrayList<BeautyViewModel>();
        if (type == TYPE_BEAUTY) {
            mList.add(new BeautyViewModel(Type.dayan, getDrawable("#90DAFF", "#72C1E9")));
            mList.add(new BeautyViewModel(Type.shoulian, getDrawable("#FFB1CF", "#DF8BAB")));
            mList.add(new BeautyViewModel(Type.mopi, getDrawable("#F9CC82", "#D79F43")));
            mList.add(new BeautyViewModel(Type.meibai, getDrawable("#C7E4AC", "#A1C580")));
            mList.add(new BeautyViewModel(Type.ruihua, getDrawable("#D7ABEE", "#BB81CF")));
        } else if (type == TYPE_FITER) {
            mList.add(new BeautyViewModel(Type.ruanyang, getDrawable("#90DAFF", "#72C1E9")));
            mList.add(new BeautyViewModel(Type.musi, getDrawable("#FFB1CF", "#DF8BAB")));
            mList.add(new BeautyViewModel(Type.yangqi, getDrawable("#F9CC82", "#D79F43")));
//            mList.add(new BeautyViewModel(4, "经典", getDrawable("#C7E4AC", "#A1C580")));
        } else if (type == TYPE_STICKER) {
            mList.add(new BeautyViewModel(Type.xiaogou, getDrawable("#90DAFF", "#72C1E9")));
//            mList.add(new BeautyViewModel(2, "耳朵", getDrawable("#FFB1CF", "#DF8BAB")));
//            mList.add(new BeautyViewModel(3, "小狗", getDrawable("#F9CC82", "#D79F43")));
        }
        return mList;
    }

    Drawable getDrawable(String solidColor, String strokeColor) {
        return new DrawableCreator.Builder()
                .setSolidColor(Color.parseColor(solidColor))
                .setStrokeColor(Color.parseColor(strokeColor))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2f))
                .setCornersRadius(U.getDisplayUtils().dip2px(22f))
                .build();
    }


    public void show() {
        tryInflate();
        if (mShowOrHideAnimator != null) {
            mShowOrHideAnimator.cancel();
        }

        mShowOrHideAnimator = ObjectAnimator.ofFloat(mParentView, View.TRANSLATION_Y, mParentView.getHeight(), 0f);
        mShowOrHideAnimator.setDuration(300);
        mShowOrHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mParentView.setVisibility(View.VISIBLE);
            }
        });
        mShowOrHideAnimator.start();
    }

    void hide() {
        //tryInflate()
        if (mShowOrHideAnimator != null) {
            mShowOrHideAnimator.cancel();
        }

        mShowOrHideAnimator = ObjectAnimator.ofFloat(mParentView, View.TRANSLATION_Y, 0f, mParentView.getHeight());
        mShowOrHideAnimator.setDuration(300);
        mShowOrHideAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mParentView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                mParentView.setVisibility(View.GONE);
            }
        });
        mShowOrHideAnimator.start();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        mShowOrHideAnimator.cancel();
    }

    public boolean onBackPressed() {
        if (mParentView != null && mParentView.getVisibility() == View.VISIBLE) {
            hide();
            return true;
        }
        return false;
    }


//    /**
//     * 调节美白
//     */
//    BeautyWhite(1),
//    /**
//     * 调节磨皮
//     */
//    BeautySmooth(2),
//    /**
//     * 同时调节瘦脸和大眼
//     */
//    FaceReshape(3),
//    /**
//     * 调节锐化
//     */
//    BeautySharp(9),
//    /**
//     * 唇色
//     */
//    MakeUpLip(17),
//    /**
//     * 腮红
//     */
//    MakeUpBlusher(18);


    @Override
    public void onChangeBeauty(Type type, int progress) {
        BytedEffectFilter filter = ZqEngineKit.getInstance().getBytedEffectFilter();
        switch (type) {
            case dayan:
                filter.updateReshape(progress / 100.0f, progress / 100.0f);
                break;
            case shoulian:
                filter.updateReshape(progress / 100.0f, progress / 100.0f);
                break;
            case mopi:
                filter.updateIntensity(BytedEffectConstants.IntensityType.BeautySmooth.getId(), progress / 100.0f);
                break;
            case meibai:
                filter.updateIntensity(BytedEffectConstants.IntensityType.BeautyWhite.getId(), progress / 100.0f);
                break;
            case ruihua:
                filter.updateIntensity(BytedEffectConstants.IntensityType.BeautySharp.getId(), progress / 100.0f);
                break;
//            case ruanyang:
//                break;
//            case musi:
//                break;
//            case yangqi:
//                break;
//            case xiaogou:
//                break;
        }
    }

    @Override
    public void onChangeFiter(Type type, int progress) {
        BytedEffectFilter filter = ZqEngineKit.getInstance().getBytedEffectFilter();
        switch (type) {
            case ruanyang:
                filter.setFilter(mDyEffectResManager.getFilterResources()[0].getPath());
                filter.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), progress / 100.0f);
                break;
            case musi:
                filter.setFilter(mDyEffectResManager.getFilterResources()[1].getPath());
                filter.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), progress / 100.0f);
                break;
            case yangqi:
                filter.setFilter(mDyEffectResManager.getFilterResources()[2].getPath());
                filter.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), progress / 100.0f);
                break;
        }
    }

    @Override
    public void onChangePater(Type type) {
        BytedEffectFilter filter = ZqEngineKit.getInstance().getBytedEffectFilter();
        switch (type) {
            case xiaogou:
                File file = new File(mDyEffectResManager.getStickersPath(), "black_cat");
                filter.setSticker(file.getPath());
                break;
        }
    }

    public static class BeautyViewModel {
        Type mType;
        Drawable drawable;

        public BeautyViewModel(Type type, Drawable drawable) {
            mType = type;
            this.drawable = drawable;
        }

        public Type getType() {
            return mType;
        }

        public Drawable getDrawable() {
            return drawable;
        }
    }

    public enum Type {
        dayan(TYPE_BEAUTY, "大眼"), shoulian(TYPE_BEAUTY, "瘦脸"), mopi(TYPE_BEAUTY, "磨皮"), meibai(TYPE_BEAUTY, "美白"), ruihua(TYPE_BEAUTY, "锐化"),
        ruanyang(TYPE_FITER, "暖阳"), musi(TYPE_FITER, "慕斯"), yangqi(TYPE_FITER, "氧气"),
        xiaogou(TYPE_STICKER, "小狗");

        int classify;
        String name;

        Type(int type, String name) {
            this.classify = type;
            this.name = name;
        }

        public int getClassify() {
            return classify;
        }

        public String getName() {
            return name;
        }
    }
}
