package com.moudule.playways.beauty.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import com.common.view.viewpager.SlidingTabLayout;
import com.engine.Params;
import com.module.playways.R;
import com.zq.mediaengine.kit.ZqEngineKit;
import com.zq.mediaengine.kit.bytedance.BytedEffectFilter;

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

    public BeautyControlPanelView(ViewStub viewStub) {
        super(viewStub);
    }

    @Override
    protected void init(View parentView) {
        mBeautyTitleStl = getMParentView().findViewById(R.id.beauty_title_stl);
        mBeautyTitleStl.setCustomTabView(R.layout.beauty_tab_view, R.id.tab_tv);
        mBeautyTitleStl.setSelectedIndicatorColors(U.getColor(R.color.black_trans_20));
        mBeautyTitleStl.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        mBeautyTitleStl.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_NONE);
        mBeautyTitleStl.setIndicatorWidth(U.getDisplayUtils().dip2px(56f));
        mBeautyTitleStl.setIndicatorBottomMargin(U.getDisplayUtils().dip2px(13f));
        mBeautyTitleStl.setSelectedIndicatorThickness(U.getDisplayUtils().dip2px(24f));
        mBeautyTitleStl.setIndicatorCornorRadius(U.getDisplayUtils().dip2px(12f));

        mBeautyVp = getMParentView().findViewById(R.id.beauty_vp);
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
        mPlaceHolderView = getMParentView().findViewById(R.id.place_holder_view);
        mPlaceHolderView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                hide();
            }
        });
        mBeautyVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    // 美颜
                    if (mBeautyView != null) {
                        mBeautyView.onPageSelected();
                    }
                } else if (position == 1) {
                    // 滤镜
                    if (mFiterView != null) {
                        mFiterView.onPageSelected();
                    }
                } else if (position == 2) {
                    // 贴纸
                    if (mStickerView != null) {
                        mStickerView.onPageSelected();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_beauty_control_panel_view_stub_layout;
    }


    List<BeautyViewModel> getViewModel(int type) {
        List mList = new ArrayList<BeautyViewModel>();
        if (type == TYPE_BEAUTY) {
            mList.add(new BeautyViewModel(Type.dayan, "#72C1E9", 0));
            mList.add(new BeautyViewModel(Type.shoulian, "#DF8BAB", 0));
            mList.add(new BeautyViewModel(Type.mopi, "#D79F43", 0));
            mList.add(new BeautyViewModel(Type.meibai, "#A1C580", 0));
            mList.add(new BeautyViewModel(Type.ruihua, "#BB81CF", 0));
        } else if (type == TYPE_FITER) {
            mList.add(new BeautyViewModel(Type.none_filter, "#72C1E9", 0));
            mList.add(new BeautyViewModel(Type.ruanyang, "#DF8BAB", 0));
            mList.add(new BeautyViewModel(Type.musi, "#D79F43", 0));
            mList.add(new BeautyViewModel(Type.yangqi, "#A1C580", 0));
        } else if (type == TYPE_STICKER) {
            mList.add(new BeautyViewModel(Type.none_sticker, null, R.drawable.sticker_no_icon));
            mList.add(new BeautyViewModel(Type.cat, null, R.drawable.sticker_cat_icon));
            mList.add(new BeautyViewModel(Type.pump, null, R.drawable.sticker_pump_icon));
            mList.add(new BeautyViewModel(Type.rabbit, null, R.drawable.sticker_rabbit_icon));
        }
        return mList;
    }

    public void show() {
        tryInflate();
        if (mShowOrHideAnimator != null) {
            mShowOrHideAnimator.cancel();
        }

        mShowOrHideAnimator = ObjectAnimator.ofFloat(getMParentView(), View.TRANSLATION_Y, getMParentView().getHeight(), 0f);
        mShowOrHideAnimator.setDuration(300);
        mShowOrHideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getMParentView().setVisibility(View.VISIBLE);
            }
        });
        mShowOrHideAnimator.start();
    }

    public void enableHide(boolean enable) {
        if (enable) {
            mPlaceHolderView.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    hide();
                }
            });
        } else {
            mPlaceHolderView.setClickable(false);
        }
    }

    void hide() {
        //tryInflate()
        if (mShowOrHideAnimator != null) {
            mShowOrHideAnimator.cancel();
        }

        mShowOrHideAnimator = ObjectAnimator.ofFloat(getMParentView(), View.TRANSLATION_Y, 0f, getMParentView().getHeight());
        mShowOrHideAnimator.setDuration(300);
        mShowOrHideAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                getMParentView().setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getMParentView().setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                getMParentView().setVisibility(View.GONE);
            }

        });
        mShowOrHideAnimator.start();
        Params config = ZqEngineKit.getInstance().getParams();
        if (config != null) {
            Params.save2Pref(config);
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        if (mShowOrHideAnimator != null) {
            mShowOrHideAnimator.cancel();
        }
    }

    public boolean onBackPressed() {
        if (getMParentView() != null && getMParentView().getVisibility() == View.VISIBLE) {
            hide();
            return true;
        }
        return false;
    }

    @Override
    public void onChangeBeauty(Type type, float progress) {
        BytedEffectFilter filter = ZqEngineKit.getInstance().getBytedEffectFilter();
        Params config = ZqEngineKit.getInstance().getParams();
        switch (type) {
            case dayan:
                config.setIntensityBigEye(progress);
                filter.updateReshape(config.getIntensityThinFace(), config.getIntensityBigEye());
                break;
            case shoulian:
                config.setIntensityThinFace(progress);
                filter.updateReshape(config.getIntensityThinFace(), config.getIntensityBigEye());
                break;
            case mopi:
                config.setIntensityMopi(progress);
                filter.updateIntensity(BytedEffectConstants.IntensityType.BeautySmooth.getId(), config.getIntensityMopi());
                break;
            case meibai:
                config.setIntensityMeibai(progress);
                filter.updateIntensity(BytedEffectConstants.IntensityType.BeautyWhite.getId(), config.getIntensityMeibai());
                break;
            case ruihua:
                config.setIntensityRuihua(progress);
                filter.updateIntensity(BytedEffectConstants.IntensityType.BeautySharp.getId(), config.getIntensityRuihua());
                break;
            // 滤镜
            case none_filter:
                config.setNoFilter(-1);
                filter.setFilter(-1);
                break;
            case ruanyang:
                config.setNoFilter(0);
                config.setIntensityFilter(progress);
                filter.setFilter(0);
                filter.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), config.getIntensityFilter());
                break;
            case musi:
                config.setNoFilter(1);
                config.setIntensityFilter(progress);
                filter.setFilter(1);
                filter.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), config.getIntensityFilter());
                break;
            case yangqi:
                config.setNoFilter(2);
                filter.setFilter(2);
                filter.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), config.getIntensityFilter());
                break;
            // 贴纸
            case none_sticker:
                config.setNoSticker(-1);
                filter.setSticker(-1);
                break;
            case cat:
                config.setNoSticker(0);
                filter.setSticker(0);
                break;
            case pump:
                config.setNoSticker(1);
                filter.setSticker(1);
                break;
            case rabbit:
                config.setNoSticker(2);
                filter.setSticker(2);
                break;
        }
    }

    public static class BeautyViewModel {
        Type mType;
        String color;  //正常的颜色
        int resId;     //给贴纸使用的

        public BeautyViewModel(Type type, String color, int resId) {
            this.mType = type;
            this.color = color;
            this.resId = resId;
        }

        public Type getType() {
            return mType;
        }

        public void setType(Type type) {
            mType = type;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public int getResId() {
            return resId;
        }

        public void setResId(int resId) {
            this.resId = resId;
        }
    }

    public enum Type {
        dayan(TYPE_BEAUTY, "大眼"), shoulian(TYPE_BEAUTY, "瘦脸"), mopi(TYPE_BEAUTY, "磨皮"),
        meibai(TYPE_BEAUTY, "美白"), ruihua(TYPE_BEAUTY, "锐化"),/* chunse(TYPE_BEAUTY, "唇色"), saihong(TYPE_BEAUTY, "腮红"),*/
        none_filter(TYPE_FITER, "无"), ruanyang(TYPE_FITER, "暖阳"), musi(TYPE_FITER, "慕斯"), yangqi(TYPE_FITER, "氧气"),
        none_sticker(TYPE_STICKER, "无"), cat(TYPE_STICKER, "猫咪"), pump(TYPE_STICKER, "南瓜"), rabbit(TYPE_STICKER, "兔子");

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
