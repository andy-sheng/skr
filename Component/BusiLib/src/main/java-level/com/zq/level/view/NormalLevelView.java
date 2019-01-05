package com.zq.level.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.component.busilib.R;

import java.util.ArrayList;
import java.util.List;

// 正常的段位 到铂金段位
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

    int level; //父段位
    int subLevel; //子段位
    int totalStats; //总星星数
    int selecStats; //亮着的星星

    public NormalLevelView(Context context, int level, int subLevel, int totalStats, int selecStats) {
        super(context);

        this.level = level;
        this.subLevel = subLevel;
        this.totalStats = totalStats;
        this.selecStats = selecStats;

        starTotalHeight = totalStats * U.getDisplayUtils().dip2px(6);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.normal_level_view_layout, this);
        mLevelIv = (ImageView) this.findViewById(R.id.level_iv);
        mSubLeveIv = (ImageView) this.findViewById(R.id.sub_leve_iv);

        initStart();
        for (ImageView imageView : starts) {
            addView(imageView);
        }
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
            starts.add(imageView2);
        }
    }

}
