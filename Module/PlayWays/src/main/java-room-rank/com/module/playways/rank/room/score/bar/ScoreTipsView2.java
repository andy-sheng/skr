package com.module.playways.rank.room.score.bar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;
import com.module.rank.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScoreTipsView2 extends View {

    List<Item> mItemList = new ArrayList<>();

    HashMap<Integer, Drawable> mHashMap = new HashMap<>();

    public ScoreTipsView2(Context context) {
        super(context);
        init();
    }

    public ScoreTipsView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScoreTipsView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int vw = getWidth();
        int vh = getHeight();
        List<Item> removeList = new ArrayList<>();
        for (Item item : mItemList) {
            Drawable drawable = getDrawable(item.level.mDrawableId);
            drawable.setAlpha((int) (255 * item.alpha));
            // 根据
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();
            int left = (int) (vw / 2 - dw * item.scale / 2);
            int top = (int) (vh - item.flyHeight * vh - dh * item.scale);
            int right = (int) (vw / 2 + dw * item.scale / 2);
            int bottom = vh - item.flyHeight * vh;
            drawable.setBounds(left, top, right, bottom);
            drawable.draw(canvas);
            item.goOn();
            if (item.isDead()) {
                removeList.add(item);
            }
        }
        for (Item item : removeList) {
            mItemList.remove(item);
        }
        if (!mItemList.isEmpty()) {
            postInvalidateDelayed(30);
        }
    }

    public void add(Item item) {
        item.setCreateTs(System.currentTimeMillis());
        mItemList.add(item);
        invalidate();
    }

    public Drawable getDrawable(int drawableId) {
        Drawable drawable = mHashMap.get(drawableId);
        if (drawable == null) {
            drawable = U.getDrawable(drawableId);
            mHashMap.put(drawableId, drawable);
        }
        return drawable;
    }

    public void setProgress(int progress) {
        ScoreTipsView2.Item item = new Item();
        if (progress > 90) {
            item.setLevel(Level.Perfect);
        } else if (progress > 70) {
            item.setLevel(Level.Good);
        } else if (progress > 50) {
            item.setLevel(Level.Ok);
        } else {
            item.setLevel(Level.Bad);
        }
        add(item);
    }

    enum Level {
        Perfect(R.drawable.yanchangjiemian_perfect),
        Good(R.drawable.yanchangjiemian_goood),
        Ok(R.drawable.yanchangjiemian_ok),
        Bad(R.drawable.yanchangjiemian_bad);

        int mDrawableId;

        Level(int id) {
            mDrawableId = id;
        }
    }


    public static class Item {
        Level level;

        int num = 1;

        float scale = 0.5f;

        float alpha = 0.5f;

        long createTs = 0;

        int flyHeight = 0;

        public Level getLevel() {
            return level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        private void setCreateTs(long createTs) {
            this.createTs = createTs;
        }

        public void goOn() {
            if (scale < 1) {
                scale += 0.1f;
                alpha += 0.1f;
            }
            if (flyHeight < 1) {
                flyHeight += 0.1f;
            }
        }

        public boolean isDead() {
            return flyHeight >= 1f;
        }
    }

}
