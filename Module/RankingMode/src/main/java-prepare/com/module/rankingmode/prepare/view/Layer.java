package com.module.rankingmode.prepare.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class Layer extends RelativeLayout implements ILayer{
    public static final String TAG = "CircleLayerView";

    public ArrayList<Sprite> sprites = new ArrayList<>();

    public Layer(Context context) {
        this(context, null);
    }

    public Layer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Layer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addSprite(Sprite view) {
        if(view == null){
            return;
        }

        sprites.add(view);

        int[] size = view.getSize();
        int[] location = view.getIconLocation();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size[0], size[1]);
        view.setLayoutParams(layoutParams);
        layoutParams.setMargins(location[0], location[1], 0, 0);
        addView(view);
    }

    @Override
    public void removeSprite(Sprite view) {
        if(view != null && this == view.getParent()){
            removeView(view);
        }
    }

    @Override
    public Sprite getAllSprite() {
        return null;
    }

    @Override
    public void addSpriteList(List<Sprite> viewList) {
        if(viewList != null){
            for (Sprite sprite : viewList){
                addSprite(sprite);
            }
        }
    }

    @Override
    public void reset() {
        removeAllViews();
    }
}
