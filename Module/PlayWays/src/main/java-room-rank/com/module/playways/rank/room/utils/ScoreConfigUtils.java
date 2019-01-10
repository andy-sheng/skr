package com.module.playways.rank.room.utils;

import com.module.rank.R;

public class ScoreConfigUtils {
    // 父段位资源
    public static int getImageResoucesScore(int score) {
        switch (score) {
            case 3:
                return R.drawable.score_f;
            case 4:
                return R.drawable.score_e;
            case 5:
                return R.drawable.score_d;
            case 6:
                return R.drawable.score_c;
            case 7:
                return R.drawable.score_b;
            case 8:
                return R.drawable.score_a;
            case 9:
                return R.drawable.score_s;
            case 10:
                return R.drawable.score_ss;
            case 11:
                return R.drawable.score_sss;
            default:
                return 0;
        }

    }
}
