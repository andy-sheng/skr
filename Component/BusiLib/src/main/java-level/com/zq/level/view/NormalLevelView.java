package com.zq.level.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.component.busilib.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import model.UserScoreModel;

// 正常的段位 到铂金段位 星星是斜着排的
public class NormalLevelView extends RelativeLayout {

    int starTotalWidth = U.getDisplayUtils().dip2px(100);   // 星星的横向排列的长度
    int starTotalHeight;    //  星星的纵向排列的高度,每增加一颗星星就加6dp

    int widthStar = U.getDisplayUtils().dip2px(18); //普通星星的宽度
    int heightStar = U.getDisplayUtils().dip2px(18); //普通星星的长度

    int largeStarWidth = U.getDisplayUtils().dip2px(20); //星星的宽度
    int largeStarHeight = U.getDisplayUtils().dip2px(20); //星星的长度

    ImageView mLevelIv; // 大段位
    ImageView mSubLeveIv;  // 子段位

    List<ImageView> starts = new ArrayList<>(); // 星星数

    List<ImageView> rightStarts = new ArrayList<>(); // 右边的星星

    int level; //父段位
    int subLevel; //子段位
    int totalStats; //总星星数
    int selecStats; //亮着的星星

    public NormalLevelView(Context context) {
        super(context);
        init();
    }

    public NormalLevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NormalLevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.normal_level_view_layout, this);
        mLevelIv = (ImageView) this.findViewById(R.id.level_iv);
        mSubLeveIv = (ImageView) this.findViewById(R.id.sub_leve_iv);
    }

    public void bindData(int level, int subLevel, int totalStats, int selecStats) {
        this.level = level;
        this.subLevel = subLevel;
        this.totalStats = totalStats;
        this.selecStats = selecStats;
        starTotalHeight = totalStats * U.getDisplayUtils().dip2px(6);

        initStart();
    }

    private void initStart() {
        float widDis = starTotalWidth / (totalStats + 1); //横向间距
        float highDis = starTotalHeight / (totalStats - 1); //纵向间距

        for (int i = 0; i < totalStats / 2 + 1; i++) {
            // 左边的星星
            ImageView imageView1 = new ImageView(getContext());
            RelativeLayout.LayoutParams rl1;
            int left = (int) (widDis * (i + 1) - widthStar / 2);
            int bottom = Math.abs(starTotalHeight / 2 - (int) (highDis * i));
            if (totalStats % 2 != 0 && i == totalStats / 2) {
                rl1 = new RelativeLayout.LayoutParams(largeStarWidth, largeStarHeight);
                left = left - (largeStarWidth - widthStar) / 2;
                rl1.setMargins(left, 0, 0, bottom);
            } else {
                rl1 = new RelativeLayout.LayoutParams(widthStar, heightStar);
                rl1.setMargins(left, 0, 0, bottom);
            }
            rl1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rl1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
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
            rl2 = new RelativeLayout.LayoutParams(widthStar, heightStar);
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

    // 升段动画，段位提升动画
    public void levelUp(final ViewGroup viewGroup, List<UserScoreModel> userScoreModels) {
        // 根据解析UserScoreModel不同数据值，给SVGA指定的key
    }

    // 星星增加动画,从第几颗星增加到几个行
    public void starUp(final ViewGroup viewGroup, final int from, final int to) {
        final int dis = to - from;
        SVGACallback callback = new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (dis >= 0) {
                    starUp(viewGroup, from + 1, to);
                }
            }

            @Override
            public void onRepeat() {

            }

            @Override
            public void onStep(int i, double v) {

            }
        };
        starUp(viewGroup, from, callback);
    }

    private void starUp(ViewGroup viewGroup, int index, SVGACallback callback) {
        if (index < 0 || index >= totalStats) {
            return;
        }
        final SVGAImageView starUp = new SVGAImageView(getContext());
        starUp.setClearsAfterStop(false);   // 停在最后一帧
        starUp.setLoops(1);  // 只播1次

        ImageView imageView = starts.get(index);
        int[] location = new int[2];
        imageView.getLocationOnScreen(location);

        if (totalStats % 2 != 0 && index == totalStats / 2) {
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(134), U.getDisplayUtils().dip2px(134));
            rl.setMargins(Math.abs(location[0]) - U.getDisplayUtils().dip2px((134 - 20) / 2),
                    Math.abs(location[1]) - U.getDisplayUtils().dip2px((134 - 20) / 2), 0, 0);
            starUp.setLayoutParams(rl);
        } else {
            RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams((int) (U.getDisplayUtils().dip2px(134) * 0.9),
                    (int) (U.getDisplayUtils().dip2px(134) * 0.9));
            rl.setMargins(Math.abs(location[0]) - U.getDisplayUtils().dip2px((int) ((134 - 20) * 0.9 / 2)),
                    Math.abs(location[1]) - U.getDisplayUtils().dip2px((int) ((134 - 20) * 0.9 / 2)), 0, 0);
            starUp.setLayoutParams(rl);
        }

        viewGroup.addView(starUp);

        SVGAParser parser = new SVGAParser(getContext());
        try {
            parser.parse("start_up.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    starUp.setImageDrawable(drawable);
                    starUp.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }
        starUp.setCallback(callback);
    }

    // 星星掉落动画
    private void starDown() {

    }
}
