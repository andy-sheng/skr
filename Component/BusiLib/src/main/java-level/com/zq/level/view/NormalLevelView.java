package com.zq.level.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.glidebitmappool.BitmapFactoryAdapter;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGADynamicEntity;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.level.utils.LevelConfigUtils;


import java.util.ArrayList;
import java.util.List;

// 正常的段位 到铂金段位 星星是斜着排的
public class NormalLevelView extends RelativeLayout {

    public final static String TAG = "NormalLevelView";

    int mainWidth = U.getDisplayUtils().dip2px(99);  // 主段位宽度
    int mainHeight = U.getDisplayUtils().dip2px(86); // 主段位高度
    int subWidth = U.getDisplayUtils().dip2px(44);   // 子段位宽度
    int subHeight = U.getDisplayUtils().dip2px(24);  // 子段位高度
    int sudBottom = U.getDisplayUtils().dip2px(1);   // 子段位距离主段位底部距离
    int normalStar = U.getDisplayUtils().dip2px(18); // 正常星星的大小
    int largeStar = U.getDisplayUtils().dip2px(20);  // 大星星的大小
    int starDiffH = U.getDisplayUtils().dip2px(6);   // 相邻两颗星星高度差
    int starTotalWidth = U.getDisplayUtils().dip2px(100);  // 所有星星高度总长度
    int starTotalHeight = 0;
    int textColor = Color.parseColor("#FFED61");   // 字体颜色
    float textSize = U.getDisplayUtils().dip2px(16); // 字体大小
    int horizonStar = U.getDisplayUtils().dip2px(20);// 横向星星大小

    ImageView mLevelIv; // 大段位
    ImageView mSubLeveIv;  // 子段位

    // 超过星星限制
    RelativeLayout mStarArea;
    ExImageView mImageStar;
    ExTextView mStarTv;

    List<ImageView> starts = new ArrayList<>(); // 星星数

    List<ImageView> rightStarts = new ArrayList<>(); // 右边的星星

    int level; //父段位
    int subLevel; //子段位
    int totalStats; //总星星数
    int selecStats; //亮着的星星

    int starUpSVGASize = U.getDisplayUtils().dip2px(60);
    int starLossSVGASize = U.getDisplayUtils().dip2px(100);

    List<SVGAImageView> mayLeaksSvgaViews = new ArrayList<>();

    public NormalLevelView(Context context) {
        super(context);
        init(context, null);
    }

    public NormalLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NormalLevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.levelView);
        mainWidth = typedArray.getDimensionPixelSize(R.styleable.levelView_mainWidth, U.getDisplayUtils().dip2px(99));
        mainHeight = typedArray.getDimensionPixelSize(R.styleable.levelView_mainHeight, U.getDisplayUtils().dip2px(86));
        subWidth = typedArray.getDimensionPixelSize(R.styleable.levelView_subWidth, U.getDisplayUtils().dip2px(15));
        subHeight = typedArray.getDimensionPixelSize(R.styleable.levelView_subHeight, U.getDisplayUtils().dip2px(15));
        sudBottom = typedArray.getDimensionPixelSize(R.styleable.levelView_subBottom, U.getDisplayUtils().dip2px(1));
        normalStar = typedArray.getDimensionPixelSize(R.styleable.levelView_normalStar, U.getDisplayUtils().dip2px(18));
        largeStar = typedArray.getDimensionPixelSize(R.styleable.levelView_largeStar, U.getDisplayUtils().dip2px(20));
        starDiffH = typedArray.getDimensionPixelSize(R.styleable.levelView_starDiffH, U.getDisplayUtils().dip2px(6));
        starTotalWidth = typedArray.getDimensionPixelSize(R.styleable.levelView_starTotalWidth, U.getDisplayUtils().dip2px(100));
        textColor = typedArray.getColor(R.styleable.levelView_txtColor, Color.parseColor("#FFED61"));
        textSize = typedArray.getDimension(R.styleable.levelView_txtSize, U.getDisplayUtils().dip2px(16));   // 字体大小
        horizonStar = typedArray.getDimensionPixelSize(R.styleable.levelView_horizonStar, U.getDisplayUtils().dip2px(20));// 横向星星大小
        typedArray.recycle();

        inflate(getContext(), R.layout.normal_level_view_layout, this);
        mLevelIv = (ImageView) this.findViewById(R.id.level_iv);
        mSubLeveIv = (ImageView) this.findViewById(R.id.sub_leve_iv);
        mStarArea = (RelativeLayout) this.findViewById(R.id.star_area);
        mImageStar = (ExImageView) this.findViewById(R.id.image_star);
        mStarTv = (ExTextView) this.findViewById(R.id.star_tv);

    }

    public void bindData(int level, int subLevel, int totalStats, int selecStats) {
        this.level = level;
        this.subLevel = subLevel;
        this.totalStats = totalStats;
        this.selecStats = selecStats;
        starTotalHeight = totalStats * starDiffH;

        // 主段位
        if (LevelConfigUtils.getImageResoucesLevel(level) != 0) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mainWidth, mainHeight);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            mLevelIv.setLayoutParams(params);
            mLevelIv.setBackground(getResources().getDrawable(LevelConfigUtils.getImageResoucesLevel(level)));
        }

        // 子段位
        if (LevelConfigUtils.getImageResoucesSubLevel(level, subLevel) != 0) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(subWidth, subHeight);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, mLevelIv.getId());
            params.setMargins(0, 0, 0, sudBottom);
            mSubLeveIv.setLayoutParams(params);
            mSubLeveIv.setBackground(getResources().getDrawable(LevelConfigUtils.getImageResoucesSubLevel(level, subLevel)));
        }

        initStart();
    }

    public void bindStarData(int totalStats, int selecStats) {
        MyLog.d(TAG, "bindStarData" + " totalStats=" + totalStats + " selecStats=" + selecStats);
        this.totalStats = totalStats;
        this.selecStats = selecStats;
        starTotalHeight = totalStats * starDiffH;

        initStart();
    }

    private void initStart() {
        // 先清除所有的星星
        if (starts != null) {
            for (ImageView imageView : starts) {
                removeView(imageView);
            }
            starts.clear();
            rightStarts.clear();
        }

        if (totalStats == 0 || totalStats > 6) {
            mStarArea.setVisibility(VISIBLE);
            mStarTv.setVisibility(VISIBLE);
            mImageStar.setVisibility(VISIBLE);
            ViewGroup.LayoutParams params = mImageStar.getLayoutParams();
            params.width = horizonStar;
            params.height = horizonStar;
            mImageStar.setLayoutParams(params);
            mStarTv.setText("x" + selecStats);
            mStarTv.setTextColor(textColor);
            mStarTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            return;
        }

        if (totalStats < selecStats) {
            MyLog.e(TAG, "bindStarData" + " level=" + level + " subLevel=" + subLevel + " totalStats=" + totalStats + " selecStats=" + selecStats);
            return;
        }

        mStarArea.setVisibility(GONE);

        float widDis = starTotalWidth / (totalStats + 1); //横向间距
        float highDis = starTotalHeight / (totalStats - 1); //纵向间距

        int mid = 0;
        if (totalStats % 2 == 0) {
            mid = totalStats / 2;
        } else {
            mid = totalStats / 2 + 1;
        }

        for (int i = 0; i < mid; i++) {
            // 左边的星星
            ImageView imageView1 = new ImageView(getContext());
            RelativeLayout.LayoutParams rl1;
            int left = (int) (widDis * (i + 1) - normalStar / 2);
            int bottom = Math.abs(starTotalHeight / 2 - (int) (highDis * i));
            if (totalStats % 2 != 0 && i == totalStats / 2) {
                rl1 = new RelativeLayout.LayoutParams(largeStar, largeStar);
                rl1.addRule(RelativeLayout.CENTER_HORIZONTAL);
            } else {
                rl1 = new RelativeLayout.LayoutParams(normalStar, normalStar);
                rl1.setMargins(left, 0, 0, bottom);
                rl1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            }
            rl1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            imageView1.setLayoutParams(rl1);
            if (i < selecStats) {
                imageView1.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.zhanji_daxingxing_dianliang));
            } else {
                imageView1.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.zhanji_xiaoxingxing_zhihui));
            }
            starts.add(imageView1);

            if (totalStats % 2 != 0 && i == totalStats / 2) {
                // 如果是放大的中间的星星，已处理，直接返回
                break;
            }

            // 与左边对称的星星
            ImageView imageView2 = new ImageView(getContext());
            RelativeLayout.LayoutParams rl2;
            rl2 = new RelativeLayout.LayoutParams(normalStar, normalStar);
            rl2.setMargins(0, 0, left, bottom);
            rl2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rl2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            imageView2.setLayoutParams(rl2);
            if ((totalStats - 1 - i) < selecStats) {
                imageView2.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.zhanji_daxingxing_dianliang));
            } else {
                imageView2.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.zhanji_xiaoxingxing_zhihui));
            }
            rightStarts.add(0, imageView2);
        }

        starts.addAll(rightStarts);
        for (ImageView imageView : starts) {
            addView(imageView);
        }
    }

    /**
     * 段位变换动画
     *
     * @param viewGroup      承载动画的容器
     * @param levelBefore    之前的父段位
     * @param subLevelBefore 之前的子段位
     * @param levelNow       现在的父段位
     * @param sublevelNow    现在的子段位
     */
    public void levelChange(final ViewGroup viewGroup, final int levelBefore, final int subLevelBefore, final int levelNow, final int sublevelNow,
                            int totalStatsNow,
                            final SVGAListener listener) {
        // 播放音效
//        U.getSoundUtils().play(TAG, R.raw.rank_levelchange);

        if (totalStatsNow == 0 || totalStatsNow > 6) {
            // 星星超过限制，不用动
        } else {
            // 现在段位下星星，并全变灰
            bindStarData(totalStatsNow, 0);
        }

        // 播放段位动画
        final SVGAImageView levelChange = new SVGAImageView(U.app());
        mayLeaksSvgaViews.add(levelChange);
        levelChange.setClearsAfterStop(false);   // 停在最后一帧
        levelChange.setLoops(1);  // 只播1次
        // 先隐藏之前的静态段位
        mLevelIv.setVisibility(GONE);
        mSubLeveIv.setVisibility(GONE);

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(375), U.getDisplayUtils().dip2px(400));
        rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rl.topMargin = U.getDisplayUtils().dip2px(160) - U.getDisplayUtils().dip2px(400 - 172) / 2;
        levelChange.setLayoutParams(rl);
        viewGroup.addView(levelChange);

            SvgaParserAdapter.parse("duanwei_change.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete( SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem, requestDynamicBitmapItem(levelBefore, subLevelBefore, levelNow, sublevelNow));
                    levelChange.setImageDrawable(drawable);
                    levelChange.startAnimation();
                }

                @Override
                public void onError() {

                }
            });

        levelChange.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (listener != null) {
                    listener.onFinish();
                }
            }

            @Override
            public void onRepeat() {

            }

            @Override
            public void onStep(int i, double v) {

            }
        });

    }

    private SVGADynamicEntity requestDynamicBitmapItem(int levelBefore, int subLevelBefore, int levelNow, int sublevelNow) {
        SVGADynamicEntity dynamicEntity = new SVGADynamicEntity();
        if (LevelConfigUtils.getImageResoucesSubLevel(levelBefore, subLevelBefore) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(getResources(), LevelConfigUtils.getImageResoucesSubLevel(levelBefore, subLevelBefore)), "keyLevelBefore");
        }
        if (LevelConfigUtils.getImageResoucesLevel(levelBefore) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(getResources(), LevelConfigUtils.getImageResoucesLevel(levelBefore)), "keyMedalBefore");
        }

        if (LevelConfigUtils.getImageResoucesSubLevel(levelNow, sublevelNow) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(getResources(), LevelConfigUtils.getImageResoucesSubLevel(levelNow, sublevelNow)), "keyLevelNew");
        }

        if (LevelConfigUtils.getImageResoucesLevel(levelNow) != 0) {
            dynamicEntity.setDynamicImage(BitmapFactoryAdapter.decodeResource(getResources(), LevelConfigUtils.getImageResoucesLevel(levelNow)), "keyMedalNew");
        }
        return dynamicEntity;
    }

    // 星星增加动画,从第几颗星增加到几个行
    // TODO: 2019/1/10 from 和 to都是从0开始计算
    public void starUp(final ViewGroup viewGroup, int from, final int to, final SVGAListener listener) {
        MyLog.d(TAG, "starUp" + " from=" + from + " to=" + to);
        for (int postion = from; postion <= to; postion++) {
            final int finalPostion = postion;
            if (finalPostion < 0 || finalPostion >= totalStats) {
                if (listener != null) {
                    listener.onFinish();
                }
                return;
            }
            mLevelIv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    starUp(viewGroup, finalPostion, new StarListener() {
                        @Override
                        public void onFinish(int index) {
                            if (index == to) {
                                if (listener != null) {
                                    listener.onFinish();
                                }
                            }
                        }
                    });
                }
            }, 300 * postion);

        }

    }

    // 星星掉落动画 from必须大于to，表示从第几颗星星掉落
    // TODO: 2019/1/10 from 和 to都是从0开始计算
    public void starLoss(final ViewGroup viewGroup, final int from, final int to, final SVGAListener listener) {
        MyLog.d(TAG, "starLoss" + " from=" + from + " to=" + to);
        bindStarData(totalStats, from + 1);
        for (int postion = from; postion >= to; postion--) {
            final int finalPostion = postion;
            if (finalPostion < 0 || finalPostion >= totalStats) {
                if (listener != null) {
                    listener.onFinish();
                }
                return;
            }
            mLevelIv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    starLoss(viewGroup, finalPostion, new StarListener() {
                        @Override
                        public void onFinish(int index) {
                            if (index == to) {
                                if (listener != null) {
                                    listener.onFinish();
                                }
                            }
                        }
                    });
                }
            }, 300 * (from - postion));

        }
    }

    private void starUp(ViewGroup viewGroup, final int index, final StarListener starListener) {
        if (index < 0 || index >= totalStats) {
            return;
        }
//        U.getSoundUtils().play(TAG, R.raw.rank_addstar);
        final SVGAImageView starUp = new SVGAImageView(getContext());
        mayLeaksSvgaViews.add(starUp);
        starUp.setLoops(1);  // 只播1次

        ImageView imageView = starts.get(index);
        int[] location = new int[2];
        imageView.getLocationOnScreen(location);

        if (totalStats % 2 != 0 && index == totalStats / 2) {
            // 大星星
            int width = starUpSVGASize * largeStar / normalStar;
            int height = starUpSVGASize * largeStar / normalStar;
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(width, height);
            rl.setMargins(Math.abs(location[0]) - (width - largeStar) / 2,
                    Math.abs(location[1]) - (height - largeStar) / 2, 0, 0);
            starUp.setLayoutParams(rl);
        } else {
            // 正常星星
            int width = starUpSVGASize;
            int height = starUpSVGASize;
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(width, height);
            rl.setMargins(Math.abs(location[0]) - (width - normalStar) / 2,
                    Math.abs(location[1]) - (height - normalStar) / 2, 0, 0);
            starUp.setLayoutParams(rl);
        }

        viewGroup.addView(starUp);

            SvgaParserAdapter.parse("star_up.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete( SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    starUp.setImageDrawable(drawable);
                    starUp.startAnimation();
                }

                @Override
                public void onError() {

                }
            });

        starUp.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                ImageView imageView = starts.get(index);
                imageView.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.zhanji_daxingxing_dianliang));
                if (starListener != null) {
                    starListener.onFinish(index);
                }
            }

            @Override
            public void onRepeat() {

            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    private void starLoss(ViewGroup viewGroup, final int index, final StarListener starListener) {
        if (index < 0 || index >= totalStats) {
            return;
        }
        final SVGAImageView starLoss = new SVGAImageView(getContext());
        mayLeaksSvgaViews.add(starLoss);
        starLoss.setClearsAfterStop(false);
        starLoss.setLoops(1);  // 只播1次

        // 音效
//        U.getSoundUtils().play(TAG, R.raw.rank_deductstar);

        final ImageView imageView = starts.get(index);
        int[] location = new int[2];
        imageView.getLocationOnScreen(location);

        if (totalStats % 2 != 0 && index == totalStats / 2) {
            // 大星星
            int width = starLossSVGASize * largeStar / normalStar;
            int height = starLossSVGASize * largeStar / normalStar;
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(width, height);
            rl.setMargins(Math.abs(location[0]) - (width - largeStar) / 2,
                    Math.abs(location[1]) - (height - largeStar) / 2, 0, 0);
            starLoss.setLayoutParams(rl);
        } else {
            // 正常星星
            int width = starLossSVGASize;
            int height = starLossSVGASize;
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(width, height);
            rl.setMargins(Math.abs(location[0]) - (width - normalStar) / 2,
                    Math.abs(location[1]) - (height - normalStar) / 2, 0, 0);
            starLoss.setLayoutParams(rl);
        }

        viewGroup.addView(starLoss);

            SvgaParserAdapter.parse("star_loss.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete( SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    imageView.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.zhanji_xiaoxingxing_zhihui));
                    starLoss.setImageDrawable(drawable);
                    starLoss.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        starLoss.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (starListener != null) {
                    starListener.onFinish(index);
                }
            }

            @Override
            public void onRepeat() {

            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (int i = 0; i < mayLeaksSvgaViews.size(); i++) {
            SVGAImageView view = mayLeaksSvgaViews.get(i);
            if (view != null) {
                view.setCallback(null);
                view.stopAnimation(true);
            }
        }
    }

    public interface SVGAListener {
        void onFinish();
    }

    public interface StarListener {
        void onFinish(int index);
    }
}
